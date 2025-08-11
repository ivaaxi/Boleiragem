package com.victorhugo.boleiragem.ui.screens.sorteio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorhugo.boleiragem.data.model.Time
import com.victorhugo.boleiragem.data.repository.JogadorRepository
import com.victorhugo.boleiragem.data.repository.SorteioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultadoSorteioViewModel @Inject constructor(
    private val sorteioRepository: SorteioRepository,
    private val jogadorRepository: JogadorRepository
) : ViewModel() {

    // Agora pegamos o resultado diretamente do repositório compartilhado
    val resultadoSorteio = sorteioRepository.resultadoSorteio

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

    // Estado para controlar o modo de visualização (normal ou sorteio rápido)
    private val _modoSorteioRapido = MutableStateFlow(false)
    val modoSorteioRapido: StateFlow<Boolean> = _modoSorteioRapido

    init {
        // Verificar se existe pelada em andamento
        viewModelScope.launch {
            sorteioRepository.temPeladaAtiva.collect { temPelada ->
                _peladaEmAndamento.value = temPelada
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

        // Verificar se todos os times têm capitães selecionados
        verificarTodosCapitaesDefinidos()
    }

    // Verificar se todos os times têm capitães definidos
    private fun verificarTodosCapitaesDefinidos() {
        val resultado = resultadoSorteio.value ?: return

        // Conta quantos times precisam de capitão (exclui o Time Reserva)
        val timesQueNecessitamCapitao = resultado.times.count { time -> time.nome != "Time Reserva" }

        // Conta quantos capitães já foram selecionados
        val capitaesSelecionados = _capitaesSelecionados.value.size

        // Todos os times estão definidos se todos que precisam de capitão têm um selecionado
        _todosCapitaesDefinidos.value = timesQueNecessitamCapitao == capitaesSelecionados
    }

    // Método para cancelar o sorteio atual
    fun cancelarSorteio() {
        viewModelScope.launch {
            // Se houver uma pelada ativa, cancelamos ela
            if (_peladaEmAndamento.value) {
                sorteioRepository.cancelarPeladaAtiva()
            }

            // Limpa o resultado do sorteio
            sorteioRepository.limparResultado()
            // Reseta o estado de sorteio em andamento para resolver o problema de loading infinito
            sorteioRepository.setSorteioEmAndamento(false)
            // Reseta o estado de sorteio não contabilizado
            sorteioRepository.resetSorteioContabilizacao()
            // Limpar os capitães selecionados
            _capitaesSelecionados.value = emptyMap()
        }
    }

    // Método para confirmar o sorteio
    fun confirmarSorteio() {
        // Salva o resultado atual no banco de dados com os nomes dos times atualizados com os capitães
        resultadoSorteio.value?.let { resultado ->
            // Criar novos times com nomes baseados nos capitães
            val timesAtualizados = resultado.times.map { time ->
                val capitaoId = _capitaesSelecionados.value[time.id.toLong()]
                val capitao = time.jogadores.find { it.id.toLong() == capitaoId }
                val novoNome = capitao?.let { "Time do ${it.nome.split(" ").first()}" } ?: time.nome

                // Criar cópia do time com o novo nome
                time.copy(nome = novoNome)
            }

            // Se houver uma pelada ativa, cancelamos ela antes de salvar a nova
            viewModelScope.launch {
                if (_peladaEmAndamento.value) {
                    sorteioRepository.cancelarPeladaAtiva()
                }

                // Salvar os times com os novos nomes
                sorteioRepository.salvarTimesNoBancoDeDados(resultado.copy(times = timesAtualizados))

                // Atualizar a disponibilidade dos jogadores
                atualizarDisponibilidadeJogadores(resultado.times)
            }
        }
        // Reseta o estado de sorteio em andamento
        sorteioRepository.setSorteioEmAndamento(false)
    }

    // Função para atualizar a disponibilidade dos jogadores com base no resultado do sorteio
    private fun atualizarDisponibilidadeJogadores(times: List<Time>) {
        viewModelScope.launch {
            // Coletamos todos os jogadores que participaram do sorteio
            val jogadoresSorteio = mutableSetOf<Long>()
            times.forEach { time ->
                time.jogadores.forEach { jogador ->
                    jogadoresSorteio.add(jogador.id)
                }
            }

            // Atualizamos o status de disponibilidade de todos os jogadores
            val todosJogadores = jogadorRepository.getJogadores().first()
            todosJogadores.forEach { jogador ->
                // Se o jogador está no sorteio, ele está disponível
                val disponivel = jogadoresSorteio.contains(jogador.id)

                // Só atualizamos se o status mudou, para evitar operações desnecessárias
                if (jogador.disponivel != disponivel) {
                    jogadorRepository.atualizarJogador(jogador.copy(disponivel = disponivel))
                }
            }
        }
    }

    fun compartilharResultado() {
        // Gera o texto e o disponibiliza para compartilhamento
        gerarTextoCompartilhamento()
    }

    // Limpa as seleções de capitães
    fun limparCapitaes() {
        _todosCapitaesDefinidos.value = false
    }

    // Função para gerar o texto formatado para compartilhamento
    fun gerarTextoCompartilhamento() {
        val resultado = resultadoSorteio.value ?: return
        val builder = StringBuilder()
        resultado.times.forEach { time ->
            builder.appendLine(time.nome)
            time.jogadores.forEachIndexed { idx, jogador ->
                builder.appendLine("${idx + 1}. ${jogador.nome}")
            }
            builder.appendLine()
        }
        _textoCompartilhamento.value = builder.toString().trim()
    }

    // Função para limpar o texto de compartilhamento após compartilhar
    fun limparTextoCompartilhamento() {
        _textoCompartilhamento.value = ""
    }

    // Método para definir o modo de sorteio rápido
    fun definirModoSorteioRapido(modoRapido: Boolean) {
        _modoSorteioRapido.value = modoRapido
    }
}
