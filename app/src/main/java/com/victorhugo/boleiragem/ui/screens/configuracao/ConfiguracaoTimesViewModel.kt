package com.victorhugo.boleiragem.ui.screens.configuracao

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorhugo.boleiragem.data.model.ConfiguracaoSorteio
import com.victorhugo.boleiragem.data.model.CriterioSorteio
import com.victorhugo.boleiragem.data.repository.ConfiguracaoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfiguracaoTimesViewModel @Inject constructor(
    private val repository: ConfiguracaoRepository
) : ViewModel() {

    val configuracao = repository.getConfiguracao()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    var jogadoresPorTime by mutableIntStateOf(5)
        internal set

    var quantidadeTimes by mutableIntStateOf(2)
        internal set

    // Aleatório é agora um booleano separado
    var aleatorio by mutableStateOf(true)
        private set

    // Todos os outros critérios são extras combináveis
    var criteriosExtras by mutableStateOf<Set<CriterioSorteio>>(emptySet())
        private set

    init {
        viewModelScope.launch {
            repository.getConfiguracao().collect { config ->
                config?.let {
                    jogadoresPorTime = it.qtdJogadoresPorTime
                    quantidadeTimes = it.qtdTimes
                    aleatorio = it.aleatorio
                    criteriosExtras = it.criteriosExtras
                }
            }
        }
    }

    // Método para alternar o critério aleatório
    fun toggleAleatorio(ativo: Boolean) {
        aleatorio = ativo

        // Se aleatório for ativado, limpa todos os critérios extras
        if (ativo) {
            criteriosExtras = emptySet()
        }
        // Se aleatório for desativado, seleciona pelo menos um critério extra (Pontuação)
        else if (criteriosExtras.isEmpty()) {
            criteriosExtras = setOf(CriterioSorteio.PONTUACAO)
        }

        saveConfiguracao()
    }

    // Método para alternar critérios extras
    fun toggleCriterioExtra(criterio: CriterioSorteio) {
        // Se aleatório estiver ativo, não permite selecionar critérios extras
        if (aleatorio) return

        // Atualiza os critérios extras
        criteriosExtras = if (criteriosExtras.contains(criterio)) {
            // Se está removendo um critério, verifica se há pelo menos um selecionado
            val novosCriterios = criteriosExtras.minus(criterio)
            if (novosCriterios.isEmpty()) {
                // Mantém pelo menos um critério selecionado
                criteriosExtras
            } else {
                novosCriterios
            }
        } else {
            criteriosExtras.plus(criterio)
        }

        saveConfiguracao()
    }

    // Método para salvar a configuração atual
    private fun saveConfiguracao() {
        viewModelScope.launch {
            val novaConfig = ConfiguracaoSorteio(
                qtdJogadoresPorTime = jogadoresPorTime,
                qtdTimes = quantidadeTimes,
                aleatorio = aleatorio,
                criteriosExtras = criteriosExtras
            )
            repository.salvarConfiguracao(novaConfig)
        }
    }

    fun salvarConfiguracoes() {
        saveConfiguracao()
    }

    fun atualizarJogadoresPorTime(valor: Int) {
        jogadoresPorTime = valor
        saveConfiguracao()
    }

    fun atualizarQuantidadeTimes(valor: Int) {
        quantidadeTimes = valor
        saveConfiguracao()
    }
}
