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
        if (jogadoresAtivos.size < totalJogadoresNecessarios) {
            // Não temos jogadores suficientes, ajusta a configuração
            val novaQtdTimes = jogadoresAtivos.size / configuracao.qtdJogadoresPorTime
            if (novaQtdTimes == 0) {
                // Não temos jogadores suficientes nem para um time, ajusta jogadores por time
                val jogadoresPorTime = jogadoresAtivos.size
                return sortearTimesAjustado(jogadoresAtivos, jogadoresPorTime, 1, configuracao.aleatorio, configuracao.criteriosExtras)
            }
            return sortearTimesAjustado(jogadoresAtivos, configuracao.qtdJogadoresPorTime, novaQtdTimes, configuracao.aleatorio, configuracao.criteriosExtras)
        }

        return sortearTimesAjustado(jogadoresAtivos, configuracao.qtdJogadoresPorTime, configuracao.qtdTimes, configuracao.aleatorio, configuracao.criteriosExtras)
    }

    private fun sortearTimesAjustado(
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

        // Distribuir jogadores em times (serpentina)
        val times = distribuirJogadoresEmTimes(jogadoresOrdenados, jogadoresPorTime, qtdTimes)

        return ResultadoSorteio(times)
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
