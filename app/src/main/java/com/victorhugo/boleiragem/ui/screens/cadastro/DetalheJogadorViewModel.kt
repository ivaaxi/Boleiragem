package com.victorhugo.boleiragem.ui.screens.cadastro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorhugo.boleiragem.data.model.Jogador
import com.victorhugo.boleiragem.data.model.PosicaoJogador
import com.victorhugo.boleiragem.data.repository.JogadorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetalheJogadorViewModel @Inject constructor(
    private val jogadorRepository: JogadorRepository
) : ViewModel() {

    private val _jogador = MutableStateFlow<Jogador?>(null)
    val jogador: StateFlow<Jogador?> = _jogador

    private val _salvo = MutableStateFlow(false)
    val salvo: StateFlow<Boolean> = _salvo

    // ID do grupo atual (pelada) - obrigatório para criar/editar jogadores
    private var grupoId: Long = 0

    // Estados para formulário
    var nome by mutableStateOf("")
        internal set

    var posicaoPrincipal by mutableStateOf(PosicaoJogador.MEIO_CAMPO)
        internal set

    var notaPrincipal by mutableStateOf(3)
        internal set

    var possuiSecundaria by mutableStateOf(false)
        internal set

    var posicaoSecundaria by mutableStateOf<PosicaoJogador?>(null)
        internal set

    var notaSecundaria by mutableStateOf<Int?>(null)
        internal set

    private var jogadorId: Long = 0

    /**
     * Define qual pelada/grupo o jogador pertence
     */
    fun setGrupoId(grupoId: Long) {
        this.grupoId = grupoId
    }

    fun resetarSalvo() {
        _salvo.value = false
    }

    fun carregarJogador(id: Long) {
        // Sempre resetamos o estado de salvo quando carregamos um jogador
        resetarSalvo()

        // Reset dos campos do formulário para evitar dados antigos
        resetarFormulario()

        if (id <= 0) {
            // É um novo jogador - precisa do grupoId válido
            if (grupoId <= 0) {
                throw IllegalStateException("Grupo ID não definido. Chame setGrupoId() primeiro.")
            }

            _jogador.value = Jogador(
                id = 0,
                grupoId = grupoId, // Incluindo grupoId para novos jogadores
                nome = "",
                posicaoPrincipal = PosicaoJogador.MEIO_CAMPO,
                posicaoSecundaria = null,
                notaPosicaoPrincipal = 3,
                notaPosicaoSecundaria = null
            )
            jogadorId = 0
            return
        }

        jogadorId = id
        viewModelScope.launch {
            val jogadorCarregado = jogadorRepository.getJogadorPorId(id)
            _jogador.value = jogadorCarregado

            jogadorCarregado?.let {
                nome = it.nome
                posicaoPrincipal = it.posicaoPrincipal
                notaPrincipal = it.notaPosicaoPrincipal
                possuiSecundaria = it.posicaoSecundaria != null
                posicaoSecundaria = it.posicaoSecundaria
                notaSecundaria = it.notaPosicaoSecundaria
                // Atualizar o grupoId com o do jogador carregado
                grupoId = it.grupoId
            }
        }
    }

    private fun resetarFormulario() {
        nome = ""
        posicaoPrincipal = PosicaoJogador.MEIO_CAMPO
        notaPrincipal = 3
        possuiSecundaria = false
        posicaoSecundaria = null
        notaSecundaria = null
    }

    fun salvarJogador() {
        // Valida os dados antes de salvar
        if (nome.isBlank()) {
            return  // Não salva se o nome estiver em branco
        }

        if (grupoId <= 0) {
            throw IllegalStateException("Grupo ID não definido. Chame setGrupoId() primeiro.")
        }

        // Se possuiSecundaria for false, garantimos que os valores secundários sejam null
        val posSecundaria = if (possuiSecundaria) posicaoSecundaria else null
        val notaSecund = if (possuiSecundaria && posicaoSecundaria != null) notaSecundaria else null

        val jogadorAtualizado = if (jogadorId > 0) {
            _jogador.value?.copy(
                nome = nome,
                posicaoPrincipal = posicaoPrincipal,
                posicaoSecundaria = posSecundaria,
                notaPosicaoPrincipal = notaPrincipal,
                notaPosicaoSecundaria = notaSecund
            )
        } else {
            Jogador(
                grupoId = grupoId, // Agora incluindo o grupoId obrigatório
                nome = nome,
                posicaoPrincipal = posicaoPrincipal,
                posicaoSecundaria = posSecundaria,
                notaPosicaoPrincipal = notaPrincipal,
                notaPosicaoSecundaria = notaSecund
            )
        }

        viewModelScope.launch {
            if (jogadorAtualizado != null) {
                if (jogadorId > 0) {
                    jogadorRepository.atualizarJogador(jogadorAtualizado)
                } else {
                    jogadorRepository.inserirJogador(jogadorAtualizado)
                }
                _salvo.value = true
            }
        }
    }
}
