package com.victorhugo.boleiragem.ui.screens.cadastro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorhugo.boleiragem.data.model.Jogador
import com.victorhugo.boleiragem.data.repository.JogadorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CriterioOrdenacao {
    ALFABETICO, PONTUACAO, JOGOS, POSICAO
}

@HiltViewModel
class CadastroJogadoresViewModel @Inject constructor(
    private val repository: JogadorRepository
) : ViewModel() {

    var criterioOrdenacao by mutableStateOf(CriterioOrdenacao.ALFABETICO)
        private set

    // Jogadores ordenados conforme o critério atual
    val jogadores: Flow<List<Jogador>> = repository.getJogadores().map { lista ->
        when (criterioOrdenacao) {
            CriterioOrdenacao.ALFABETICO -> lista.sortedBy { it.nome }
            CriterioOrdenacao.PONTUACAO -> lista.sortedByDescending { it.pontuacaoTotal }
            CriterioOrdenacao.JOGOS -> lista.sortedByDescending { it.totalJogos }
            CriterioOrdenacao.POSICAO -> lista.sortedWith(
                compareBy<Jogador> { it.posicaoPrincipal.name }
                    .thenByDescending { it.notaPosicaoPrincipal }
            )
        }
    }

    fun mudarCriterioOrdenacao(criterio: CriterioOrdenacao) {
        criterioOrdenacao = criterio
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
