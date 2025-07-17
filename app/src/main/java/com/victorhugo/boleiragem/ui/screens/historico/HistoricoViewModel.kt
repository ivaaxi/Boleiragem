package com.victorhugo.boleiragem.ui.screens.historico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorhugo.boleiragem.data.model.HistoricoPelada
import com.victorhugo.boleiragem.data.repository.HistoricoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class OrdenacaoHistorico {
    MAIS_RECENTES,
    MAIS_ANTIGAS
}

@HiltViewModel
class HistoricoViewModel @Inject constructor(
    private val historicoRepository: HistoricoRepository
) : ViewModel() {

    private val _historicoPartidas = MutableStateFlow<List<HistoricoPelada>>(emptyList())
    val historicoPartidas: StateFlow<List<HistoricoPelada>> = _historicoPartidas.asStateFlow()

    // Estado para controlar a ordenação atual
    private val _ordenacaoAtual = MutableStateFlow(OrdenacaoHistorico.MAIS_RECENTES)
    val ordenacaoAtual: StateFlow<OrdenacaoHistorico> = _ordenacaoAtual.asStateFlow()

    // Lista original de partidas (sem ordenação)
    private var partidasOriginais = listOf<HistoricoPelada>()

    init {
        carregarHistoricoPartidas()
    }

    private fun carregarHistoricoPartidas() {
        viewModelScope.launch {
            historicoRepository.getHistoricoPartidas().collect { partidas ->
                partidasOriginais = partidas
                aplicarOrdenacao()
            }
        }
    }

    // Alterna entre os modos de ordenação
    fun alternarOrdenacao() {
        _ordenacaoAtual.value = if (_ordenacaoAtual.value == OrdenacaoHistorico.MAIS_RECENTES) {
            OrdenacaoHistorico.MAIS_ANTIGAS
        } else {
            OrdenacaoHistorico.MAIS_RECENTES
        }
        aplicarOrdenacao()
    }

    // Aplica a ordenação atual à lista de partidas
    private fun aplicarOrdenacao() {
        _historicoPartidas.value = when (_ordenacaoAtual.value) {
            OrdenacaoHistorico.MAIS_RECENTES -> {
                partidasOriginais.sortedByDescending { it.dataFinalizacao }
            }
            OrdenacaoHistorico.MAIS_ANTIGAS -> {
                partidasOriginais.sortedBy { it.dataFinalizacao }
            }
        }
    }

    // Apaga todo o histórico de partidas
    fun apagarHistorico() {
        viewModelScope.launch {
            // Implementar o método limparHistorico no repositório ou usar algum método existente
            // que limpe o histórico de partidas
            historicoRepository.getHistoricoPartidas().collect { partidas ->
                // Como alternativa, apenas atualizamos a lista local para vazia
                // até que o método seja implementado no repositório
                partidasOriginais = emptyList()
                _historicoPartidas.value = emptyList()
            }
        }
    }
}
