package com.victorhugo.boleiragem.ui.screens.configuracao

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorhugo.boleiragem.data.model.ConfiguracaoSorteio
import com.victorhugo.boleiragem.data.model.CriterioSorteio
import com.victorhugo.boleiragem.data.repository.ConfiguracaoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfiguracaoTimesViewModel @Inject constructor(
    private val repository: ConfiguracaoRepository
) : ViewModel() {

    // Método para definir o ID do grupo atual
    fun setGrupoId(id: Long) {
        repository.setGrupoId(id)
    }

    // Configuração atualmente selecionada
    val configuracao = repository.getConfiguracao()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Lista de todas as configurações disponíveis
    val todasConfiguracoes = repository.getTodasConfiguracoes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Configuração selecionada no spinner
    private val _configuracaoSelecionadaId = MutableStateFlow<Long>(0)
    val configuracaoSelecionadaId: StateFlow<Long> = _configuracaoSelecionadaId

    var jogadoresPorTime by mutableIntStateOf(5)
        internal set

    var quantidadeTimes by mutableIntStateOf(2)
        internal set

    // Aleatório é agora um booleano separado
    var aleatorio by mutableStateOf(true)
        private set

    // Nome da configuração atual
    var nomeConfiguracao by mutableStateOf("")
        private set

    // Todos os outros critérios são extras combináveis
    var criteriosExtras by mutableStateOf<Set<CriterioSorteio>>(emptySet())
        private set

    // Estado para controlar o feedback de salvamento
    private val _configSalva = MutableStateFlow(false)
    val configSalva: StateFlow<Boolean> = _configSalva

    // Estado para controlar a navegação para a tela de gerenciamento de perfis
    private val _navegarParaGerenciadorPerfis = MutableStateFlow(false)
    val navegarParaGerenciadorPerfis: StateFlow<Boolean> = _navegarParaGerenciadorPerfis

    // Estado para controlar diálogo de sobrescrever configuração
    private val _mostrarDialogoSobrescrever = MutableStateFlow(false)
    val mostrarDialogoSobrescrever: StateFlow<Boolean> = _mostrarDialogoSobrescrever

    // Configuração a ser salva/sobrescrita
    private var configuracaoParaSalvar: ConfiguracaoSorteio? = null

    // Estado para controlar diálogo de nomeação
    private val _mostrarDialogoNomeConfiguracao = MutableStateFlow(false)
    val mostrarDialogoNomeConfiguracao: StateFlow<Boolean> = _mostrarDialogoNomeConfiguracao

    init {
        viewModelScope.launch {
            repository.getConfiguracao().collect { config ->
                config?.let {
                    jogadoresPorTime = it.qtdJogadoresPorTime
                    quantidadeTimes = it.qtdTimes
                    aleatorio = it.aleatorio
                    criteriosExtras = it.criteriosExtras
                    nomeConfiguracao = it.nome
                    _configuracaoSelecionadaId.value = it.id
                }
            }
        }
    }

    // Método para selecionar uma configuração do spinner
    fun selecionarConfiguracao(id: Long) {
        viewModelScope.launch {
            val config = repository.getConfiguracaoById(id)
            config?.let {
                jogadoresPorTime = it.qtdJogadoresPorTime
                quantidadeTimes = it.qtdTimes
                aleatorio = it.aleatorio
                criteriosExtras = it.criteriosExtras
                nomeConfiguracao = it.nome
                _configuracaoSelecionadaId.value = it.id

                // Define esta configuração como padrão
                repository.definirConfiguracaoPadrao(id)
            }
        }
    }

    // Método para atualizar o nome da configuração
    fun atualizarNomeConfiguracao(novoNome: String) {
        nomeConfiguracao = novoNome
    }

    // Verifica se já existe uma configuração com as mesmas características
    private fun verificaConfiguracaoDuplicada(config: ConfiguracaoSorteio): Boolean {
        return todasConfiguracoes.value.any { existente ->
            // Não comparar o item com ele mesmo (mesma ID)
            existente.id != config.id &&
            // Verificar se todas as características importantes são iguais
            existente.qtdJogadoresPorTime == config.qtdJogadoresPorTime &&
            existente.qtdTimes == config.qtdTimes &&
            existente.aleatorio == config.aleatorio &&
            compareSetsCriterios(existente.criteriosExtras, config.criteriosExtras)
        }
    }

    // Comparação adequada de Sets de critérios
    private fun compareSetsCriterios(set1: Set<CriterioSorteio>, set2: Set<CriterioSorteio>): Boolean {
        if (set1.size != set2.size) return false
        return set1.containsAll(set2) && set2.containsAll(set1)
    }

    // Método para alternar o critério aleatório
    fun toggleAleatorio(ativo: Boolean) {
        aleatorio = ativo

        // Se aleatório for ativado, limpa todos os critérios extras
        if (ativo) {
            criteriosExtras = emptySet()
        }
        // Se aleatório for desativado, seleciona pelo menos um critério extra (Pontuação)
        else if (criteriosExtras.isEmpty()) {
            criteriosExtras = setOf(CriterioSorteio.PONTUACAO)
        }

        saveConfiguracao()
    }

    // Método para alternar critérios extras
    fun toggleCriterioExtra(criterio: CriterioSorteio) {
        // Se aleatório estiver ativo, não permite selecionar critérios extras
        if (aleatorio) return

        // Atualiza os critérios extras
        criteriosExtras = if (criteriosExtras.contains(criterio)) {
            // Se está removendo um critério, verifica se há pelo menos um selecionado
            val novosCriterios = criteriosExtras.minus(criterio)
            if (novosCriterios.isEmpty()) {
                // Mantém pelo menos um critério selecionado
                criteriosExtras
            } else {
                novosCriterios
            }
        } else {
            criteriosExtras.plus(criterio)
        }

        saveConfiguracao()
    }

    // Método para salvar a configuração atual com verificação de duplicidade
    fun iniciarSalvamentoConfiguracao() {
        val novaConfig = ConfiguracaoSorteio(
            id = 0, // Usando 0 para indicar uma nova configuração (será atribuído automaticamente)
            nome = nomeConfiguracao,
            qtdJogadoresPorTime = jogadoresPorTime,
            qtdTimes = quantidadeTimes,
            aleatorio = aleatorio,
            criteriosExtras = criteriosExtras,
            isPadrao = false // Não definimos como padrão automaticamente
        )

        configuracaoParaSalvar = novaConfig

        // Verifica se existe configuração duplicada
        if (verificaConfiguracaoDuplicada(novaConfig)) {
            // Se for uma configuração duplicada, mostra diálogo de confirmação
            _mostrarDialogoSobrescrever.value = true
        } else {
            // Se for uma nova configuração, pede um nome
            _mostrarDialogoNomeConfiguracao.value = true
        }
    }

    // Confirmar sobrescrita de configuração
    fun confirmarSobrescrita() {
        _mostrarDialogoSobrescrever.value = false
        _mostrarDialogoNomeConfiguracao.value = true
    }

    // Cancelar sobrescrita
    fun cancelarSobrescrita() {
        _mostrarDialogoSobrescrever.value = false
        configuracaoParaSalvar = null
    }

    // Confirmar nome e salvar configuração
    fun confirmarNomeESalvar(nome: String) {
        _mostrarDialogoNomeConfiguracao.value = false

        configuracaoParaSalvar?.let { config ->
            val configFinal = config.copy(nome = nome)
            viewModelScope.launch {
                repository.salvarConfiguracao(configFinal)

                // Ativar o estado de feedback
                _configSalva.value = true
                // Resetar o estado após 2 segundos
                kotlinx.coroutines.delay(2000)
                _configSalva.value = false
            }
        }

        configuracaoParaSalvar = null
    }

    // Cancelar diálogo de nome
    fun cancelarNomeConfiguracao() {
        _mostrarDialogoNomeConfiguracao.value = false
        configuracaoParaSalvar = null
    }

    // Método para salvar a configuração atual
    private fun saveConfiguracao() {
        viewModelScope.launch {
            val novaConfig = ConfiguracaoSorteio(
                id = configuracaoSelecionadaId.value, // Preservando o ID da configuração atual
                nome = nomeConfiguracao, // Preservando o nome da configuração
                qtdJogadoresPorTime = jogadoresPorTime,
                qtdTimes = quantidadeTimes,
                aleatorio = aleatorio,
                criteriosExtras = criteriosExtras,
                isPadrao = true // Mantendo como padrão
            )
            repository.salvarConfiguracao(novaConfig)
        }
    }

    fun salvarConfiguracoes() {
        val novaConfig = ConfiguracaoSorteio(
            id = configuracaoSelecionadaId.value,
            nome = nomeConfiguracao,
            qtdJogadoresPorTime = jogadoresPorTime,
            qtdTimes = quantidadeTimes,
            aleatorio = aleatorio,
            criteriosExtras = criteriosExtras,
            isPadrao = true
        )

        // Verifica se já existe uma configuração igual (exceto a atual)
        val configDuplicada = todasConfiguracoes.value.firstOrNull { existente ->
            existente.id != novaConfig.id &&
            existente.qtdJogadoresPorTime == novaConfig.qtdJogadoresPorTime &&
            existente.qtdTimes == novaConfig.qtdTimes &&
            existente.aleatorio == novaConfig.aleatorio &&
            compareSetsCriterios(existente.criteriosExtras, novaConfig.criteriosExtras)
        }

        viewModelScope.launch {
            // Se encontrou uma configuração duplicada
            if (configDuplicada != null) {
                // Em vez de criar um novo perfil, apenas define o existente como padrão
                repository.definirConfiguracaoPadrao(configDuplicada.id)
                _configuracaoSelecionadaId.value = configDuplicada.id
            } else {
                // Caso contrário, salva a configuração atual
                repository.salvarConfiguracao(novaConfig)
            }

            // Ativar o estado de feedback
            _configSalva.value = true
            // Resetar o estado após 2 segundos
            kotlinx.coroutines.delay(2000)
            _configSalva.value = false
        }
    }

    fun atualizarJogadoresPorTime(valor: Int) {
        jogadoresPorTime = valor
        saveConfiguracao()
    }

    fun atualizarQuantidadeTimes(valor: Int) {
        quantidadeTimes = valor
        saveConfiguracao()
    }

    // Método para navegar para a tela de gerenciamento de perfis
    fun navegarParaGerenciadorPerfis() {
        _navegarParaGerenciadorPerfis.value = true
    }

    // Método para resetar o estado de navegação após a navegação ter sido realizada
    fun onNavegacaoRealizada() {
        _navegarParaGerenciadorPerfis.value = false
    }
}
