package com.victorhugo.boleiragem.ui.screens.estatisticas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorhugo.boleiragem.data.model.Jogador
import com.victorhugo.boleiragem.data.repository.JogadorRepository
import com.victorhugo.boleiragem.data.repository.HistoricoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EstatisticasViewModel @Inject constructor(
    private val jogadorRepository: JogadorRepository
) : ViewModel() {

    // Estado para armazenar o ID do grupo atual
    private val _grupoId = MutableStateFlow(-1L)

    // Jogador selecionado para visualizar estatísticas detalhadas
    private val _jogadorSelecionado = MutableStateFlow<Jogador?>(null)
    val jogadorSelecionado: StateFlow<Jogador?> = _jogadorSelecionado

    // Estatísticas calculadas
    private val _estatisticasCarregando = MutableStateFlow(false)
    val estatisticasCarregando: StateFlow<Boolean> = _estatisticasCarregando

    // Taxa de vitória (%)
    val taxaVitoria = jogadorSelecionado.map { jogador ->
        jogador?.let {
            if (it.totalJogos > 0) (it.vitorias.toFloat() / it.totalJogos) * 100 else 0f
        } ?: 0f
    }

    // Taxa de empate (%)
    val taxaEmpate = jogadorSelecionado.map { jogador ->
        jogador?.let {
            if (it.totalJogos > 0) (it.empates.toFloat() / it.totalJogos) * 100 else 0f
        } ?: 0f
    }

    // Taxa de derrota (%)
    val taxaDerrota = jogadorSelecionado.map { jogador ->
        jogador?.let {
            if (it.totalJogos > 0) (it.derrotas.toFloat() / it.totalJogos) * 100 else 0f
        } ?: 0f
    }

    // Média de pontuação por partida
    val mediaPontuacao = jogadorSelecionado.map { jogador ->
        jogador?.let {
            if (it.totalJogos > 0) it.pontuacaoTotal.toFloat() / it.totalJogos else 0f
        } ?: 0f
    }

    // Jogadores filtrados pelo grupo atual
    val jogadores = _grupoId.map { grupoId ->
        if (grupoId > 0) {
            jogadorRepository.getJogadoresPorGrupo(grupoId)
        } else {
            // Fallback para compatibilidade, mas não deve ser usado
            MutableStateFlow(emptyList())
        }
    }.flatMapLatest { it }

    // Ranking filtrado pelo grupo atual
    val ranking = _grupoId.map { grupoId ->
        if (grupoId > 0) {
            jogadorRepository.getJogadoresPorGrupo(grupoId)
        } else {
            // Fallback para compatibilidade, mas não deve ser usado
            MutableStateFlow(emptyList())
        }
    }.flatMapLatest { flow ->
        flow.map { jogadores ->
            // Ordena jogadores por pontuação total (decrescente)
            jogadores.sortedByDescending { it.pontuacaoTotal }
        }
    }

    fun selecionarJogador(jogador: Jogador) {
        _jogadorSelecionado.value = jogador
    }

    // Método para definir o ID do grupo atual
    fun setGrupoId(id: Long) {
        _grupoId.value = id
    }

    // Calcular estatísticas adicionais do jogador
    fun calcularEstatisticasAvancadas() {
        viewModelScope.launch {
            _estatisticasCarregando.value = true

            // Aqui você pode implementar cálculos mais complexos
            // como sequência de vitórias, desempenho com diferentes times, etc.

            _estatisticasCarregando.value = false
        }
    }
}
