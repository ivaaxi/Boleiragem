package com.victorhugo.boleiragem.ui.screens.configuracao

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorhugo.boleiragem.data.model.ConfiguracaoSorteio
import com.victorhugo.boleiragem.data.repository.ConfiguracaoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GerenciadorPerfisViewModel @Inject constructor(
    private val configuracaoRepository: ConfiguracaoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Obtém o ID do grupo da navegação
    private val grupoId: Long = savedStateHandle.get<Long>("grupoId") ?: 0L

    // Método para definir o ID do grupo atual
    fun setGrupoId(id: Long) {
        configuracaoRepository.setGrupoId(id)
        // Recarrega os perfis com o novo ID de grupo
        _perfis.value = emptyList()
    }

    // Lista de todos os perfis para o grupo atual
    private val _perfis = MutableStateFlow<List<ConfiguracaoSorteio>>(emptyList())
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
            aleatorio = true,
            grupoId = grupoId
        )
    }

    // Edita um perfil existente
    fun editarPerfil(perfil: ConfiguracaoSorteio) {
        _perfilEmEdicao.value = perfil
    }

    // Confirma a criação/edição de um perfil
    fun salvarPerfil(perfil: ConfiguracaoSorteio) {
        // Garantir que o grupoId está sempre definido
        val perfilAtualizado = perfil.copy(grupoId = grupoId)

        viewModelScope.launch {
            configuracaoRepository.salvarConfiguracao(perfilAtualizado)
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
            // Verificar se há pelo menos dois perfis antes de excluir
            val perfisAtuais = configuracaoRepository.getTodasConfiguracoes().firstOrNull() ?: emptyList()
            if (perfisAtuais.size > 1) {
                // Se o perfil a ser excluído é o padrão, temos que definir outro como padrão primeiro
                if (perfil.isPadrao) {
                    // Encontrar outro perfil para definir como padrão
                    val outroPerfil = perfisAtuais.firstOrNull { it.id != perfil.id }
                    if (outroPerfil != null) {
                        configuracaoRepository.definirConfiguracaoPadrao(outroPerfil.id)
                    }
                }

                // Agora podemos excluir com segurança
                configuracaoRepository.deletarConfiguracao(perfil.id)
            }

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
