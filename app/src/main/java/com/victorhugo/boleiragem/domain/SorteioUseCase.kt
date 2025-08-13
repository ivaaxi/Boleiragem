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
            aplicarCriteriosOrdenacao(jogadoresAtivos, criteriosExtras)
        }

        // Nova lógica de distribuição de jogadores
        // Vamos tentar completar o máximo de times possível com o número correto de jogadores
        val timesCompletosConfigurados = qtdTimes
        val jogadoresEmTimesCompletos = timesCompletosConfigurados * jogadoresPorTime

        // Se temos jogadores suficientes para todos os times
        if (jogadoresOrdenados.size >= jogadoresEmTimesCompletos) {
            // Jogadores para times completos
            val jogadoresParaTimesCompletos = jogadoresOrdenados.take(jogadoresEmTimesCompletos)
            // Jogadores sobrando vão para o time reserva
            val jogadoresReserva = jogadoresOrdenados.drop(jogadoresEmTimesCompletos)

            // Distribuir jogadores nos times completos
            val timesPrincipais = distribuirJogadoresEmTimes(jogadoresParaTimesCompletos, jogadoresPorTime, timesCompletosConfigurados)

            // Se tivermos jogadores na reserva, criamos um time reserva
            val todosOsTimes = if (jogadoresReserva.isNotEmpty()) {
                val timeReserva = Time(
                    id = timesPrincipais.size, // ID do time reserva é o próximo após os times completos
                    nome = "Time Reserva", // Nome fixo: "Time Reserva"
                    jogadores = jogadoresReserva,
                    ehTimeReserva = true // Marca como time reserva
                )
                timesPrincipais + timeReserva
            } else {
                timesPrincipais
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
                val timesPrincipais = distribuirJogadoresEmTimes(jogadoresParaTimesCompletos, jogadoresPorTime, timesCompletosPodemSerFormados)

                // Criamos o time reserva com os jogadores restantes
                val timeReserva = Time(
                    id = timesPrincipais.size,
                    nome = "Time Reserva",
                    jogadores = jogadoresReserva,
                    ehTimeReserva = true // Marca como time reserva
                )

                return ResultadoSorteio(timesPrincipais + timeReserva)
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

    // Método sortearTimesRapido modificado para incluir time reserva
    fun sortearTimesRapido(
        jogadores: List<Jogador>,
        configuracao: ConfiguracaoSorteio, // Usaremos qtdJogadoresPorTime daqui
        numeroTimesPrincipais: Int // Este é o número de times principais desejado
    ): ResultadoSorteio {
        if (jogadores.isEmpty()) {
            return ResultadoSorteio(emptyList())
        }

        // Ordenar jogadores conforme critérios da configuração (aleatorio ou criteriosExtras)
        val jogadoresOrdenados = if (configuracao.aleatorio) {
            jogadores.shuffled()
        } else {
            aplicarCriteriosOrdenacao(jogadores, configuracao.criteriosExtras)
        }

        val jogadoresPorTimeDefinido = configuracao.qtdJogadoresPorTime
        val totalJogadoresParaTimesPrincipais = numeroTimesPrincipais * jogadoresPorTimeDefinido

        // ViewModel já deve garantir que jogadores.size >= totalJogadoresParaTimesPrincipais
        // Se, por algum motivo, não for o caso, pode-se adicionar um tratamento de erro ou fallback aqui,
        // mas idealmente a camada de ViewModel (GruposPeladaViewModel) já validou isso.

        val jogadoresParaDistribuirNosTimes = jogadoresOrdenados.take(totalJogadoresParaTimesPrincipais)
        val jogadoresReserva = jogadoresOrdenados.drop(totalJogadoresParaTimesPrincipais)

        val timesPrincipais = distribuirJogadoresEmTimes(
            jogadoresParaDistribuirNosTimes,
            jogadoresPorTimeDefinido,
            numeroTimesPrincipais
        )

        val todosOsTimes = if (jogadoresReserva.isNotEmpty()) {
            val timeReserva = Time(
                id = timesPrincipais.size, // ID sequencial após os times principais
                nome = "Time Reserva",
                jogadores = jogadoresReserva,
                ehTimeReserva = true
            )
            timesPrincipais + timeReserva
        } else {
            timesPrincipais
        }

        return ResultadoSorteio(todosOsTimes)
    }

    private fun aplicarCriteriosOrdenacao(
        jogadores: List<Jogador>,
        criteriosExtras: Set<CriterioSorteio>
    ): List<Jogador> {
        return when {
            criteriosExtras.contains(CriterioSorteio.PONTUACAO) -> {
                val jogadoresPorPontuacao = ordenarPorPontuacao(jogadores)
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
            criteriosExtras.contains(CriterioSorteio.MEDIA_NOTAS) ->
                ordenarPorMedia(jogadores)
            criteriosExtras.contains(CriterioSorteio.POSICAO) ->
                ordenarPorPosicao(jogadores)
            else -> jogadores.shuffled() // Fallback para aleatório se nenhum critério especificado
        }
    }

    private fun ordenarPorPontuacao(jogadores: List<Jogador>): List<Jogador> {
        return jogadores.sortedByDescending { it.pontuacaoTotal }
    }

    private fun ordenarPorPosicaoEMedia(jogadores: List<Jogador>): List<Jogador> {
        return jogadores
            .groupBy { it.posicaoPrincipal }
            .flatMap { (_, jogadoresDaPosicao) ->
                jogadoresDaPosicao.sortedByDescending { it.notaPosicaoPrincipal }
            }
    }

    private fun ordenarPorPosicao(jogadores: List<Jogador>): List<Jogador> {
        return jogadores.sortedBy { it.posicaoPrincipal.ordinal }
    }

    private fun ordenarPorMedia(jogadores: List<Jogador>): List<Jogador> {
        return jogadores.sortedByDescending { it.notaPosicaoPrincipal }
    }

    private fun distribuirJogadoresEmTimes(
        jogadoresOrdenados: List<Jogador>,
        jogadoresPorTime: Int,
        qtdTimes: Int
    ): List<Time> {
        if (qtdTimes <= 0 || jogadoresPorTime <= 0) return emptyList()

        val times = MutableList(qtdTimes) { index ->
            Time(
                id = index,
                nome = "Time ${index + 1}",
                jogadores = ArrayList()
            )
        }

        var direcaoCrescente = true
        var timeIndex = 0
        var jogadoresAlocados = 0

        jogadoresOrdenados.forEach { jogador ->
            if (jogadoresAlocados >= jogadoresOrdenados.size) return@forEach // Evita IndexOutOfBounds se jogadoresOrdenados for menor que o esperado

            (times[timeIndex].jogadores as ArrayList).add(jogador)
            jogadoresAlocados++

            if (jogadoresAlocados % jogadoresPorTime == 0 && jogadoresAlocados >= jogadoresPorTime * (timeIndex +1) && qtdTimes > 1) { // Lógica para avançar time após ele estar "cheio" conceitualmente
                 // A distribuição serpentina continua baseada na iteração total
            }

            if (qtdTimes > 1) { // Apenas muda de time se houver mais de um time
                if (direcaoCrescente) {
                    timeIndex++
                    if (timeIndex >= qtdTimes) {
                        timeIndex = qtdTimes - 2 // Volta para o penúltimo para descer
                        direcaoCrescente = false
                    }
                } else {
                    timeIndex--
                    if (timeIndex < 0) {
                        timeIndex = 1 // Volta para o segundo para subir
                        direcaoCrescente = true
                    }
                }
            }
        }
        return times
    }
}
