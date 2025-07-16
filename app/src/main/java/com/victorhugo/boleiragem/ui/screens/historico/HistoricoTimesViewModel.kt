package com.victorhugo.boleiragem.ui.screens.historico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorhugo.boleiragem.data.model.HistoricoTime
import com.victorhugo.boleiragem.data.repository.PontuacaoRepository
import com.victorhugo.boleiragem.data.repository.SorteioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoricoTimesViewModel @Inject constructor(
    private val sorteioRepository: SorteioRepository,
    private val pontuacaoRepository: PontuacaoRepository
) : ViewModel() {

    private val _historicoTimes = MutableStateFlow<List<HistoricoTime>>(emptyList())
    val historicoTimes: StateFlow<List<HistoricoTime>> = _historicoTimes.asStateFlow()

    private val _peladaFinalizada = MutableStateFlow(false)
    val peladaFinalizada: StateFlow<Boolean> = _peladaFinalizada.asStateFlow()

    init {
        carregarHistoricoTimes()
    }

    private fun carregarHistoricoTimes() {
        viewModelScope.launch {
            // Obter apenas os times da última pelada
            sorteioRepository.getTimesUltimaPelada().collect { historicoTimes ->
                _historicoTimes.value = historicoTimes
            }
        }
    }

    fun adicionarVitoria(time: HistoricoTime) {
        viewModelScope.launch {
            val timeAtualizado = time.copy(vitorias = time.vitorias + 1)
            sorteioRepository.atualizarHistoricoTime(timeAtualizado)
        }
    }

    fun adicionarDerrota(time: HistoricoTime) {
        viewModelScope.launch {
            val timeAtualizado = time.copy(derrotas = time.derrotas + 1)
            sorteioRepository.atualizarHistoricoTime(timeAtualizado)
        }
    }

    fun adicionarEmpate(time: HistoricoTime) {
        viewModelScope.launch {
            val timeAtualizado = time.copy(empates = time.empates + 1)
            sorteioRepository.atualizarHistoricoTime(timeAtualizado)
        }
    }

    fun diminuirVitoria(time: HistoricoTime) {
        if (time.vitorias > 0) {
            viewModelScope.launch {
                val timeAtualizado = time.copy(vitorias = time.vitorias - 1)
                sorteioRepository.atualizarHistoricoTime(timeAtualizado)
            }
        }
    }

    fun diminuirDerrota(time: HistoricoTime) {
        if (time.derrotas > 0) {
            viewModelScope.launch {
                val timeAtualizado = time.copy(derrotas = time.derrotas - 1)
                sorteioRepository.atualizarHistoricoTime(timeAtualizado)
            }
        }
    }

    fun diminuirEmpate(time: HistoricoTime) {
        if (time.empates > 0) {
            viewModelScope.launch {
                val timeAtualizado = time.copy(empates = time.empates - 1)
                sorteioRepository.atualizarHistoricoTime(timeAtualizado)
            }
        }
    }

    fun finalizarPelada() {
        viewModelScope.launch {
            try {
                // Finaliza a partida e atualiza as estatísticas dos jogadores
                pontuacaoRepository.finalizarPartida(_historicoTimes.value)

                // Resetar o estado de sorteio não contabilizado
                sorteioRepository.resetSorteioContabilizacao()

                // Indica que a pelada foi finalizada com sucesso
                _peladaFinalizada.value = true

                // Após 3 segundos, resetar a mensagem de sucesso
                kotlinx.coroutines.delay(3000)
                _peladaFinalizada.value = false
            } catch (e: Exception) {
                // Em caso de erro, poderia atualizar um estado para mostrar uma mensagem de erro
            }
        }
    }

    fun apagarPeladaAtual() {
        viewModelScope.launch {
            try {
                // Limpar os times atuais no repositório
                sorteioRepository.limparHistoricoTimes()

                // Resetar estado do sorteio
                sorteioRepository.resetSorteioContabilizacao()

                // Indica que a operação foi concluída com sucesso
                _peladaFinalizada.value = true

                // Após 3 segundos, resetar a mensagem de sucesso
                kotlinx.coroutines.delay(3000)
                _peladaFinalizada.value = false
            } catch (e: Exception) {
                // Em caso de erro, poderia atualizar um estado para mostrar uma mensagem de erro
            }
        }
    }
}
