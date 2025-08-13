package com.victorhugo.boleiragem.ui.screens.grupos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorhugo.boleiragem.data.model.ConfiguracaoSorteio
import com.victorhugo.boleiragem.data.model.DiaSemana
import com.victorhugo.boleiragem.data.model.GrupoPelada
import com.victorhugo.boleiragem.data.model.Jogador
import com.victorhugo.boleiragem.data.model.ResultadoSorteio
import com.victorhugo.boleiragem.data.model.TipoRecorrencia
import com.victorhugo.boleiragem.data.model.Time
import com.victorhugo.boleiragem.data.repository.ConfiguracaoRepository
import com.victorhugo.boleiragem.data.repository.GrupoPeladaRepository
import com.victorhugo.boleiragem.data.repository.JogadorRepository
import com.victorhugo.boleiragem.data.repository.SorteioRepository
import com.victorhugo.boleiragem.domain.SorteioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TipoVisualizacao { LISTA, CARDS, MINIMALISTA }

@HiltViewModel
class GruposPeladaViewModel @Inject constructor(
    private val grupoPeladaRepository: GrupoPeladaRepository,
    private val jogadorRepository: JogadorRepository,
    private val configuracaoRepository: ConfiguracaoRepository,
    private val sorteioUseCase: SorteioUseCase,
    private val sorteioRepository: SorteioRepository
) : ViewModel() {

    private val _grupos = MutableStateFlow<List<GrupoPelada>>(emptyList())
    val grupos: StateFlow<List<GrupoPelada>> = _grupos.asStateFlow()

    private val _jogadoresPorGrupo = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val jogadoresPorGrupo: StateFlow<Map<Long, Int>> = _jogadoresPorGrupo.asStateFlow()

    private val _tipoVisualizacao = MutableStateFlow(TipoVisualizacao.LISTA)
    val tipoVisualizacao: StateFlow<TipoVisualizacao> = _tipoVisualizacao.asStateFlow()

    private val _carregando = MutableStateFlow(false)
    val carregando: StateFlow<Boolean> = _carregando.asStateFlow()

    private val _mostrarDialogoGrupo = MutableStateFlow(false)
    val mostrarDialogoGrupo: StateFlow<Boolean> = _mostrarDialogoGrupo.asStateFlow()

    private val _grupoEmEdicao = MutableStateFlow<GrupoPelada?>(null)
    val grupoEmEdicao: StateFlow<GrupoPelada?> = _grupoEmEdicao.asStateFlow()

    private val _mostrarSeletorMapas = MutableStateFlow(false)
    val mostrarSeletorMapas: StateFlow<Boolean> = _mostrarSeletorMapas.asStateFlow()

    private val _retornandoDoMapa = MutableStateFlow(false)
    val retornandoDoMapa: StateFlow<Boolean> = _retornandoDoMapa.asStateFlow()

    private var localSelecionadoLatitude: Double? = null
    private var localSelecionadoLongitude: Double? = null
    private var localSelecionadoEndereco: String? = null
    private var localSelecionadoNome: String? = null

    private val _grupoParaCompartilhar = MutableStateFlow<GrupoPelada?>(null)
    val grupoParaCompartilhar: StateFlow<GrupoPelada?> = _grupoParaCompartilhar.asStateFlow()

    private val _mostrarTelaCompartilhamento = MutableStateFlow(false)
    val mostrarTelaCompartilhamento: StateFlow<Boolean> = _mostrarTelaCompartilhamento.asStateFlow()

    private val _mostrarDialogoConfigSorteioRapido = MutableStateFlow(false)
    val mostrarDialogoConfigSorteioRapido: StateFlow<Boolean> = _mostrarDialogoConfigSorteioRapido.asStateFlow()

    private val _grupoSelecionadoParaSorteioRapido = MutableStateFlow<GrupoPelada?>(null)
    val grupoSelecionadoParaSorteioRapido: StateFlow<GrupoPelada?> = _grupoSelecionadoParaSorteioRapido.asStateFlow()

    private val _jogadoresAtivosParaDialogoSorteioRapido = MutableStateFlow(0)
    val jogadoresAtivosParaDialogoSorteioRapido: StateFlow<Int> = _jogadoresAtivosParaDialogoSorteioRapido.asStateFlow()

    private val _perfisConfiguracaoSorteio = MutableStateFlow<List<ConfiguracaoSorteio>>(emptyList())
    val perfisConfiguracaoSorteio: StateFlow<List<ConfiguracaoSorteio>> = _perfisConfiguracaoSorteio.asStateFlow()

    private val _usarPerfilExistenteSorteioRapido = MutableStateFlow(false)
    val usarPerfilExistenteSorteioRapido: StateFlow<Boolean> = _usarPerfilExistenteSorteioRapido.asStateFlow()

    private val _perfilConfigSelecionadoSorteioRapido = MutableStateFlow<ConfiguracaoSorteio?>(null)
    val perfilConfigSelecionadoSorteioRapido: StateFlow<ConfiguracaoSorteio?> = _perfilConfigSelecionadoSorteioRapido.asStateFlow()

    private val _jogadoresPorTimeSorteioRapido = MutableStateFlow(5)
    val jogadoresPorTimeSorteioRapido: StateFlow<Int> = _jogadoresPorTimeSorteioRapido.asStateFlow()

    private val _numeroDeTimesSorteioRapido = MutableStateFlow(2)
    val numeroDeTimesSorteioRapido: StateFlow<Int> = _numeroDeTimesSorteioRapido.asStateFlow()

    private val _navegarParaResultadoSorteio = MutableStateFlow<Boolean?>(null)
    val navegarParaResultadoSorteio: StateFlow<Boolean?> = _navegarParaResultadoSorteio.asStateFlow()

    private val _erroSorteioRapido = MutableStateFlow<String?>(null)
    val erroSorteioRapido: StateFlow<String?> = _erroSorteioRapido.asStateFlow()

    private val _podeRealizarSorteioRapido = MutableStateFlow(false)
    val podeRealizarSorteioRapido: StateFlow<Boolean> = _podeRealizarSorteioRapido.asStateFlow()

    init {
        carregarGrupos()
    }

    fun carregarGrupos() {
        viewModelScope.launch {
            _carregando.value = true
            try {
                grupoPeladaRepository.getTodosGrupos().collect { gruposLista ->
                    _grupos.value = gruposLista
                    atualizarJogadoresPorGrupo()
                    _carregando.value = false
                }
            } catch (e: Exception) {
                Log.e("GruposVM", "Erro ao carregar grupos", e)
                _carregando.value = false
            }
        }
    }

    private fun atualizarJogadoresPorGrupo() {
        viewModelScope.launch {
            val mapaContagem = mutableMapOf<Long, Int>()
            _grupos.value.forEach { grupo ->
                mapaContagem[grupo.id] = jogadorRepository.countJogadoresAtivosPorGrupo(grupo.id)
            }
            _jogadoresPorGrupo.value = mapaContagem
            // Atualizar contagem para o diálogo se já estiver aberto e o grupo for o mesmo
            _grupoSelecionadoParaSorteioRapido.value?.let { grupoAtualDialogo ->
                if (_mostrarDialogoConfigSorteioRapido.value) {
                    _jogadoresAtivosParaDialogoSorteioRapido.value = _jogadoresPorGrupo.value[grupoAtualDialogo.id] ?: 0
                    validarConfiguracaoSorteioRapido()
                }
            }
        }
    }

    fun alterarTipoVisualizacao(tipo: TipoVisualizacao) { _tipoVisualizacao.value = tipo }
    fun mostrarDialogoCriarGrupo() { _grupoEmEdicao.value = null; _mostrarDialogoGrupo.value = true }
    fun mostrarDialogoEditarGrupo(grupo: GrupoPelada) { _grupoEmEdicao.value = grupo; _mostrarDialogoGrupo.value = true }
    fun fecharDialogoGrupo() {
        _mostrarDialogoGrupo.value = false
        _grupoEmEdicao.value = null
        localSelecionadoLatitude = null
        localSelecionadoLongitude = null
        localSelecionadoEndereco = null
        localSelecionadoNome = null
    }

    fun salvarGrupo(
        nome: String, local: String, horario: String, imagemUrl: String?, descricao: String?,
        tipoRecorrencia: TipoRecorrencia, diaSemana: DiaSemana?, latitude: Double?, longitude: Double?,
        endereco: String?, localNome: String?, diasSemana: List<DiaSemana> = emptyList()
    ) {
        viewModelScope.launch {
            try {
                val grupoAtual = _grupoEmEdicao.value
                val diasSemanaFinal = if (tipoRecorrencia == TipoRecorrencia.RECORRENTE) {
                    if (diasSemana.isEmpty() && diaSemana != null) listOf(diaSemana) else diasSemana
                } else {
                    if (diaSemana != null) listOf(diaSemana) else emptyList()
                }
                val grupoSalvar = grupoAtual?.copy(
                    nome = nome, local = local, horario = horario, imagemUrl = imagemUrl, descricao = descricao,
                    tipoRecorrencia = tipoRecorrencia, diaSemana = diaSemana ?: DiaSemana.DOMINGO,
                    diasSemana = diasSemanaFinal, latitude = latitude, longitude = longitude, endereco = endereco,
                    localNome = localNome, ultimaModificacao = System.currentTimeMillis()
                ) ?: GrupoPelada(
                    id = 0, nome = nome, local = local, horario = horario, imagemUrl = imagemUrl, descricao = descricao,
                    tipoRecorrencia = tipoRecorrencia, diaSemana = diaSemana ?: DiaSemana.DOMINGO,
                    diasSemana = diasSemanaFinal, latitude = latitude, longitude = longitude, endereco = endereco,
                    localNome = localNome, dataCriacao = System.currentTimeMillis(), ultimaModificacao = System.currentTimeMillis(),
                    ativo = true, jogadoresIds = emptyList(), usuarioId = "local", compartilhado = false
                )
                if (grupoAtual != null) grupoPeladaRepository.atualizarGrupo(grupoSalvar) else grupoPeladaRepository.inserirGrupo(grupoSalvar)
                fecharDialogoGrupo()
                kotlinx.coroutines.delay(200) // Pequeno delay para garantir que a UI possa reagir antes de recarregar
                carregarGrupos() // Isso chamará atualizarJogadoresPorGrupo()
            } catch (e: Exception) { Log.e("GruposVM", "Erro ao salvar grupo", e) }
        }
    }

    fun excluirGrupo(grupo: GrupoPelada) {
        viewModelScope.launch {
            try { grupoPeladaRepository.excluirGrupo(grupo); carregarGrupos() }
            catch (e: Exception) { Log.e("GruposVM", "Erro ao excluir grupo", e) }
        }
    }

    fun mostrarSeletorMapas() { _mostrarSeletorMapas.value = true }
    fun ocultarSeletorMapas() { _mostrarSeletorMapas.value = false }
    fun processarLocalSelecionado(latitude: Double, longitude: Double, endereco: String, nome: String?) {
        localSelecionadoLatitude = latitude; localSelecionadoLongitude = longitude; localSelecionadoEndereco = endereco; localSelecionadoNome = nome
        _retornandoDoMapa.value = true; _mostrarSeletorMapas.value = false; _mostrarDialogoGrupo.value = true
    }
    fun cancelarSelecaoLocalizacao() { _mostrarSeletorMapas.value = false; _mostrarDialogoGrupo.value = true }
    fun resetRetornoMapa() { _retornandoDoMapa.value = false }
    fun obterDadosLocalizacao(): Map<String, Any?> = mapOf("latitude" to localSelecionadoLatitude, "longitude" to localSelecionadoLongitude, "endereco" to localSelecionadoEndereco, "nomeLocal" to localSelecionadoNome)
    fun salvarLocalizacaoSelecionada(latitude: Double, longitude: Double, endereco: String, nome: String?) {
        localSelecionadoLatitude = latitude; localSelecionadoLongitude = longitude; localSelecionadoEndereco = endereco; localSelecionadoNome = nome
        _retornandoDoMapa.value = true; _mostrarSeletorMapas.value = false
    }
    fun exibirTelaCompartilhamento(grupo: GrupoPelada) { _grupoParaCompartilhar.value = grupo; _mostrarTelaCompartilhamento.value = true }
    fun ocultarTelaCompartilhamento() { _mostrarTelaCompartilhamento.value = false; _grupoParaCompartilhar.value = null }

    fun onAbrirDialogoSorteioRapido(grupo: GrupoPelada) {
        _grupoSelecionadoParaSorteioRapido.value = grupo
        _erroSorteioRapido.value = null
        configuracaoRepository.setGrupoId(grupo.id)
        viewModelScope.launch {
            try {
                // Busca a contagem de jogadores mais recente diretamente
                _jogadoresAtivosParaDialogoSorteioRapido.value = jogadorRepository.countJogadoresAtivosPorGrupo(grupo.id)

                val perfis = configuracaoRepository.getTodasConfiguracoes().firstOrNull() ?: emptyList()
                _perfisConfiguracaoSorteio.value = perfis
                val perfilPadrao = perfis.firstOrNull { p -> p.isPadrao } ?: perfis.firstOrNull()
                _perfilConfigSelecionadoSorteioRapido.value = perfilPadrao
                _usarPerfilExistenteSorteioRapido.value = perfilPadrao != null
                if (perfilPadrao != null) {
                    _jogadoresPorTimeSorteioRapido.value = perfilPadrao.qtdJogadoresPorTime
                    _numeroDeTimesSorteioRapido.value = perfilPadrao.qtdTimes
                } else {
                    // Mantém valores padrão ou busca de um repositório de configurações default
                    _jogadoresPorTimeSorteioRapido.value = 5
                    _numeroDeTimesSorteioRapido.value = 2
                }
                validarConfiguracaoSorteioRapido() // Valida com a contagem atualizada
            } catch (e: Exception) {
                Log.e("GruposVM", "Erro ao carregar dados para sorteio rápido", e)
                _jogadoresAtivosParaDialogoSorteioRapido.value = 0 // Reseta em caso de erro
                _perfisConfiguracaoSorteio.value = emptyList()
                _usarPerfilExistenteSorteioRapido.value = false
                 _jogadoresPorTimeSorteioRapido.value = 5
                 _numeroDeTimesSorteioRapido.value = 2
                validarConfiguracaoSorteioRapido()
            }
        }
        _mostrarDialogoConfigSorteioRapido.value = true
    }

    private fun validarConfiguracaoSorteioRapido() {
        val jogadoresDisponiveis = _jogadoresAtivosParaDialogoSorteioRapido.value
        val jogadoresPorTimeConfig: Int
        val numeroDeTimesConfig: Int

        if (_usarPerfilExistenteSorteioRapido.value) {
            val perfil = _perfilConfigSelecionadoSorteioRapido.value
            if (perfil == null && _perfisConfiguracaoSorteio.value.isNotEmpty()) {
                _erroSorteioRapido.value = "Selecione um perfil de configuração."
                _podeRealizarSorteioRapido.value = false
                return
            } else if (perfil == null && _perfisConfiguracaoSorteio.value.isEmpty()) {
                 _erroSorteioRapido.value = "Nenhum perfil disponível. Configure manualmente."
                 _podeRealizarSorteioRapido.value = false
                 return
            }
            jogadoresPorTimeConfig = perfil?.qtdJogadoresPorTime ?: _jogadoresPorTimeSorteioRapido.value
            numeroDeTimesConfig = perfil?.qtdTimes ?: _numeroDeTimesSorteioRapido.value
        } else {
            jogadoresPorTimeConfig = _jogadoresPorTimeSorteioRapido.value
            numeroDeTimesConfig = _numeroDeTimesSorteioRapido.value
        }

        if (jogadoresPorTimeConfig <= 0) {
            _erroSorteioRapido.value = "Jogadores por time deve ser maior que zero."
            _podeRealizarSorteioRapido.value = false
            return
        }
        if (numeroDeTimesConfig <= 0) {
            _erroSorteioRapido.value = "Número de times deve ser maior que zero."
            _podeRealizarSorteioRapido.value = false
            return
        }

        val jogadoresNecessarios = jogadoresPorTimeConfig * numeroDeTimesConfig
        if (jogadoresNecessarios <= 0) {
             _erroSorteioRapido.value = "Configuração de sorteio inválida."
             _podeRealizarSorteioRapido.value = false
             return
        }

        if (jogadoresDisponiveis < jogadoresNecessarios) {
            _erroSorteioRapido.value = "São necessários $jogadoresNecessarios jogadores (${numeroDeTimesConfig}x${jogadoresPorTimeConfig}). Grupo: $jogadoresDisponiveis ativos."
            _podeRealizarSorteioRapido.value = false
        } else if (jogadoresDisponiveis > jogadoresNecessarios) {
            _erroSorteioRapido.value = "Grupo: $jogadoresDisponiveis ativos. Serão usados $jogadoresNecessarios (${numeroDeTimesConfig}x${jogadoresPorTimeConfig}). ${jogadoresDisponiveis - jogadoresNecessarios} ficarão de fora."
            _podeRealizarSorteioRapido.value = true
        } else {
            _erroSorteioRapido.value = null
            _podeRealizarSorteioRapido.value = true
        }
    }

    fun onFecharDialogoSorteioRapido() {
        _mostrarDialogoConfigSorteioRapido.value = false
    }

    fun onUsarPerfilExistenteSorteioRapidoChanged(usar: Boolean) {
        _usarPerfilExistenteSorteioRapido.value = usar
        if (usar) {
            val perfilPadrao = _perfisConfiguracaoSorteio.value.firstOrNull { p -> p.isPadrao } ?: _perfisConfiguracaoSorteio.value.firstOrNull()
            _perfilConfigSelecionadoSorteioRapido.value = perfilPadrao
        } else {
            _perfilConfigSelecionadoSorteioRapido.value = null
        }
        limparErroSorteioRapido()
        validarConfiguracaoSorteioRapido()
    }

    fun onPerfilConfigSorteioRapidoSelecionado(perfil: ConfiguracaoSorteio) {
        _perfilConfigSelecionadoSorteioRapido.value = perfil
        limparErroSorteioRapido()
        validarConfiguracaoSorteioRapido()
    }

    fun onJogadoresPorTimeSorteioRapidoChanged(novoValor: Int) {
        _jogadoresPorTimeSorteioRapido.value = novoValor
        limparErroSorteioRapido()
        validarConfiguracaoSorteioRapido()
    }

    fun onNumeroDeTimesSorteioRapidoChanged(novoValor: Int) {
        _numeroDeTimesSorteioRapido.value = novoValor
        limparErroSorteioRapido()
        validarConfiguracaoSorteioRapido()
    }

    fun limparErroSorteioRapido() {
        _erroSorteioRapido.value = null
    }

    fun onConfirmarSorteioRapido() {
        if (!_podeRealizarSorteioRapido.value) {
            if (_erroSorteioRapido.value.isNullOrBlank()) {
                 _erroSorteioRapido.value = "Configuração de sorteio inválida ou jogadores insuficientes."
            }
            return
        }

        val grupo = _grupoSelecionadoParaSorteioRapido.value
        if (grupo == null) {
            _erroSorteioRapido.value = "Nenhum grupo selecionado."
            return
        }

        viewModelScope.launch {
            try {
                val jogadoresAtivos = jogadorRepository.getJogadoresListAtivosPorGrupo(grupo.id)

                if (jogadoresAtivos.isEmpty()) {
                    _erroSorteioRapido.value = "Não há jogadores ativos no grupo para o sorteio."
                    _podeRealizarSorteioRapido.value = false // Garante que não possa prosseguir
                    return@launch
                }

                val configSorteioUsada: ConfiguracaoSorteio
                if (_usarPerfilExistenteSorteioRapido.value && _perfilConfigSelecionadoSorteioRapido.value != null) {
                    configSorteioUsada = _perfilConfigSelecionadoSorteioRapido.value!!
                } else {
                    configSorteioUsada = ConfiguracaoSorteio(
                        id = 0L, 
                        nome = "Sorteio Rápido Manual",
                        qtdJogadoresPorTime = _jogadoresPorTimeSorteioRapido.value,
                        qtdTimes = _numeroDeTimesSorteioRapido.value,
                        aleatorio = true, // Sorteio rápido é sempre aleatório por enquanto
                        isPadrao = false,
                        grupoId = grupo.id
                        // criteriosExtras pode ser omitido se aleatorio = true for suficiente
                    )
                }

                val jogadoresNecessarios = configSorteioUsada.qtdJogadoresPorTime * configSorteioUsada.qtdTimes
                if (jogadoresAtivos.size < jogadoresNecessarios) {
                    _erroSorteioRapido.value = "Jogadores insuficientes. Necessários: $jogadoresNecessarios, Disponíveis: ${jogadoresAtivos.size}."
                    _podeRealizarSorteioRapido.value = false // Garante que não possa prosseguir
                    return@launch
                }

                // Lógica de simulação ou chamada real ao SorteioUseCase
                // A simulação abaixo é um placeholder e pode ser substituída pela chamada real.
                Log.d("GruposVM", "SORTEANDO com ${jogadoresAtivos.size} jogadores, ${configSorteioUsada.qtdTimes} times de ${configSorteioUsada.qtdJogadoresPorTime}")
                val jogadoresSorteaveis = if (jogadoresAtivos.size > jogadoresNecessarios) {
                    jogadoresAtivos.shuffled().take(jogadoresNecessarios)
                } else {
                    jogadoresAtivos.shuffled()
                }

                val resultadoSorteio: ResultadoSorteio? = sorteioUseCase.sortearTimesRapido(jogadoresSorteaveis, configSorteioUsada, configSorteioUsada.qtdTimes)

                if (resultadoSorteio != null && resultadoSorteio.times.isNotEmpty()) {
                    sorteioRepository.salvarResultadoSorteioRapido(resultadoSorteio)
                    _navegarParaResultadoSorteio.value = true
                    onFecharDialogoSorteioRapido()
                } else {
                    _erroSorteioRapido.value = "Falha ao formar times. Verifique a configuração e jogadores disponíveis."
                    Log.e("GruposVM", "Sorteio (real ou simulado) não formou times ou falhou.")
                }

            } catch (e: Exception) {
                Log.e("GruposVM", "Erro ao confirmar sorteio rápido", e)
                _erroSorteioRapido.value = "Erro inesperado ao sortear: ${e.localizedMessage}"
            }
        }
    }

    fun onNavegacaoParaResultadoSorteioRealizada() {
        _navegarParaResultadoSorteio.value = null
    }
}
