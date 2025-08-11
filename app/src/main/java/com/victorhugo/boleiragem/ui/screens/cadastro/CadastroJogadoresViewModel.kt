package com.victorhugo.boleiragem.ui.screens.cadastro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorhugo.boleiragem.data.model.CriterioOrdenacao
import com.victorhugo.boleiragem.data.model.Jogador
import com.victorhugo.boleiragem.data.repository.JogadorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CadastroJogadoresViewModel @Inject constructor(
    private val repository: JogadorRepository
) : ViewModel() {

    // ID do grupo atual (pelada) para filtrar jogadores
    private val _grupoId = MutableStateFlow<Long?>(null)
    private val grupoId: Long? get() = _grupoId.value

    // Critério atual de ordenação como StateFlow para garantir recomposição
    private val _criterioOrdenacao = MutableStateFlow(CriterioOrdenacao.ALFABETICO)
    val criterioOrdenacao: CriterioOrdenacao
        get() = _criterioOrdenacao.value

    // Jogadores ordenados conforme o critério atual, filtrados por pelada
    val jogadores: Flow<List<Jogador>> = _grupoId.flatMapLatest { grupoId ->
        if (grupoId != null) {
            repository.getJogadoresPorGrupo(grupoId).combine(_criterioOrdenacao) { lista, criterio ->
                when (criterio) {
                    CriterioOrdenacao.ALFABETICO -> lista.sortedBy { it.nome }
                    CriterioOrdenacao.PONTUACAO -> lista.sortedByDescending { it.pontuacaoTotal }
                    CriterioOrdenacao.JOGOS -> lista.sortedByDescending { it.totalJogos }
                    CriterioOrdenacao.POSICAO -> lista.sortedBy { it.posicaoPrincipal.name }
                    CriterioOrdenacao.OVERAL -> lista.sortedByDescending { it.notaPosicaoPrincipal }
                }
            }
        } else {
            flowOf(emptyList())
        }
    }

    /**
     * Define qual pelada/grupo está sendo gerenciado
     */
    fun setGrupoId(grupoId: Long) {
        _grupoId.value = grupoId
    }

    fun mudarCriterioOrdenacao(criterio: CriterioOrdenacao) {
        _criterioOrdenacao.value = criterio
    }

    fun inserirJogador(jogador: Jogador) {
        viewModelScope.launch {
            // Garantir que o jogador está vinculado ao grupo correto
            val jogadorComGrupo = if (grupoId != null) {
                jogador.copy(grupoId = grupoId!!)
            } else {
                throw IllegalStateException("Grupo ID não definido. Chame setGrupoId() primeiro.")
            }
            repository.inserirJogador(jogadorComGrupo)
        }
    }

    fun atualizarJogador(jogador: Jogador) {
        viewModelScope.launch {
            repository.atualizarJogador(jogador)
        }
    }

    fun deletarJogador(jogador: Jogador) {
        viewModelScope.launch {
            repository.deletarJogador(jogador)
        }
    }
}
