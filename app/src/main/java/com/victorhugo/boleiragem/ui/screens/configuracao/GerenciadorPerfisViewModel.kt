package com.victorhugo.boleiragem.ui.screens.configuracao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorhugo.boleiragem.data.model.ConfiguracaoSorteio
import com.victorhugo.boleiragem.data.repository.ConfiguracaoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GerenciadorPerfisViewModel @Inject constructor(
    private val configuracaoRepository: ConfiguracaoRepository
) : ViewModel() {

    // Lista de todos os perfis
    val perfis = configuracaoRepository.getTodasConfiguracoes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Estado para o diálogo de edição/criação
    private val _perfilEmEdicao = MutableStateFlow<ConfiguracaoSorteio?>(null)
    val perfilEmEdicao: StateFlow<ConfiguracaoSorteio?> = _perfilEmEdicao.asStateFlow()

    // Estado para controlar a exibição do diálogo de confirmação de exclusão
    private val _perfilParaExcluir = MutableStateFlow<ConfiguracaoSorteio?>(null)
    val perfilParaExcluir: StateFlow<ConfiguracaoSorteio?> = _perfilParaExcluir.asStateFlow()

    // Cria um novo perfil vazio para edição
    fun criarNovoPerfil() {
        _perfilEmEdicao.value = ConfiguracaoSorteio(
            nome = "Novo Perfil",
            qtdJogadoresPorTime = 5,
            qtdTimes = 2,
            aleatorio = true
        )
    }

    // Edita um perfil existente
    fun editarPerfil(perfil: ConfiguracaoSorteio) {
        _perfilEmEdicao.value = perfil
    }

    // Confirma a criação/edição de um perfil
    fun salvarPerfil(perfil: ConfiguracaoSorteio) {
        viewModelScope.launch {
            configuracaoRepository.salvarConfiguracao(perfil)
            // Fecha o diálogo de edição
            _perfilEmEdicao.value = null
        }
    }

    // Cancela a edição de um perfil
    fun cancelarEdicao() {
        _perfilEmEdicao.value = null
    }

    // Solicita confirmação para excluir um perfil
    fun confirmarExclusao(perfil: ConfiguracaoSorteio) {
        _perfilParaExcluir.value = perfil
    }

    // Exclui um perfil após confirmação
    fun excluirPerfil() {
        val perfil = _perfilParaExcluir.value ?: return

        viewModelScope.launch {
            configuracaoRepository.deletarConfiguracao(perfil.id)
            // Fecha o diálogo de confirmação
            _perfilParaExcluir.value = null
        }
    }

    // Cancela a exclusão de um perfil
    fun cancelarExclusao() {
        _perfilParaExcluir.value = null
    }

    // Define um perfil como padrão
    fun definirComoPadrao(perfilId: Long) {
        viewModelScope.launch {
            configuracaoRepository.definirConfiguracaoPadrao(perfilId)
        }
    }
}
