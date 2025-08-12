package com.victorhugo.boleiragem.ui.screens.sorteio

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorhugo.boleiragem.data.model.ResultadoSorteio
import com.victorhugo.boleiragem.data.model.Time
import com.victorhugo.boleiragem.data.repository.JogadorRepository
import com.victorhugo.boleiragem.data.repository.SorteioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultadoSorteioViewModel @Inject constructor(
    private val sorteioRepository: SorteioRepository,
    private val jogadorRepository: JogadorRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Estado para controlar o modo de visualização (normal ou sorteio rápido)
    // Inicializado a partir do SavedStateHandle
    private val _modoSorteioRapido = MutableStateFlow(savedStateHandle.get<Boolean>("isSorteioRapido") ?: false)
    val modoSorteioRapido: StateFlow<Boolean> = _modoSorteioRapido

    // resultadoSorteio agora coleta do fluxo apropriado baseado em _modoSorteioRapido
    val resultadoSorteio: StateFlow<ResultadoSorteio?> = _modoSorteioRapido.flatMapLatest { isRapido ->
        if (isRapido) {
            sorteioRepository.resultadoSorteioRapido
        } else {
            sorteioRepository.resultadoSorteio
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = if (_modoSorteioRapido.value) sorteioRepository.resultadoSorteioRapido.value else sorteioRepository.resultadoSorteio.value
    )

    // Mapa para armazenar o capitão de cada time (ID do time -> ID do jogador capitão)
    private val _capitaesSelecionados = MutableStateFlow<Map<Long, Long>>(emptyMap())
    val capitaesSelecionados: StateFlow<Map<Long, Long>> = _capitaesSelecionados

    // Verifica se todos os times têm capitães selecionados
    private val _todosCapitaesDefinidos = MutableStateFlow(false)
    val todosCapitaesDefinidos: StateFlow<Boolean> = _todosCapitaesDefinidos

    // Verificar se existe uma pelada em andamento
    private val _peladaEmAndamento = MutableStateFlow(false)
    val peladaEmAndamento: StateFlow<Boolean> = _peladaEmAndamento

    // Evento para compartilhar o texto dos times
    private val _textoCompartilhamento = MutableStateFlow("")
    val textoCompartilhamento: StateFlow<String> = _textoCompartilhamento

    init {
        // Verificar se existe pelada em andamento apenas se não for sorteio rápido
        if (!_modoSorteioRapido.value) {
            viewModelScope.launch {
                sorteioRepository.temPeladaAtiva.collect { temPelada ->
                    _peladaEmAndamento.value = temPelada
                }
            }
        }
    }

    // Método para selecionar um jogador como capitão de um time
    fun selecionarCapitao(timeId: Long, jogadorId: Long) {
        _capitaesSelecionados.update { capitaes ->
            val novoMapa = capitaes.toMutableMap()
            novoMapa[timeId] = jogadorId
            novoMapa
        }
        verificarTodosCapitaesDefinidos()
    }

    private fun verificarTodosCapitaesDefinidos() {
        val resultado = resultadoSorteio.value ?: return
        val timesQueNecessitamCapitao = resultado.times.count { time -> time.nome != "Time Reserva" && !time.ehTimeReserva }
        val capitaesSelecionadosCount = _capitaesSelecionados.value.size
        _todosCapitaesDefinidos.value = timesQueNecessitamCapitao == capitaesSelecionadosCount && timesQueNecessitamCapitao > 0
    }

    // Método para cancelar o sorteio atual
    fun cancelarSorteio() {
        viewModelScope.launch {
            if (_modoSorteioRapido.value) {
                sorteioRepository.limparResultadoSorteioRapido()
            } else {
                if (_peladaEmAndamento.value) {
                    sorteioRepository.cancelarPeladaAtiva()
                }
                sorteioRepository.limparResultado()
                sorteioRepository.setSorteioEmAndamento(false)
                sorteioRepository.resetSorteioContabilizacao()
            }
            _capitaesSelecionados.value = emptyMap()
            _todosCapitaesDefinidos.value = false
        }
    }

    // Método para confirmar o sorteio
    fun confirmarSorteio() {
        if (!_modoSorteioRapido.value) {
            resultadoSorteio.value?.let { resultado ->
                val timesAtualizados = resultado.times.map { time ->
                    val capitaoId = _capitaesSelecionados.value[time.id.toLong()]
                    val capitao = time.jogadores.find { it.id.toLong() == capitaoId }
                    val novoNome = capitao?.let { "Time do ${it.nome.split(" ").first()}" } ?: time.nome
                    time.copy(nome = novoNome)
                }

                viewModelScope.launch {
                    if (_peladaEmAndamento.value) {
                        sorteioRepository.cancelarPeladaAtiva()
                    }
                    sorteioRepository.salvarTimesNoBancoDeDados(resultado.copy(times = timesAtualizados))
                    atualizarDisponibilidadeJogadores(resultado.times)
                }
            }
            sorteioRepository.setSorteioEmAndamento(false)
        } else {
            // Em modo sorteio rápido, confirmar não persiste dados, apenas permite a tela fechar.
            // A limpeza do resultado rápido é feita no onCleared ou no cancelarSorteio.
        }
    }

    private fun atualizarDisponibilidadeJogadores(times: List<Time>) {
        viewModelScope.launch {
            val jogadoresSorteio = mutableSetOf<Long>()
            times.forEach { time ->
                time.jogadores.forEach { jogador ->
                    jogadoresSorteio.add(jogador.id)
                }
            }
            val todosJogadores = jogadorRepository.getJogadores().first()
            todosJogadores.forEach { jogador ->
                val disponivel = jogadoresSorteio.contains(jogador.id)
                if (jogador.disponivel != disponivel) {
                    jogadorRepository.atualizarJogador(jogador.copy(disponivel = disponivel))
                }
            }
        }
    }

    fun gerarTextoCompartilhamento() {
        val resultado = resultadoSorteio.value ?: return
        val builder = StringBuilder()
        resultado.times.forEach { time ->
            // Removido o prefixo "Team:" e o \n inicial desnecessário se for o primeiro time
            if (builder.isNotEmpty()) { // Adiciona linha em branco antes do próximo time, exceto para o primeiro
                builder.appendLine()
            }
            builder.appendLine(time.nome) 
            time.jogadores.forEachIndexed { idx, jogador ->
                builder.appendLine("${idx + 1}. ${jogador.nome}")
            }
            // Não precisa de builder.appendLine() extra aqui se já tem no início do próximo time
        }
        _textoCompartilhamento.value = builder.toString().trim()
    }

    fun limparTextoCompartilhamento() {
        _textoCompartilhamento.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        if (_modoSorteioRapido.value) {
            sorteioRepository.limparResultadoSorteioRapido()
        }
    }
}
