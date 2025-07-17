package com.victorhugo.boleiragem.data.repository

import com.victorhugo.boleiragem.data.dao.ConfiguracaoPontuacaoDao
import com.victorhugo.boleiragem.data.dao.JogadorDao
import com.victorhugo.boleiragem.data.model.ConfiguracaoPontuacao
import com.victorhugo.boleiragem.data.model.HistoricoTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PontuacaoRepository @Inject constructor(
    private val configuracaoPontuacaoDao: ConfiguracaoPontuacaoDao,
    private val jogadorDao: JogadorDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    // Obtém a configuração atual de pontuação
    fun getConfiguracaoPontuacao(): Flow<ConfiguracaoPontuacao> {
        return configuracaoPontuacaoDao.getConfiguracaoPontuacao()
    }

    // Atualiza a configuração de pontuação
    suspend fun atualizarConfiguracaoPontuacao(configuracao: ConfiguracaoPontuacao) {
        // Garantir que estamos sempre usando o ID 1 para a configuração de pontuação
        val configComIdCorreto = configuracao.copy(id = 1)

        try {
            // Tenta atualizar primeiro
            val linhasAtualizadas = configuracaoPontuacaoDao.atualizarConfiguracaoPontuacao(configComIdCorreto)

            // Se nenhuma linha foi atualizada, então o registro não existe e precisamos inserir
            if (linhasAtualizadas.equals(0)) {
                configuracaoPontuacaoDao.inserirConfiguracaoPontuacao(configComIdCorreto)
            }

            // Depois de salvar, recalcula a pontuação dos jogadores
            recalcularPontuacaoJogadores()
        } catch (_: Exception) {
            // Em caso de falha, tenta inserir diretamente
            try {
                configuracaoPontuacaoDao.inserirConfiguracaoPontuacao(configComIdCorreto)
                recalcularPontuacaoJogadores()
            } catch (innerE: Exception) {
                innerE.printStackTrace()
                throw innerE  // Propaga a exceção para tratamento superior
            }
        }
    }

    // Finaliza a partida e atualiza as estatísticas dos jogadores
    suspend fun finalizarPartida(times: List<HistoricoTime>) {
        val configuracao = withContext(Dispatchers.IO) {
            configuracaoPontuacaoDao.getConfiguracaoPontuacao().first()
        }

        val todosJogadores = withContext(Dispatchers.IO) {
            jogadorDao.getJogadoresList()
        }

        val jogadoresMapa = todosJogadores.associateBy { it.id }

        // Para cada time, atualiza os jogadores com vitórias, derrotas ou empates
        times.forEach { time ->
            val jogadoresIds = time.jogadoresIds

            // Atualizar as estatísticas de cada jogador do time
            jogadoresIds.forEach { jogadorId ->
                jogadoresMapa[jogadorId]?.let { jogador ->
                    // Criar uma cópia do jogador com as estatísticas atualizadas
                    val jogadorAtualizado = jogador.copy(
                        totalJogos = jogador.totalJogos + 1,
                        vitorias = jogador.vitorias + if (time.vitorias > 0) 1 else 0,
                        derrotas = jogador.derrotas + if (time.derrotas > 0) 1 else 0,
                        empates = jogador.empates + if (time.empates > 0) 1 else 0
                    )

                    // Calcular a pontuação
                    val pontuacaoVitorias = jogadorAtualizado.vitorias * configuracao.pontosPorVitoria
                    val pontuacaoDerrotas = jogadorAtualizado.derrotas * configuracao.pontosPorDerrota
                    val pontuacaoEmpates = jogadorAtualizado.empates * configuracao.pontosPorEmpate

                    val pontuacaoTotal = pontuacaoVitorias + pontuacaoDerrotas + pontuacaoEmpates

                    // Atualizar o jogador com a nova pontuação
                    val jogadorComPontuacao = jogadorAtualizado.copy(
                        pontuacaoTotal = pontuacaoTotal
                    )

                    // Salvar o jogador atualizado
                    scope.launch {
                        jogadorDao.atualizarJogador(jogadorComPontuacao)
                    }
                }
            }
        }
    }

    // Recalcula a pontuação de todos os jogadores com base na configuração atual
    private suspend fun recalcularPontuacaoJogadores() {
        val configuracao = withContext(Dispatchers.IO) {
            configuracaoPontuacaoDao.getConfiguracaoPontuacao().first()
        }

        val jogadores = withContext(Dispatchers.IO) {
            jogadorDao.getJogadoresList()
        }

        // Utiliza withContext para garantir que todas as atualizações sejam concluídas
        withContext(Dispatchers.IO) {
            jogadores.forEach { jogador ->
                // Calcular a pontuação com base na nova configuração
                val pontuacaoVitorias = jogador.vitorias * configuracao.pontosPorVitoria
                val pontuacaoDerrotas = jogador.derrotas * configuracao.pontosPorDerrota
                val pontuacaoEmpates = jogador.empates * configuracao.pontosPorEmpate

                val novaPontuacaoTotal = pontuacaoVitorias + pontuacaoDerrotas + pontuacaoEmpates

                // Atualizar o jogador apenas se a pontuação mudou
                if (novaPontuacaoTotal != jogador.pontuacaoTotal) {
                    val jogadorAtualizado = jogador.copy(
                        pontuacaoTotal = novaPontuacaoTotal
                    )

                    // Atualiza o jogador diretamente, sem usar launch
                    jogadorDao.atualizarJogador(jogadorAtualizado)
                }
            }
        }

        // Pequeno atraso para garantir que as atualizações sejam refletidas no banco de dados
        kotlinx.coroutines.delay(200)
    }

    // Atualiza as estatísticas e a pontuação de um jogador específico
    suspend fun atualizarPontuacaoJogador(jogador: com.victorhugo.boleiragem.data.model.Jogador) {
        try {
            // Obter a configuração atual de pontuação
            val configuracao = withContext(Dispatchers.IO) {
                configuracaoPontuacaoDao.getConfiguracaoPontuacao().first()
            }

            // Calcular a pontuação com base nas estatísticas atualizadas
            val pontuacaoVitorias = jogador.vitorias * configuracao.pontosPorVitoria
            val pontuacaoDerrotas = jogador.derrotas * configuracao.pontosPorDerrota
            val pontuacaoEmpates = jogador.empates * configuracao.pontosPorEmpate
            val pontuacaoTotal = pontuacaoVitorias + pontuacaoDerrotas + pontuacaoEmpates

            // Criar uma cópia do jogador com a pontuação atualizada
            val jogadorComPontuacao = jogador.copy(pontuacaoTotal = pontuacaoTotal)

            // Salvar o jogador atualizado no banco de dados
            withContext(Dispatchers.IO) {
                jogadorDao.atualizarJogador(jogadorComPontuacao)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
