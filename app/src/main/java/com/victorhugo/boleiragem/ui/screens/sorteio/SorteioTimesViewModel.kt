package com.victorhugo.boleiragem.ui.screens.sorteio

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorhugo.boleiragem.data.model.ConfiguracaoSorteio
import com.victorhugo.boleiragem.data.repository.ConfiguracaoRepository
import com.victorhugo.boleiragem.data.repository.JogadorRepository
import com.victorhugo.boleiragem.data.repository.SorteioRepository
import com.victorhugo.boleiragem.domain.SorteioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SorteioTimesViewModel @Inject constructor(
    private val jogadorRepository: JogadorRepository,
    private val configuracaoRepository: ConfiguracaoRepository,
    private val sorteioRepository: SorteioRepository,
    private val sorteioUseCase: SorteioUseCase
) : ViewModel() {

    val jogadores = jogadorRepository.getJogadores()
    val configuracao = configuracaoRepository.getConfiguracao()
    val resultadoSorteio = sorteioRepository.resultadoSorteio
    val loading = sorteioRepository.sorteioEmAndamento
    val temSorteioNaoContabilizado = sorteioRepository.temSorteioNaoContabilizado
    val temPeladaAtiva = sorteioRepository.temPeladaAtiva

    // Lista de configurações disponíveis para simulação
    private val _configuracoesDisponiveis = mutableStateListOf<ConfiguracaoSorteio>()
    val configuracoesDisponiveis: List<ConfiguracaoSorteio> = _configuracoesDisponiveis

    private val _sorteioRealizado = MutableStateFlow(false)
    val sorteioRealizado: StateFlow<Boolean> = _sorteioRealizado

    private val _mostrarDialogConfirmacao = MutableStateFlow(false)
    val mostrarDialogConfirmacao: StateFlow<Boolean> = _mostrarDialogConfirmacao

    // StateFlow para controlar se o botão de sorteio deve estar habilitado ou não
    private val _botaoSorteioHabilitado = MutableStateFlow(true)
    val botaoSorteioHabilitado: StateFlow<Boolean> = _botaoSorteioHabilitado

    val jogadoresSelecionados = mutableStateListOf<Long>()

    // Lista de jogadores que participaram do último sorteio
    private val _jogadoresSorteados = mutableStateListOf<Long>()

    init {
        // Inicializamos os jogadores selecionados
        viewModelScope.launch {
            jogadorRepository.getJogadores().collect { jogadoresLista ->
                // Inicializa com jogadores ativos E disponíveis
                jogadoresSelecionados.clear()
                jogadoresSelecionados.addAll(
                    jogadoresLista.filter { it.ativo && it.disponivel }.map { it.id }
                )
            }
        }

        // Observa a flag de pelada ativa para atualizar o estado do botão de sorteio
        viewModelScope.launch {
            sorteioRepository.temPeladaAtiva.collectLatest { temPeladaAtiva ->
                _botaoSorteioHabilitado.value = !temPeladaAtiva
            }
        }

        // Carrega a configuração para o dropdown
        viewModelScope.launch {
            configuracaoRepository.getConfiguracao().collect { config ->
                if (config != null) {
                    // Como ainda não temos múltiplos perfis implementados,
                    // vamos simular alguns perfis diferentes para o dropdown
                    _configuracoesDisponiveis.clear()

                    // Adiciona o perfil padrão (o que está salvo)
                    _configuracoesDisponiveis.add(config.copy(nome = "Padrão", isPadrao = true))
                }
            }
        }

        // Carrega as configurações disponíveis para o dropdown
        viewModelScope.launch {
            configuracaoRepository.getTodasConfiguracoes().collect { configuracoes ->
                _configuracoesDisponiveis.clear()
                _configuracoesDisponiveis.addAll(configuracoes)
            }
        }
    }

    // Método para selecionar uma configuração
    fun selecionarConfiguracao(config: ConfiguracaoSorteio) {
        // Em um cenário real, você salvaria essa configuração no repositório
        // Por enquanto apenas simulamos a seleção mantendo a configuração atual
        // Aqui seria o lugar para implementar a lógica de salvar a nova configuração
        viewModelScope.launch {
            configuracaoRepository.salvarConfiguracao(config)
        }
    }

    fun toggleJogadorSelecionado(jogadorId: Long, selecionado: Boolean) {
        if (selecionado && !jogadoresSelecionados.contains(jogadorId)) {
            jogadoresSelecionados.add(jogadorId)
        } else if (!selecionado && jogadoresSelecionados.contains(jogadorId)) {
            jogadoresSelecionados.remove(jogadorId)
        }
    }

    fun selecionarTodos() {
        viewModelScope.launch {
            jogadorRepository.getJogadores().first().let { jogadoresLista ->
                jogadoresSelecionados.clear()
                jogadoresSelecionados.addAll(jogadoresLista.filter { it.ativo }.map { it.id })
            }
        }
    }

    fun selecionarDisponiveis() {
        viewModelScope.launch {
            jogadorRepository.getJogadores().first().let { jogadoresLista ->
                jogadoresSelecionados.clear()
                jogadoresSelecionados.addAll(jogadoresLista.filter { it.ativo && it.disponivel }.map { it.id })
            }
        }
    }

    fun desmarcarTodos() {
        jogadoresSelecionados.clear()
    }

    fun verificarESortearTimes() {
        // Com a implementação da pelada ativa, não precisamos mais verificar
        // se há um sorteio não contabilizado, pois o botão já estará desabilitado
        // se houver uma pelada ativa

        // Se o botão estiver habilitado, realizamos o sorteio diretamente
        if (_botaoSorteioHabilitado.value) {
            sortearTimes()
        }
    }

    fun confirmarNovoSorteio() {
        _mostrarDialogConfirmacao.value = false

        // Limpa o histórico de times atual antes de sortear novos times
        viewModelScope.launch {
            try {
                // Limpa os times no repositório
                sorteioRepository.limparHistoricoTimes()

                // Após limpar, inicia um novo sorteio
                sortearTimes()
            } catch (_: Exception) {
                // Em caso de erro, desativa o loading
                sorteioRepository.setSorteioEmAndamento(false)
            }
        }
    }

    fun cancelarNovoSorteio() {
        _mostrarDialogConfirmacao.value = false
    }

    fun sortearTimes() {
        viewModelScope.launch {
            try {
                sorteioRepository.setSorteioEmAndamento(true)

                // Salva os jogadores selecionados para atualizar disponibilidade depois
                _jogadoresSorteados.clear()
                _jogadoresSorteados.addAll(jogadoresSelecionados)

                // Obtém a configuração atual do repositório
                val config = configuracaoRepository.getConfiguracao().first()
                val jogadoresList = jogadorRepository.getJogadores().first()
                    .filter { jogador -> jogadoresSelecionados.contains(jogador.id) }

                // Simula um tempo de processamento para dar feedback visual
                delay(1500)

                // Realiza o sorteio através do use case
                val resultado = sorteioUseCase.sortearTimes(jogadoresList, config)

                // IMPORTANTE: Salvar o resultado no repositório para que a tela de resultado possa acessá-lo
                sorteioRepository.salvarResultadoSorteio(resultado)

                _sorteioRealizado.value = true
                sorteioRepository.setSorteioEmAndamento(false)
            } catch (e: Exception) {
                // Em caso de erro, desativa o loading
                sorteioRepository.setSorteioEmAndamento(false)
            }
        }
    }

    fun resetSorteioRealizado() {
        _sorteioRealizado.value = false
    }

    // Função para atualizar a disponibilidade dos jogadores após confirmar o sorteio
    fun atualizarDisponibilidadeJogadores() {
        viewModelScope.launch {
            val jogadoresAtuais = jogadorRepository.getJogadores().first()

            // Para cada jogador no banco de dados
            jogadoresAtuais.forEach { jogador ->
                // Atualiza o jogador com base na participação no último sorteio
                // Jogadores que participaram são marcados como disponíveis
                // Jogadores que não participaram são marcados como indisponíveis
                val disponivel = _jogadoresSorteados.contains(jogador.id)

                if (jogador.disponivel != disponivel) {
                    jogadorRepository.atualizarJogador(
                        jogador.copy(disponivel = disponivel)
                    )
                }
            }
        }
    }

    fun abrirConfiguracoes() {
        // Aqui poderia navegar para a tela de configurações
    }
}
