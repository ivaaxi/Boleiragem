package com.victorhugo.boleiragem.data.repository

import com.victorhugo.boleiragem.data.dao.ConfiguracaoPontuacaoDao
import com.victorhugo.boleiragem.data.dao.JogadorDao
import com.victorhugo.boleiragem.data.model.ConfiguracaoPontuacao
import com.victorhugo.boleiragem.data.model.HistoricoTime
import com.victorhugo.boleiragem.data.model.Jogador
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
        configuracaoPontuacaoDao.atualizarConfiguracaoPontuacao(configuracao)

        // Recalcular a pontuação de todos os jogadores com base na nova configuração
        recalcularPontuacaoJogadores()
    }

    // Finaliza a partida e atualiza as estatísticas dos jogadores
    suspend fun finalizarPartida(times: List<HistoricoTime>) {
        val configuracao = withContext(Dispatchers.IO) {
            configuracaoPontuacaoDao.getConfiguracaoPontuacao().first() ?: ConfiguracaoPontuacao()
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
            configuracaoPontuacaoDao.getConfiguracaoPontuacao().first() ?: ConfiguracaoPontuacao()
        }

        val jogadores = withContext(Dispatchers.IO) {
            jogadorDao.getJogadoresList()
        }

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

                scope.launch {
                    jogadorDao.atualizarJogador(jogadorAtualizado)
                }
            }
        }
    }
}
