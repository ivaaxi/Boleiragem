package com.victorhugo.boleiragem.ui.screens.grupos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorhugo.boleiragem.data.model.DiaSemana
import com.victorhugo.boleiragem.data.model.GrupoPelada
import com.victorhugo.boleiragem.data.model.TipoRecorrencia
import com.victorhugo.boleiragem.data.repository.GrupoPeladaRepository
import com.victorhugo.boleiragem.data.repository.JogadorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Tipos de visualização possíveis para a lista de grupos
 */
enum class TipoVisualizacao {
    LISTA,       // Lista vertical tradicional
    CARDS,       // Cards retangulares (3 por linha)
    MINIMALISTA  // Ícones arredondados com nome abaixo
}

@HiltViewModel
class GruposPeladaViewModel @Inject constructor(
    private val grupoPeladaRepository: GrupoPeladaRepository,
    private val jogadorRepository: JogadorRepository
) : ViewModel() {

    // Estado para armazenar os grupos de pelada ativos
    private val _grupos = MutableStateFlow<List<GrupoPelada>>(emptyList())
    val grupos: StateFlow<List<GrupoPelada>> = _grupos.asStateFlow()

    // Estado para controlar o tipo de visualização
    private val _tipoVisualizacao = MutableStateFlow(TipoVisualizacao.LISTA)
    val tipoVisualizacao: StateFlow<TipoVisualizacao> = _tipoVisualizacao.asStateFlow()

    // Estado para controlar o carregamento
    private val _carregando = MutableStateFlow(true)
    val carregando: StateFlow<Boolean> = _carregando.asStateFlow()

    // Estado para controlar o diálogo de criação/edição
    private val _mostrarDialogo = MutableStateFlow(false)
    val mostrarDialogo: StateFlow<Boolean> = _mostrarDialogo.asStateFlow()

    // Estado para controlar o grupo em edição
    private val _grupoEmEdicao = MutableStateFlow<GrupoPelada?>(null)
    val grupoEmEdicao: StateFlow<GrupoPelada?> = _grupoEmEdicao.asStateFlow()

    // Estados para o sorteio rápido
    private val _mostrarDialogSorteioRapido = MutableStateFlow(false)
    val mostrarDialogSorteioRapido: StateFlow<Boolean> = _mostrarDialogSorteioRapido.asStateFlow()

    private val _mostrarDialogSelecaoPelada = MutableStateFlow(false)
    val mostrarDialogSelecaoPelada: StateFlow<Boolean> = _mostrarDialogSelecaoPelada.asStateFlow()

    private val _mostrarDialogOpcoesPelada = MutableStateFlow(false)
    val mostrarDialogOpcoesPelada: StateFlow<Boolean> = _mostrarDialogOpcoesPelada.asStateFlow()

    private val _peladaSelecionadaId = MutableStateFlow<Long?>(null)
    val peladaSelecionadaId: StateFlow<Long?> = _peladaSelecionadaId.asStateFlow()

    // Estado para controlar o seletor de mapas
    private val _mostrarSeletorMapas = MutableStateFlow(false)
    val mostrarSeletorMapas: StateFlow<Boolean> = _mostrarSeletorMapas.asStateFlow()

    // Estado para controlar o compartilhamento
    private val _mostrarTelaCompartilhamento = MutableStateFlow(false)
    val mostrarTelaCompartilhamento: StateFlow<Boolean> = _mostrarTelaCompartilhamento.asStateFlow()

    // Estado para controlar o grupo para compartilhar
    private val _grupoParaCompartilhar = MutableStateFlow<GrupoPelada?>(null)
    val grupoParaCompartilhar: StateFlow<GrupoPelada?> = _grupoParaCompartilhar.asStateFlow()

    // Estado para controlar o retorno do mapa
    private val _retornandoDoMapa = MutableStateFlow(false)
    val retornandoDoMapa: StateFlow<Boolean> = _retornandoDoMapa.asStateFlow()

    // Dados temporários para armazenar localização selecionada
    private var localSelecionadoLatitude: Double? = null
    private var localSelecionadoLongitude: Double? = null
    private var localSelecionadoEndereco: String? = null
    private var localSelecionadoNome: String? = null

    // Estado para armazenar o número de jogadores por grupo
    private val _jogadoresPorGrupo = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val jogadoresPorGrupo: StateFlow<Map<Long, Int>> = _jogadoresPorGrupo.asStateFlow()

    init {
        carregarGrupos()
        carregarJogadoresPorGrupo()
    }

    /**
     * Carrega todos os grupos de pelada ativos
     */
    fun carregarGrupos() {
        viewModelScope.launch {
            _carregando.value = true
            try {
                // Coleta os resultados do Flow para uma lista
                grupoPeladaRepository.getTodosGrupos().collect { gruposLista ->
                    _grupos.value = gruposLista
                    _carregando.value = false
                    // Atualiza o mapeamento de jogadores após carregar os grupos
                    atualizarJogadoresPorGrupo()
                }
            } catch (e: Exception) {
                // Tratar erro de carregamento
                e.printStackTrace()
                _carregando.value = false
            }
        }
    }

    /**
     * Altera o tipo de visualização da lista de grupos
     */
    fun alterarTipoVisualizacao(tipo: TipoVisualizacao) {
        _tipoVisualizacao.value = tipo
    }

    /**
     * Exibe o diálogo para criar um novo grupo de pelada
     */
    fun mostrarDialogoCriarGrupo() {
        _grupoEmEdicao.value = null
        _mostrarDialogo.value = true
    }

    /**
     * Exibe o diálogo para editar um grupo existente
     */
    fun mostrarDialogoEditarGrupo(grupo: GrupoPelada) {
        _grupoEmEdicao.value = grupo
        _mostrarDialogo.value = true
    }

    /**
     * Fecha o diálogo de criação/edição
     */
    fun fecharDialogo() {
        _mostrarDialogo.value = false
        _grupoEmEdicao.value = null
        // Limpar dados de localização
        localSelecionadoLatitude = null
        localSelecionadoLongitude = null
        localSelecionadoEndereco = null
        localSelecionadoNome = null
    }

    /**
     * Salva um grupo de pelada (novo ou existente)
     */
    fun salvarGrupo(
        nome: String,
        local: String,
        horario: String,
        imagemUrl: String?,
        descricao: String?,
        tipoRecorrencia: TipoRecorrencia,
        diaSemana: DiaSemana?,
        latitude: Double?,
        longitude: Double?,
        endereco: String?,
        localNome: String?,
        diasSemana: List<DiaSemana> = emptyList()
    ) {
        println("DEBUG: Iniciando método salvarGrupo - nome: $nome, tipo: $tipoRecorrencia")
        
        viewModelScope.launch {
            try {
                // Log para debug: início da tentativa de salvar
                println("DEBUG: Processando salvamento de grupo: $nome")

                // Criar ou atualizar o grupo
                val grupoAtual = _grupoEmEdicao.value

                val diasSemanaFinal = if (tipoRecorrencia == TipoRecorrencia.RECORRENTE) {
                    // Se for recorrente, usa a lista de dias fornecida
                    if (diasSemana.isEmpty() && diaSemana != null) {
                        // Fallback para compatibilidade
                        listOf(diaSemana)
                    } else {
                        diasSemana
                    }
                } else {
                    // Se não for recorrente, usa apenas o dia principal
                    if (diaSemana != null) listOf(diaSemana) else emptyList()
                }

                val grupo = grupoAtual?.copy(
                    nome = nome,
                    local = local,
                    horario = horario,
                    imagemUrl = imagemUrl,
                    descricao = descricao,
                    tipoRecorrencia = tipoRecorrencia,
                    diaSemana = diaSemana ?: DiaSemana.DOMINGO, // Valor padrão para evitar nulos
                    diasSemana = diasSemanaFinal,
                    latitude = latitude,
                    longitude = longitude,
                    endereco = endereco,
                    localNome = localNome,
                    ultimaModificacao = System.currentTimeMillis()
                ) ?: GrupoPelada(
                    id = 0, // O repositório atribuirá um ID
                    nome = nome,
                    local = local,
                    horario = horario,
                    imagemUrl = imagemUrl,
                    descricao = descricao,
                    tipoRecorrencia = tipoRecorrencia,
                    diaSemana = diaSemana ?: DiaSemana.DOMINGO, // Valor padrão para evitar nulos
                    diasSemana = diasSemanaFinal,
                    latitude = latitude,
                    longitude = longitude,
                    endereco = endereco,
                    localNome = localNome,
                    dataCriacao = System.currentTimeMillis(),
                    ultimaModificacao = System.currentTimeMillis(),
                    ativo = true,
                    jogadoresIds = emptyList(),
                    usuarioId = "local",
                    compartilhado = false
                )

                // Log para debug: antes de salvar no repositório
                println("DEBUG: Objeto grupo criado: $grupo")

                // Salvar no repositório
                if (grupoAtual != null) {
                    println("DEBUG: Atualizando grupo existente com ID: ${grupoAtual.id}")
                    grupoPeladaRepository.atualizarGrupo(grupo)
                } else {
                    println("DEBUG: Inserindo novo grupo")
                    val novoId = grupoPeladaRepository.inserirGrupo(grupo)
                    println("DEBUG: Novo grupo inserido com ID: $novoId")
                }

                // Fechar o diálogo primeiro
                _mostrarDialogo.value = false
                _grupoEmEdicao.value = null

                // Recarregar a lista após um pequeno delay para garantir que foi salvo
                kotlinx.coroutines.delay(500) // Aumentado o delay para garantir que a transação seja finalizada
                println("DEBUG: Recarregando lista de grupos após salvar")
                carregarGrupos()

            } catch (e: Exception) {
                // Tratar erro de salvamento
                e.printStackTrace()
                // Log detalhado do erro para debug
                println("ERRO ao salvar grupo: ${e.message}")
                println("ERRO detalhado: ${e.stackTraceToString()}")
            }
        }
    }

    /**
     * Exclui um grupo de pelada
     */
    fun excluirGrupo(grupo: GrupoPelada) {
        viewModelScope.launch {
            try {
                grupoPeladaRepository.excluirGrupo(grupo)
                // Recarregar a lista
                carregarGrupos()
            } catch (e: Exception) {
                // Tratar erro de exclusão
                e.printStackTrace()
            }
        }
    }

    /**
     * Exibe o seletor de mapas
     */
    fun mostrarSeletorMapas() {
        _mostrarSeletorMapas.value = true
    }

    /**
     * Oculta o seletor de mapas
     */
    fun ocultarSeletorMapas() {
        _mostrarSeletorMapas.value = false
    }

    /**
     * Processa a localização selecionada no mapa
     */
    fun processarLocalSelecionado(
        latitude: Double,
        longitude: Double,
        endereco: String,
        nome: String?
    ) {
        localSelecionadoLatitude = latitude
        localSelecionadoLongitude = longitude
        localSelecionadoEndereco = endereco
        localSelecionadoNome = nome

        _retornandoDoMapa.value = true
        _mostrarSeletorMapas.value = false
        _mostrarDialogo.value = true
    }

    /**
     * Cancela a seleção de localização no mapa
     */
    fun cancelarSelecaoLocalizacao() {
        _mostrarSeletorMapas.value = false
    }

    /**
     * Reseta o estado de retorno do mapa
     */
    fun resetRetornoMapa() {
        _retornandoDoMapa.value = false
    }

    /**
     * Obtém os dados de localização selecionados
     */
    fun obterDadosLocalizacao(): Map<String, Any?> {
        return mapOf(
            "latitude" to localSelecionadoLatitude,
            "longitude" to localSelecionadoLongitude,
            "endereco" to localSelecionadoEndereco,
            "nomeLocal" to localSelecionadoNome
        )
    }

    /**
     * Salva a localização selecionada no mapa
     */
    fun salvarLocalizacaoSelecionada(
        latitude: Double,
        longitude: Double,
        endereco: String,
        nome: String?
    ) {
        localSelecionadoLatitude = latitude
        localSelecionadoLongitude = longitude
        localSelecionadoEndereco = endereco
        localSelecionadoNome = nome

        _retornandoDoMapa.value = true
        _mostrarSeletorMapas.value = false
    }

    /**
     * Exibe a tela de compartilhamento para um grupo
     */
    fun exibirTelaCompartilhamento(grupo: GrupoPelada) {
        _grupoParaCompartilhar.value = grupo
        _mostrarTelaCompartilhamento.value = true
    }

    /**
     * Oculta a tela de compartilhamento
     */
    fun ocultarTelaCompartilhamento() {
        _mostrarTelaCompartilhamento.value = false
        _grupoParaCompartilhar.value = null
    }

    // Métodos para o sorteio rápido

    /**
     * Mostra o diálogo para escolher entre lista manual e pelada existente,
     * validando se há grupos de pelada criados
     */
    fun mostrarDialogSorteioRapido() {
        // Sempre exibe o diálogo, mas o conteúdo dele será adaptado
        // baseado na existência ou não de grupos
        _mostrarDialogSorteioRapido.value = true
    }

    /**
     * Navega diretamente para a tela de sorteio rápido com lista manual
     */
    fun navegarDiretamenteParaSorteioRapidoManual(): Boolean {
        // Se não há peladas criadas, retorna true para indicar que deve
        // navegar diretamente para a entrada manual
        return _grupos.value.isEmpty()
    }

    /**
     * Fecha o diálogo de sorteio rápido
     */
    fun fecharDialogSorteioRapido() {
        _mostrarDialogSorteioRapido.value = false
    }

    /**
     * Mostra o diálogo para selecionar uma pelada existente
     */
    fun mostrarDialogSelecaoPelada() {
        _mostrarDialogSelecaoPelada.value = true
        _peladaSelecionadaId.value = null // Limpa a seleção anterior
    }

    /**
     * Fecha o diálogo de seleção de pelada
     */
    fun fecharDialogSelecaoPelada() {
        _mostrarDialogSelecaoPelada.value = false
        _peladaSelecionadaId.value = null
    }

    /**
     * Seleciona uma pelada para o sorteio rápido
     */
    fun selecionarPelada(peladaId: Long) {
        _peladaSelecionadaId.value = peladaId
    }

    /**
     * Mostra o diálogo de opções ao clicar em uma pelada
     */
    fun mostrarDialogOpcoesPelada(peladaId: Long) {
        _peladaSelecionadaId.value = peladaId
        _mostrarDialogOpcoesPelada.value = true
    }

    /**
     * Fecha o diálogo de opções de pelada
     */
    fun fecharDialogOpcoesPelada() {
        _mostrarDialogOpcoesPelada.value = false
    }

    /**
     * Carrega o número de jogadores ativos por grupo
     */
    private fun carregarJogadoresPorGrupo() {
        viewModelScope.launch {
            try {
                // Agora vamos verificar jogadores específicos de cada grupo
                val jogadoresPorGrupoMap = mutableMapOf<Long, Int>()

                _grupos.value.forEach { grupo ->
                    val numJogadores = jogadorRepository.countJogadoresAtivosPorGrupo(grupo.id)
                    jogadoresPorGrupoMap[grupo.id] = numJogadores
                }

                _jogadoresPorGrupo.value = jogadoresPorGrupoMap
            } catch (e: Exception) {
                e.printStackTrace()
                // Em caso de erro, marca todos os grupos como sem jogadores
                val jogadoresPorGrupoMap = _grupos.value.associate { grupo ->
                    grupo.id to 0
                }
                _jogadoresPorGrupo.value = jogadoresPorGrupoMap
            }
        }
    }

    /**
     * Atualiza o mapeamento de jogadores quando os grupos são carregados
     */
    private fun atualizarJogadoresPorGrupo() {
        viewModelScope.launch {
            try {
                val jogadoresPorGrupoMap = mutableMapOf<Long, Int>()

                _grupos.value.forEach { grupo ->
                    val numJogadores = jogadorRepository.countJogadoresAtivosPorGrupo(grupo.id)
                    jogadoresPorGrupoMap[grupo.id] = numJogadores
                }

                _jogadoresPorGrupo.value = jogadoresPorGrupoMap
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Verifica se uma pelada tem jogadores cadastrados
     */
    fun peladaTemJogadores(peladaId: Long): Boolean {
        return (_jogadoresPorGrupo.value[peladaId] ?: 0) > 0
    }
}
