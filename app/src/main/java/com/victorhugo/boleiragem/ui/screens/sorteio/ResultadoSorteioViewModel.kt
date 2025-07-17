package com.victorhugo.boleiragem.ui.screens.sorteio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorhugo.boleiragem.data.model.Jogador
import com.victorhugo.boleiragem.data.model.Time
import com.victorhugo.boleiragem.data.repository.SorteioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultadoSorteioViewModel @Inject constructor(
    private val sorteioRepository: SorteioRepository
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

    init {
        // Verificar se existe pelada em andamento
        viewModelScope.launch {
            sorteioRepository.getTimesUltimaPelada().collect { times ->
                _peladaEmAndamento.value = times.isNotEmpty()
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
        _todosCapitaesDefinidos.value = resultado.times.size == _capitaesSelecionados.value.size
    }

    // Método para cancelar o sorteio atual
    fun cancelarSorteio() {
        // Limpa o resultado do sorteio
        sorteioRepository.limparResultado()
        // Reseta o estado de sorteio em andamento para resolver o problema de loading infinito
        sorteioRepository.setSorteioEmAndamento(false)
        // Reseta o estado de sorteio não contabilizado
        sorteioRepository.resetSorteioContabilizacao()
        // Limpar os capitães selecionados
        _capitaesSelecionados.value = emptyMap()
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

            // Salvar os times com os novos nomes
            sorteioRepository.salvarTimesNoBancoDeDados(resultado.copy(times = timesAtualizados))
        }
        // Reseta o estado de sorteio em andamento
        sorteioRepository.setSorteioEmAndamento(false)
    }

    fun compartilharResultado() {
        // Esta função seria implementada para compartilhar o resultado do sorteio
        // Via Intent para outras aplicações (WhatsApp, etc.)

        // Exemplo de implementação (não funcional neste momento):
        // val textoCompartilhamento = gerarTextoCompartilhamento()
        // val sendIntent = Intent().apply {
        //     action = Intent.ACTION_SEND
        //     putExtra(Intent.EXTRA_TEXT, textoCompartilhamento)
        //     type = "text/plain"
        // }
        // val shareIntent = Intent.createChooser(sendIntent, "Compartilhar times")
        // startActivity(context, shareIntent, null)
    }

    // Limpa as seleções de capitães
    fun limparSelecaoCapitaes() {
        _capitaesSelecionados.value = emptyMap()
        _todosCapitaesDefinidos.value = false
    }

    private fun gerarTextoCompartilhamento(): String {
        val sb = StringBuilder()
        sb.appendLine("⚽ TIMES SORTEADOS - BOLEIRAGEM ⚽")
        sb.appendLine()

        resultadoSorteio.value?.times?.forEach { time ->
            sb.appendLine("🏆 ${time.nome.uppercase()}")
            time.jogadores.forEach { jogador ->
                sb.appendLine("- ${jogador.nome} (${jogador.posicaoPrincipal.name})")
            }
            sb.appendLine()
        }

        sb.appendLine("Sorteado pelo app Boleiragem 📱")
        return sb.toString()
    }
}
