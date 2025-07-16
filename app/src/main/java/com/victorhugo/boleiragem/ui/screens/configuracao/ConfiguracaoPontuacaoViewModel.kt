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

    // Inicializa com um objeto padrão para evitar NullPointerException
    private val _configuracaoPontuacao = MutableStateFlow(ConfiguracaoPontuacao(id = 1, pontosPorVitoria = 10, pontosPorDerrota = -10, pontosPorEmpate = -5))
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
                // Só atualiza se a configuração não for nula
                if (configuracao != null) {
                    _configuracaoPontuacao.value = configuracao
                }
            }
        }
    }

    fun atualizarPontosPorVitoria(pontos: Int) {
        // Adiciona verificação de nulidade para evitar NullPointerException
        val configAtual = _configuracaoPontuacao.value ?: ConfiguracaoPontuacao()
        _configuracaoPontuacao.value = configAtual.copy(pontosPorVitoria = pontos)
    }

    fun atualizarPontosPorDerrota(pontos: Int) {
        // Adiciona verificação de nulidade para evitar NullPointerException
        val configAtual = _configuracaoPontuacao.value ?: ConfiguracaoPontuacao()
        _configuracaoPontuacao.value = configAtual.copy(pontosPorDerrota = pontos)
    }

    fun atualizarPontosPorEmpate(pontos: Int) {
        // Adiciona verificação de nulidade para evitar NullPointerException
        val configAtual = _configuracaoPontuacao.value ?: ConfiguracaoPontuacao()
        _configuracaoPontuacao.value = configAtual.copy(pontosPorEmpate = pontos)
    }

    fun salvarConfiguracao() {
        viewModelScope.launch {
            _salvandoConfiguracao.value = true

            try {
                // Verifica se a configuração não é nula antes de salvar
                val configParaSalvar = _configuracaoPontuacao.value ?: ConfiguracaoPontuacao()
                pontuacaoRepository.atualizarConfiguracaoPontuacao(configParaSalvar)
                _configuracaoSalva.value = true

                // Reset do estado após 2 segundos
                kotlinx.coroutines.delay(2000)
                _configuracaoSalva.value = false

            } catch (e: Exception) {
                // Tratamento de erros
                e.printStackTrace()
            } finally {
                _salvandoConfiguracao.value = false
            }
        }
    }
}
