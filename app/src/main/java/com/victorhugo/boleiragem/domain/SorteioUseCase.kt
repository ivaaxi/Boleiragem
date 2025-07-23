package com.victorhugo.boleiragem.domain

import com.victorhugo.boleiragem.data.model.ConfiguracaoSorteio
import com.victorhugo.boleiragem.data.model.CriterioSorteio
import com.victorhugo.boleiragem.data.model.Jogador
import com.victorhugo.boleiragem.data.model.PosicaoJogador
import com.victorhugo.boleiragem.data.model.ResultadoSorteio
import com.victorhugo.boleiragem.data.model.Time
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SorteioUseCase @Inject constructor() {

    fun sortearTimes(
        jogadoresAtivos: List<Jogador>,
        configuracao: ConfiguracaoSorteio
    ): ResultadoSorteio {
        if (jogadoresAtivos.isEmpty()) {
            return ResultadoSorteio(emptyList())
        }

        // Verifica se temos jogadores suficientes para formar os times
        val totalJogadoresNecessarios = configuracao.qtdJogadoresPorTime * configuracao.qtdTimes

        // Se não tivermos jogadores suficientes para todos os times completos
        if (jogadoresAtivos.size < configuracao.qtdTimes) {
            // Não temos nem um jogador para cada time configurado
            val novaQtdTimes = 1 // No mínimo um time
            return sortearTimesComReservas(jogadoresAtivos, configuracao.qtdJogadoresPorTime, novaQtdTimes, configuracao.aleatorio, configuracao.criteriosExtras)
        }

        // Usamos o novo método que cria time reserva se necessário
        return sortearTimesComReservas(jogadoresAtivos, configuracao.qtdJogadoresPorTime, configuracao.qtdTimes, configuracao.aleatorio, configuracao.criteriosExtras)
    }

    private fun sortearTimesComReservas(
        jogadoresAtivos: List<Jogador>,
        jogadoresPorTime: Int,
        qtdTimes: Int,
        aleatorio: Boolean,
        criteriosExtras: Set<CriterioSorteio>
    ): ResultadoSorteio {
        // Ordenar jogadores conforme critérios
        val jogadoresOrdenados = if (aleatorio) {
            // Se aleatório está ativado, ignora todos os outros critérios
            jogadoresAtivos.shuffled()
        } else {
            // Se aleatório está desativado, usa os critérios extras
            when {
                // Verifica se PONTUACAO está entre os critérios
                criteriosExtras.contains(CriterioSorteio.PONTUACAO) -> {
                    // Começa ordenando por pontuação
                    val jogadoresPorPontuacao = ordenarPorPontuacao(jogadoresAtivos)

                    // Aplica critérios adicionais se necessário
                    when {
                        criteriosExtras.contains(CriterioSorteio.POSICAO) && criteriosExtras.contains(CriterioSorteio.MEDIA_NOTAS) ->
                            ordenarPorPosicaoEMedia(jogadoresPorPontuacao)

                        criteriosExtras.contains(CriterioSorteio.POSICAO) ->
                            ordenarPorPosicao(jogadoresPorPontuacao)

                        criteriosExtras.contains(CriterioSorteio.MEDIA_NOTAS) ->
                            ordenarPorMedia(jogadoresPorPontuacao)

                        else -> jogadoresPorPontuacao
                    }
                }

                // Se não tem PONTUACAO, verifica outros critérios
                criteriosExtras.contains(CriterioSorteio.MEDIA_NOTAS) ->
                    ordenarPorMedia(jogadoresAtivos)

                criteriosExtras.contains(CriterioSorteio.POSICAO) ->
                    ordenarPorPosicao(jogadoresAtivos)

                // Caso nenhum critério esteja selecionado (não deveria acontecer, mas como fallback)
                else -> jogadoresAtivos.shuffled()
            }
        }

        // Nova lógica de distribuição de jogadores
        // Vamos tentar completar o máximo de times possível com o número correto de jogadores
        val timesCompletos = qtdTimes
        val jogadoresEmTimesCompletos = timesCompletos * jogadoresPorTime

        // Se temos jogadores suficientes para todos os times
        if (jogadoresOrdenados.size >= jogadoresEmTimesCompletos) {
            // Jogadores para times completos
            val jogadoresParaTimesCompletos = jogadoresOrdenados.take(jogadoresEmTimesCompletos)
            // Jogadores sobrando vão para o time reserva
            val jogadoresReserva = jogadoresOrdenados.drop(jogadoresEmTimesCompletos)

            // Distribuir jogadores nos times completos
            val timesCompletos = distribuirJogadoresEmTimes(jogadoresParaTimesCompletos, jogadoresPorTime, timesCompletos)

            // Se tivermos jogadores na reserva, criamos um time reserva
            val todosOsTimes = if (jogadoresReserva.isNotEmpty()) {
                val timeReserva = Time(
                    id = timesCompletos.size, // ID do time reserva é o próximo após os times completos
                    nome = "Time Reserva", // Nome fixo: "Time Reserva"
                    jogadores = jogadoresReserva,
                    ehTimeReserva = true // Marca como time reserva
                )
                timesCompletos + timeReserva
            } else {
                timesCompletos
            }

            return ResultadoSorteio(todosOsTimes)
        } else {
            // Não temos jogadores suficientes para todos os times completos
            // Vamos criar times com o máximo de jogadores possível e um time reserva se necessário

            // Calculamos quantos times completos podemos formar
            val timesCompletosPodemSerFormados = jogadoresOrdenados.size / jogadoresPorTime

            if (timesCompletosPodemSerFormados > 0) {
                // Jogadores para os times completos
                val jogadoresParaTimesCompletos = jogadoresOrdenados.take(timesCompletosPodemSerFormados * jogadoresPorTime)
                // Jogadores restantes vão para o time reserva
                val jogadoresReserva = jogadoresOrdenados.drop(timesCompletosPodemSerFormados * jogadoresPorTime)

                // Distribuir jogadores nos times completos
                val timesCompletos = distribuirJogadoresEmTimes(jogadoresParaTimesCompletos, jogadoresPorTime, timesCompletosPodemSerFormados)

                // Criamos o time reserva com os jogadores restantes
                val timeReserva = Time(
                    id = timesCompletosPodemSerFormados,
                    nome = "Time Reserva",
                    jogadores = jogadoresReserva,
                    ehTimeReserva = true // Marca como time reserva
                )

                return ResultadoSorteio(timesCompletos + timeReserva)
            } else {
                // Não conseguimos formar nem um time completo, todos os jogadores vão para o time reserva
                val timeReserva = Time(
                    id = 0,
                    nome = "Time Reserva",
                    jogadores = jogadoresOrdenados,
                    ehTimeReserva = true // Marca como time reserva
                )

                return ResultadoSorteio(listOf(timeReserva))
            }
        }
    }

    private fun ordenarPorPontuacao(jogadores: List<Jogador>): List<Jogador> {
        // Ordena jogadores pela pontuação total (do maior para o menor)
        return jogadores.sortedByDescending { it.pontuacaoTotal }
    }

    private fun ordenarPorPosicaoEMedia(jogadores: List<Jogador>): List<Jogador> {
        // Agrupa por posição e ordena cada grupo por média de nota
        return jogadores
            .groupBy { it.posicaoPrincipal }
            .flatMap { (_, jogadoresDaPosicao) ->
                jogadoresDaPosicao.sortedByDescending { it.notaPosicaoPrincipal }
            }
    }

    private fun ordenarPorPosicao(jogadores: List<Jogador>): List<Jogador> {
        // Primeiro ordena por posição e depois mantém a ordem original dentro de cada posição
        return jogadores.sortedBy { it.posicaoPrincipal.ordinal }
    }

    private fun ordenarPorMedia(jogadores: List<Jogador>): List<Jogador> {
        // Ordena por média das notas (da maior para a menor)
        return jogadores.sortedByDescending { it.notaPosicaoPrincipal }
    }

    private fun distribuirJogadoresEmTimes(
        jogadoresOrdenados: List<Jogador>,
        jogadoresPorTime: Int,
        qtdTimes: Int
    ): List<Time> {
        // Inicializa a lista de times com listas mutáveis de jogadores
        var times = List(qtdTimes) { index ->
            Time(
                id = index, // Usando Int em vez de Long
                nome = "Time ${index + 1}",
                jogadores = ArrayList() // Usando ArrayList que é mutável e suporta o método add
            )
        }

        // Distribui jogadores em times usando método de serpentina
        var direcaoCrescente = true
        var timeIndex = 0

        jogadoresOrdenados.forEach { jogador ->
            // Obter lista atual de jogadores
            val jogadoresAtuais = ArrayList(times[timeIndex].jogadores)
            // Adicionar o novo jogador
            jogadoresAtuais.add(jogador)
            // Criar um novo Time com a lista atualizada
            times = times.toMutableList().apply {
                set(timeIndex, times[timeIndex].copy(jogadores = jogadoresAtuais))
            }

            // Atualiza o índice do time para o próximo jogador
            if (direcaoCrescente) {
                timeIndex++
                if (timeIndex >= qtdTimes) {
                    timeIndex = qtdTimes - 1
                    direcaoCrescente = false
                }
            } else {
                timeIndex--
                if (timeIndex < 0) {
                    timeIndex = 0
                    direcaoCrescente = true
                }
            }
        }

        return times
    }
}
