package com.victorhugo.boleiragem.ui.screens.configuracao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorhugo.boleiragem.data.model.ConfiguracaoPontuacao
import com.victorhugo.boleiragem.data.repository.PontuacaoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfiguracaoPontuacaoViewModel @Inject constructor(
    private val pontuacaoRepository: PontuacaoRepository
) : ViewModel() {

    private val _configuracaoPontuacao = MutableStateFlow(ConfiguracaoPontuacao())
    val configuracaoPontuacao: StateFlow<ConfiguracaoPontuacao> = _configuracaoPontuacao.asStateFlow()

    private val _salvandoConfiguracao = MutableStateFlow(false)
    val salvandoConfiguracao: StateFlow<Boolean> = _salvandoConfiguracao.asStateFlow()

    private val _configuracaoSalva = MutableStateFlow(false)
    val configuracaoSalva: StateFlow<Boolean> = _configuracaoSalva.asStateFlow()

    init {
        carregarConfiguracao()
    }

    private fun carregarConfiguracao() {
        viewModelScope.launch {
            pontuacaoRepository.getConfiguracaoPontuacao().collect { configuracao ->
                _configuracaoPontuacao.value = configuracao
            }
        }
    }

    fun atualizarPontosPorVitoria(pontos: Int) {
        _configuracaoPontuacao.value = _configuracaoPontuacao.value.copy(pontosPorVitoria = pontos)
    }

    fun atualizarPontosPorDerrota(pontos: Int) {
        _configuracaoPontuacao.value = _configuracaoPontuacao.value.copy(pontosPorDerrota = pontos)
    }

    fun atualizarPontosPorEmpate(pontos: Int) {
        _configuracaoPontuacao.value = _configuracaoPontuacao.value.copy(pontosPorEmpate = pontos)
    }

    fun salvarConfiguracao() {
        viewModelScope.launch {
            _salvandoConfiguracao.value = true

            try {
                pontuacaoRepository.atualizarConfiguracaoPontuacao(_configuracaoPontuacao.value)
                _configuracaoSalva.value = true

                // Reset do estado após 2 segundos
                kotlinx.coroutines.delay(2000)
                _configuracaoSalva.value = false

            } catch (e: Exception) {
                // Tratamento de erros
            } finally {
                _salvandoConfiguracao.value = false
            }
        }
    }
}
