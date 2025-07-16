package com.victorhugo.boleiragem.ui.screens.sorteio

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorhugo.boleiragem.data.model.ConfiguracaoSorteio
import com.victorhugo.boleiragem.data.model.Jogador
import com.victorhugo.boleiragem.data.model.ResultadoSorteio
import com.victorhugo.boleiragem.data.repository.ConfiguracaoRepository
import com.victorhugo.boleiragem.data.repository.JogadorRepository
import com.victorhugo.boleiragem.data.repository.SorteioRepository
import com.victorhugo.boleiragem.domain.SorteioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SorteioTimesViewModel @Inject constructor(
    private val jogadorRepository: JogadorRepository,
    private val configuracaoRepository: ConfiguracaoRepository,
    private val sorteioRepository: SorteioRepository,
    private val sorteioUseCase: SorteioUseCase
) : ViewModel() {

    val jogadores = jogadorRepository.getJogadores()
    val configuracao = configuracaoRepository.getConfiguracao()
    val resultadoSorteio = sorteioRepository.resultadoSorteio
    val loading = sorteioRepository.sorteioEmAndamento
    val temSorteioNaoContabilizado = sorteioRepository.temSorteioNaoContabilizado

    private val _sorteioRealizado = MutableStateFlow(false)
    val sorteioRealizado: StateFlow<Boolean> = _sorteioRealizado

    private val _mostrarDialogConfirmacao = MutableStateFlow(false)
    val mostrarDialogConfirmacao: StateFlow<Boolean> = _mostrarDialogConfirmacao

    val jogadoresSelecionados = mutableStateListOf<Long>()

    init {
        viewModelScope.launch {
            jogadorRepository.getJogadores().collect { jogadoresLista ->
                // Inicialmente, seleciona todos jogadores ativos
                jogadoresSelecionados.clear()
                jogadoresSelecionados.addAll(
                    jogadoresLista.filter { it.ativo }.map { it.id }
                )
            }
        }
    }

    fun toggleJogadorSelecionado(jogadorId: Long, selecionado: Boolean) {
        if (selecionado && !jogadoresSelecionados.contains(jogadorId)) {
            jogadoresSelecionados.add(jogadorId)
        } else if (!selecionado && jogadoresSelecionados.contains(jogadorId)) {
            jogadoresSelecionados.remove(jogadorId)
        }
    }

    fun selecionarTodos() {
        viewModelScope.launch {
            jogadorRepository.getJogadores().first().let { jogadoresLista ->
                jogadoresSelecionados.clear()
                jogadoresSelecionados.addAll(jogadoresLista.map { it.id })
            }
        }
    }

    fun desmarcarTodos() {
        jogadoresSelecionados.clear()
    }

    fun verificarESortearTimes() {
        // Verifica se existe um sorteio não contabilizado
        if (sorteioRepository.temSorteioNaoContabilizado.value) {
            // Se existir, exibe o dialog de confirmação
            _mostrarDialogConfirmacao.value = true
        } else {
            // Se não existir, realiza o sorteio diretamente
            sortearTimes()
        }
    }

    fun confirmarNovoSorteio() {
        _mostrarDialogConfirmacao.value = false

        // Limpa o histórico de times atual antes de sortear novos times
        viewModelScope.launch {
            try {
                // Limpa os times no repositório
                sorteioRepository.limparHistoricoTimes()

                // Após limpar, inicia um novo sorteio
                sortearTimes()
            } catch (e: Exception) {
                // Em caso de erro, desativa o loading
                sorteioRepository.setSorteioEmAndamento(false)
            }
        }
    }

    fun cancelarNovoSorteio() {
        _mostrarDialogConfirmacao.value = false
    }

    fun sortearTimes() {
        // Marca o início do sorteio
        sorteioRepository.setSorteioEmAndamento(true)

        viewModelScope.launch {
            try {
                // Obter configurações
                val config = configuracaoRepository.getConfiguracao().first()

                // Obter jogadores
                val jogadoresLista = jogadorRepository.getJogadores().first()

                // Filtrar apenas os jogadores selecionados
                val jogadoresAtivos = jogadoresLista.filter { jogador ->
                    jogadoresSelecionados.contains(jogador.id)
                }

                // Simula um pequeno delay para feedback visual (pode ser removido em produção)
                delay(1500)

                // Realiza o sorteio
                val resultado = sorteioUseCase.sortearTimes(jogadoresAtivos, config)

                // Salva o resultado no repositório
                sorteioRepository.salvarResultadoSorteio(resultado)

                // Marca que o sorteio foi concluído com sucesso
                _sorteioRealizado.value = true

            } catch (e: Exception) {
                // Em caso de erro, desativa o loading
                sorteioRepository.setSorteioEmAndamento(false)
            }
        }
    }

    fun resetSorteioRealizado() {
        _sorteioRealizado.value = false
    }

    fun abrirConfiguracoes() {
        // Aqui poderia navegar para a tela de configurações
    }
}
