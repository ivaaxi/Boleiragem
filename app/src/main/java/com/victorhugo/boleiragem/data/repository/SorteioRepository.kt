package com.victorhugo.boleiragem.data.repository

import com.victorhugo.boleiragem.data.dao.HistoricoTimeDao
import com.victorhugo.boleiragem.data.model.HistoricoTime
import com.victorhugo.boleiragem.data.model.ResultadoSorteio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SorteioRepository @Inject constructor(
    private val historicoTimeDao: HistoricoTimeDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _resultadoSorteio = MutableStateFlow<ResultadoSorteio?>(null)
    val resultadoSorteio: StateFlow<ResultadoSorteio?> = _resultadoSorteio.asStateFlow()

    // Flag para controlar se o sorteio está em andamento
    private val _sorteioEmAndamento = MutableStateFlow(false)
    val sorteioEmAndamento: StateFlow<Boolean> = _sorteioEmAndamento.asStateFlow()

    // Flag para controlar se há um sorteio não contabilizado
    private val _temSorteioNaoContabilizado = MutableStateFlow(false)
    val temSorteioNaoContabilizado: StateFlow<Boolean> = _temSorteioNaoContabilizado.asStateFlow()

    fun salvarResultadoSorteio(resultado: ResultadoSorteio) {
        _resultadoSorteio.value = resultado
        _temSorteioNaoContabilizado.value = true
        // Importante: não salvar os times no banco aqui para esperar confirmação do usuário
        // Agora também encerramos o estado de sorteio em andamento para resolver o problema de loading infinito
        _sorteioEmAndamento.value = false
    }

    // Método para efetivamente salvar os times no banco de dados após confirmação do usuário
    fun salvarTimesNoBancoDeDados(resultado: ResultadoSorteio) {
        val historicoTimes = resultado.times.mapIndexed { index, time ->
            // Calcular a média de estrelas do time
            val mediaEstrelas = time.jogadores.map { jogador ->
                jogador.notaPosicaoPrincipal.toFloat()
            }.average().toFloat()

            // Calcular a média de pontuação do time
            val mediaPontuacao = time.jogadores.map { jogador ->
                jogador.pontuacaoTotal.toFloat()
            }.average().toFloat()

            // Encontrar o jogador com maior pontuação ou estrelas para nomear o time
            val jogadorDestaque = when (resultado.tipoDeSorteio) {
                "Estrelas" -> time.jogadores.maxByOrNull { it.notaPosicaoPrincipal }
                else -> time.jogadores.maxByOrNull { it.pontuacaoTotal }
            } ?: time.jogadores.firstOrNull()

            val nomeTime = if (jogadorDestaque != null) {
                "Time do ${jogadorDestaque.nome}"
            } else {
                "Time ${index + 1}"
            }

            HistoricoTime(
                nome = nomeTime,
                jogadoresIds = time.jogadores.map { it.id },
                mediaEstrelas = mediaEstrelas,
                mediaPontuacao = mediaPontuacao,
                isUltimoPelada = true // Marca como último sorteio realizado
            )
        }

        // Salvamos realmente no banco de dados usando o DAO
        scope.launch {
            // Limpa a flag de última pelada para todos os times existentes
            historicoTimeDao.limparUltimaPelada()

            // Salvamos os novos times com a flag de última pelada ativa
            historicoTimes.forEach { historicoTime ->
                historicoTimeDao.inserirHistoricoTime(historicoTime)
            }

            // Reset da flag de sorteio não contabilizado
            resetSorteioContabilizacao()
        }
    }

    fun setSorteioEmAndamento(emAndamento: Boolean) {
        _sorteioEmAndamento.value = emAndamento
    }

    fun limparResultado() {
        _resultadoSorteio.value = null
    }

    fun getHistoricoTimes(): Flow<List<HistoricoTime>> {
        // Utiliza o DAO para buscar os dados reais do banco
        return historicoTimeDao.getHistoricoTimes()
    }

    suspend fun atualizarHistoricoTime(time: HistoricoTime) {
        // Chama o DAO para atualizar o time no banco de dados
        historicoTimeDao.atualizarHistoricoTime(time)
    }

    fun resetSorteioContabilizacao() {
        _temSorteioNaoContabilizado.value = false
    }

    // Método para limpar todos os times do histórico
    suspend fun limparHistoricoTimes() {
        // Primeiro limpamos a lista de times em memória
        _resultadoSorteio.value = null
        _temSorteioNaoContabilizado.value = false

        try {
            // Precisamos coletar o Flow para obter a lista atual de times
            val timesAtuais = historicoTimeDao.getHistoricoTimes().first()

            // Removemos cada time do banco de dados
            timesAtuais.forEach { time ->
                historicoTimeDao.deletarHistoricoTime(time)
            }
        } catch (e: Exception) {
            // Loga o erro ou trata de acordo com a necessidade
            e.printStackTrace()
        }
    }

    // Método para carregar os times da última pelada ao iniciar o app
    fun getTimesUltimaPelada(): Flow<List<HistoricoTime>> {
        return historicoTimeDao.getTimesUltimaPelada()
    }

    // Método para registrar o resultado de uma pelada
    suspend fun registrarResultadoPelada(
        timeVencedorId: Long,
        timesPerdedoresIds: List<Long>,
        timesEmpatadosIds: List<Long>,
        pontosPorVitoria: Int,
        pontosPorEmpate: Int,
        jogadorRepository: JogadorRepository
    ) {
        // Primeiro, atualizamos o histórico do time vencedor (se houver)
        if (timeVencedorId > 0) {
            val timeVencedor = historicoTimeDao.getHistoricoTimePorId(timeVencedorId)
            timeVencedor?.let { time ->
                // Atualizamos o histórico do time
                historicoTimeDao.registrarVitoria(timeVencedorId)

                // Atualizamos as estatísticas dos jogadores
                jogadorRepository.registrarVitoria(time.jogadoresIds, pontosPorVitoria)
            }
        }

        // Registra as derrotas para os times perdedores
        timesPerdedoresIds.forEach { timeId ->
            val time = historicoTimeDao.getHistoricoTimePorId(timeId)
            time?.let {
                // Atualizamos o histórico do time
                historicoTimeDao.registrarDerrota(timeId)

                // Atualizamos as estatísticas dos jogadores
                jogadorRepository.registrarDerrota(it.jogadoresIds)
            }
        }

        // Registra os empates para os times empatados
        timesEmpatadosIds.forEach { timeId ->
            val time = historicoTimeDao.getHistoricoTimePorId(timeId)
            time?.let {
                // Atualizamos o histórico do time
                historicoTimeDao.registrarEmpate(timeId)

                // Atualizamos as estatísticas dos jogadores
                jogadorRepository.registrarEmpate(it.jogadoresIds, pontosPorEmpate)
            }
        }
    }
}
