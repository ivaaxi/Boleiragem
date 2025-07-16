package com.victorhugo.boleiragem.ui.screens.sorteio

import androidx.lifecycle.ViewModel
import com.victorhugo.boleiragem.data.repository.SorteioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ResultadoSorteioViewModel @Inject constructor(
    private val sorteioRepository: SorteioRepository
) : ViewModel() {

    // Agora pegamos o resultado diretamente do repositório compartilhado
    val resultadoSorteio = sorteioRepository.resultadoSorteio

    // Método para cancelar o sorteio atual
    fun cancelarSorteio() {
        // Limpa o resultado do sorteio
        sorteioRepository.limparResultado()
        // Reseta o estado de sorteio em andamento para resolver o problema de loading infinito
        sorteioRepository.setSorteioEmAndamento(false)
        // Reseta o estado de sorteio não contabilizado
        sorteioRepository.resetSorteioContabilizacao()
    }

    // Método para confirmar o sorteio
    fun confirmarSorteio() {
        // Salva o resultado atual no banco de dados
        resultadoSorteio.value?.let { resultado ->
            sorteioRepository.salvarTimesNoBancoDeDados(resultado)
        }
        // Reseta o estado de sorteio em andamento para resolver o problema de loading infinito
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
