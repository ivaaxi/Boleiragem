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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CadastroJogadoresViewModel @Inject constructor(
    private val repository: JogadorRepository
) : ViewModel() {

    // Critério atual de ordenação como StateFlow para garantir recomposição
    private val _criterioOrdenacao = MutableStateFlow(CriterioOrdenacao.ALFABETICO)
    val criterioOrdenacao: CriterioOrdenacao
        get() = _criterioOrdenacao.value

    // Jogadores ordenados conforme o critério atual
    val jogadores: Flow<List<Jogador>> = repository.getJogadores().combine(_criterioOrdenacao) { lista, criterio ->
        when (criterio) {
            CriterioOrdenacao.ALFABETICO -> lista.sortedBy { it.nome }
            CriterioOrdenacao.PONTUACAO -> lista.sortedByDescending { it.pontuacaoTotal }
            CriterioOrdenacao.JOGOS -> lista.sortedByDescending { it.totalJogos }
            CriterioOrdenacao.POSICAO -> lista.sortedBy { it.posicaoPrincipal.name }
            CriterioOrdenacao.OVERAL -> lista.sortedByDescending { it.notaPosicaoPrincipal }
        }
    }

    fun mudarCriterioOrdenacao(criterio: CriterioOrdenacao) {
        _criterioOrdenacao.value = criterio
    }

    fun inserirJogador(jogador: Jogador) {
        viewModelScope.launch {
            repository.inserirJogador(jogador)
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
