package com.victorhugo.boleiragem.ui.screens.grupos

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewModule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.victorhugo.boleiragem.R
import com.victorhugo.boleiragem.data.model.DiaSemana
import com.victorhugo.boleiragem.data.model.GrupoPelada
import com.victorhugo.boleiragem.data.model.TipoRecorrencia
import com.victorhugo.boleiragem.ui.screens.compartilhar.CompartilharPeladaScreen
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GruposPeladaScreen(
    viewModel: GruposPeladaViewModel = hiltViewModel(),
    onGrupoSelecionado: (Long, String) -> Unit = { _, _ -> }, // Modificado para receber também o nome do grupo
    navController: NavController
) {
    val grupos by viewModel.grupos.collectAsState()
    val tipoVisualizacao by viewModel.tipoVisualizacao.collectAsState()
    val carregando by viewModel.carregando.collectAsState()
    val mostrarDialogo by viewModel.mostrarDialogo.collectAsState()
    val grupoEmEdicao by viewModel.grupoEmEdicao.collectAsState()

    // Em vez de usar o navController para navegar diretamente,
    // vamos usar a função onGrupoSelecionado para comunicar o ID e o nome do grupo selecionado
    val navegarParaDetalheGrupo: (GrupoPelada) -> Unit = { grupo ->
        // Use a função de callback em vez de navegar diretamente
        onGrupoSelecionado(grupo.id, grupo.nome)
    }

    // Função para compartilhar texto via Intent (não é @Composable)
    fun compartilharTexto(texto: String, context: Context) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, texto)
        }
        context.startActivity(Intent.createChooser(intent, "Compartilhar via"))
    }

    // Variáveis para evitar chamadas de collectAsState() diretamente nas condições
    val mostrarTelaCompartilhamento by viewModel.mostrarTelaCompartilhamento.collectAsState()
    val grupoParaCompartilhar by viewModel.grupoParaCompartilhar.collectAsState()

    // Para evitar smart cast em propriedade delegada
    val grupoParaCompartilharNaoNulo = grupoParaCompartilhar

    val context = LocalContext.current

    // Conteúdo principal
    if (mostrarTelaCompartilhamento && grupoParaCompartilharNaoNulo != null) {
        // Tela de compartilhamento
        CompartilharPeladaScreen(
            grupo = grupoParaCompartilharNaoNulo,
            onCompartilhar = { texto ->
                compartilharTexto(texto, context)
            },
            onVoltar = { viewModel.ocultarTelaCompartilhamento() }
        )
    } else {
        // Tela principal com os grupos
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Minhas Peladas") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    actions = {
                        // Botões para alternar entre os tipos de visualização
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            SegmentedButton(
                                selected = tipoVisualizacao == TipoVisualizacao.LISTA,
                                onClick = { viewModel.alterarTipoVisualizacao(TipoVisualizacao.LISTA) },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                                icon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.List,
                                        contentDescription = "Visualização em Lista"
                                    )
                                },
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = MaterialTheme.colorScheme.onPrimary,
                                    activeContentColor = MaterialTheme.colorScheme.primary,
                                    inactiveContainerColor = MaterialTheme.colorScheme.primary,
                                    inactiveContentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {}

                            SegmentedButton(
                                selected = tipoVisualizacao == TipoVisualizacao.CARDS,
                                onClick = { viewModel.alterarTipoVisualizacao(TipoVisualizacao.CARDS) },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.GridView,
                                        contentDescription = "Visualização em Cards"
                                    )
                                },
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = MaterialTheme.colorScheme.onPrimary,
                                    activeContentColor = MaterialTheme.colorScheme.primary,
                                    inactiveContainerColor = MaterialTheme.colorScheme.primary,
                                    inactiveContentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {}

                            SegmentedButton(
                                selected = tipoVisualizacao == TipoVisualizacao.MINIMALISTA,
                                onClick = { viewModel.alterarTipoVisualizacao(TipoVisualizacao.MINIMALISTA) },
                                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.ViewModule,
                                        contentDescription = "Visualização Minimalista"
                                    )
                                },
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = MaterialTheme.colorScheme.onPrimary,
                                    activeContentColor = MaterialTheme.colorScheme.primary,
                                    inactiveContainerColor = MaterialTheme.colorScheme.primary,
                                    inactiveContentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {}
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.mostrarDialogoCriarGrupo() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Adicionar Grupo")
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (carregando) {
                    // Mostrar indicador de carregamento
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (grupos.isEmpty()) {
                    // Mostrar mensagem quando não houver grupos
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_pelada_empty),
                            contentDescription = "Nenhuma pelada encontrada",
                            modifier = Modifier.size(120.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Nenhuma pelada encontrada",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Crie seu primeiro grupo de pelada clicando no botão +",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.mostrarDialogoCriarGrupo() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Adicionar"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CRIAR GRUPO")
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                } else {
                    // Mostrar os grupos de acordo com o tipo de visualização selecionado
                    when (tipoVisualizacao) {
                        TipoVisualizacao.LISTA -> ListaVisualizacao(
                            grupos = grupos,
                            onGrupoClick = navegarParaDetalheGrupo,
                            onEditarClick = { viewModel.mostrarDialogoEditarGrupo(it) },
                            onExcluirClick = { viewModel.excluirGrupo(it) },
                            onMostrarOpcoesPelada = { viewModel.mostrarDialogOpcoesPelada(it) }
                        )

                        TipoVisualizacao.CARDS -> CardsVisualizacao(
                            grupos = grupos,
                            onGrupoClick = navegarParaDetalheGrupo,
                            onEditarClick = { viewModel.mostrarDialogoEditarGrupo(it) },
                            onExcluirClick = { viewModel.excluirGrupo(it) },
                            onMostrarOpcoesPelada = { viewModel.mostrarDialogOpcoesPelada(it) }
                        )

                        TipoVisualizacao.MINIMALISTA -> MinimalistaVisualizacao(
                            grupos = grupos,
                            onGrupoClick = navegarParaDetalheGrupo,
                            onEditarClick = { viewModel.mostrarDialogoEditarGrupo(it) },
                            onExcluirClick = { viewModel.excluirGrupo(it) },
                            onMostrarOpcoesPelada = { viewModel.mostrarDialogOpcoesPelada(it) }
                        )
                    }
                }
            }
        }
    }
    
    // Diálogo para criação/edição de grupo
    if (mostrarDialogo) {
        DialogoAdicionarEditarGrupo(
            grupo = grupoEmEdicao,
            onDismissRequest = { viewModel.fecharDialogo() },
            onSalvar = { nome: String, local: String, horario: String, imagemUrl: String?, descricao: String?,
                tipoRecorrencia: TipoRecorrencia, diaSemana: DiaSemana? ->

                viewModel.salvarGrupo(
                    nome, local, horario, imagemUrl, descricao,
                    tipoRecorrencia, diaSemana, null, null,
                    null, null, emptyList()
                )
            }
        )
    }

    // Tela de compartilhamento (quando mostrarTelaCompartilhamento = true)
    if (mostrarTelaCompartilhamento && grupoParaCompartilharNaoNulo != null) {
        CompartilharPeladaScreen(
            grupo = grupoParaCompartilharNaoNulo,
            onCompartilhar = { texto ->
                compartilharTexto(texto, context)
                viewModel.ocultarTelaCompartilhamento()
            },
            onVoltar = { viewModel.ocultarTelaCompartilhamento() }
        )
    }
}

@Composable
fun ListaVisualizacao(
    grupos: List<GrupoPelada>,
    onGrupoClick: (GrupoPelada) -> Unit,
    onEditarClick: (GrupoPelada) -> Unit,
    onExcluirClick: (GrupoPelada) -> Unit,
    onMostrarOpcoesPelada: (Long) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(grupos) { grupo ->
            GrupoItemLista(
                grupo = grupo,
                onClick = { onGrupoClick(grupo) }, // Passa o objeto grupo completo
                onEditarClick = { onEditarClick(grupo) },
                onExcluirClick = { onExcluirClick(grupo) }
            )
        }
    }
}

@Composable
fun CardsVisualizacao(
    grupos: List<GrupoPelada>,
    onGrupoClick: (GrupoPelada) -> Unit,
    onEditarClick: (GrupoPelada) -> Unit,
    onExcluirClick: (GrupoPelada) -> Unit,
    onMostrarOpcoesPelada: (Long) -> Unit = {}
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(grupos) { grupo ->
            GrupoItemCard(
                grupo = grupo,
                onClick = { onGrupoClick(grupo) }, // Passa o objeto grupo completo
                onEditarClick = { onEditarClick(grupo) },
                onExcluirClick = { onExcluirClick(grupo) }
            )
        }
    }
}

@Composable
fun MinimalistaVisualizacao(
    grupos: List<GrupoPelada>,
    onGrupoClick: (GrupoPelada) -> Unit,
    onEditarClick: (GrupoPelada) -> Unit,
    onExcluirClick: (GrupoPelada) -> Unit,
    onMostrarOpcoesPelada: (Long) -> Unit = {}
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(grupos) { grupo ->
            GrupoItemMinimalista(
                grupo = grupo,
                onClick = { onGrupoClick(grupo) }, // Passa o objeto grupo completo
                onEditarClick = { onEditarClick(grupo) },
                onExcluirClick = { onExcluirClick(grupo) }
            )
        }
    }
}

@Composable
fun GrupoItemLista(
    grupo: GrupoPelada,
    onClick: () -> Unit,
    onEditarClick: () -> Unit,
    onExcluirClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagem do grupo
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                // Obter o ID do recurso com base no imagemUrl do grupo
                val resourceId = when(grupo.imagemUrl) {
                    "ic_pelada_default_1" -> R.drawable.ic_pelada_default_1
                    "ic_pelada_default_2" -> R.drawable.ic_pelada_default_2
                    "logo_boleiragem" -> R.drawable.logo_boleiragem
                    else -> R.drawable.ic_pelada_default_1
                }

                Image(
                    painter = painterResource(id = resourceId),
                    contentDescription = "Imagem do grupo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Informações do grupo
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = grupo.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Local",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = grupo.local,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Horário",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = try {
                            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                            val date = formatter.parse(grupo.horario)
                            if (date != null) {
                                if (grupo.tipoRecorrencia == TipoRecorrencia.RECORRENTE && grupo.diaSemana != null) {
                                    // Para grupos com dia fixo, mostra: Toda [dia da semana] às [horário]
                                    val diaSemanaStr = when (grupo.diaSemana) {
                                        DiaSemana.DOMINGO -> "domingo"
                                        DiaSemana.SEGUNDA -> "segunda-feira"
                                        DiaSemana.TERCA -> "terça-feira"
                                        DiaSemana.QUARTA -> "quarta-feira"
                                        DiaSemana.QUINTA -> "quinta-feira"
                                        DiaSemana.SEXTA -> "sexta-feira"
                                        DiaSemana.SABADO -> "sábado"
                                        else -> ""
                                    }
                                    val horario = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                                    "Toda $diaSemanaStr às $horario"
                                } else {
                                    // Para grupos sem dia fixo, mostra apenas o horário
                                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                                }
                            } else {
                                grupo.horario
                            }
                        } catch (_: Exception) {
                            grupo.horario
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Menu de opções
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Mais opções"
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = {
                            onEditarClick()
                            showMenu = false
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("Excluir") },
                        onClick = {
                            onExcluirClick()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GrupoItemCard(
    grupo: GrupoPelada,
    onClick: () -> Unit,
    onEditarClick: () -> Unit,
    onExcluirClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Imagem de capa
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                // Obter o ID do recurso com base no imagemUrl do grupo
                val resourceId = when(grupo.imagemUrl) {
                    "ic_pelada_default_1" -> R.drawable.ic_pelada_default_1
                    "ic_pelada_default_2" -> R.drawable.ic_pelada_default_2
                    "logo_boleiragem" -> R.drawable.logo_boleiragem
                    else -> R.drawable.ic_pelada_default_1
                }

                Image(
                    painter = painterResource(id = resourceId),
                    contentDescription = "Imagem do grupo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Menu de opções
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Mais opções",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = {
                                onEditarClick()
                                showMenu = false
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Excluir") },
                            onClick = {
                                onExcluirClick()
                                showMenu = false
                            }
                        )
                    }
                }
            }
            
            // Informações do grupo
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = grupo.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Local",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = grupo.local,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Horário",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = try {
                            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                            val date = formatter.parse(grupo.horario)
                            if (date != null) {
                                if (grupo.tipoRecorrencia == TipoRecorrencia.RECORRENTE && grupo.diaSemana != null) {
                                    // Para grupos com dia fixo, mostra: Toda [dia da semana] às [horário]
                                    val diaSemanaStr = when (grupo.diaSemana) {
                                        DiaSemana.DOMINGO -> "domingo"
                                        DiaSemana.SEGUNDA -> "segunda-feira"
                                        DiaSemana.TERCA -> "terça-feira"
                                        DiaSemana.QUARTA -> "quarta-feira"
                                        DiaSemana.QUINTA -> "quinta-feira"
                                        DiaSemana.SEXTA -> "sexta-feira"
                                        DiaSemana.SABADO -> "sábado"
                                        else -> ""
                                    }
                                    val horario = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                                    "Toda $diaSemanaStr às $horario"
                                } else {
                                    // Para grupos sem dia fixo, mostra apenas o horário
                                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                                }
                            } else {
                                grupo.horario
                            }
                        } catch (_: Exception) {
                            grupo.horario
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun GrupoItemMinimalista(
    grupo: GrupoPelada,
    onClick: () -> Unit,
    onEditarClick: () -> Unit,
    onExcluirClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box {
            // Imagem circular
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            ) {
                // Obter o ID do recurso com base no imagemUrl do grupo
                val resourceId = when(grupo.imagemUrl) {
                    "ic_pelada_default_1" -> R.drawable.ic_pelada_default_1
                    "ic_pelada_default_2" -> R.drawable.ic_pelada_default_2
                    "logo_boleiragem" -> R.drawable.logo_boleiragem
                    else -> R.drawable.ic_pelada_default_1
                }

                Image(
                    painter = painterResource(id = resourceId),
                    contentDescription = "Imagem do grupo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Botão de editar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            showMenu = true
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = {
                            onEditarClick()
                            showMenu = false
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Excluir") },
                        onClick = {
                            onExcluirClick()
                            showMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nome do grupo
        Text(
            text = grupo.nome,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(100.dp)
        )

        // Horário resumido
        Text(
            text = try {
                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = formatter.parse(grupo.horario)
                if (date != null) {
                    if (grupo.tipoRecorrencia == TipoRecorrencia.RECORRENTE && grupo.diaSemana != null) {
                        // Para grupos com dia fixo, mostra: Toda [dia da semana] às [horário]
                        val diaSemanaStr = when (grupo.diaSemana) {
                            DiaSemana.DOMINGO -> "domingo"
                            DiaSemana.SEGUNDA -> "segunda-feira"
                            DiaSemana.TERCA -> "terça-feira"
                            DiaSemana.QUARTA -> "quarta-feira"
                            DiaSemana.QUINTA -> "quinta-feira"
                            DiaSemana.SEXTA -> "sexta-feira"
                            DiaSemana.SABADO -> "sábado"
                            else -> ""
                        }
                        val horario = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                        "Toda $diaSemanaStr às $horario"
                    } else {
                        // Para grupos sem dia fixo, mostra apenas o horário
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                    }
                } else {
                    grupo.horario
                }
            } catch (_: Exception) {
                grupo.horario
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(100.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoAdicionarEditarGrupo(
    grupo: GrupoPelada?,
    onDismissRequest: () -> Unit,
    onSalvar: (nome: String, local: String, horario: String, imagemUrl: String?, descricao: String?,
              tipoRecorrencia: TipoRecorrencia, diaSemana: DiaSemana?) -> Unit
) {
    val titulo = if (grupo == null) "Novo Grupo de Pelada" else "Editar Grupo"

    // Estados para os campos do formulário
    var nome by remember { mutableStateOf(grupo?.nome ?: "") }
    var local by remember { mutableStateOf(grupo?.local ?: "") }

    // Estado para o TimePicker
    val timePickerState = rememberTimePickerState(
        initialHour = if (grupo != null) {
            try {
                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = formatter.parse(grupo.horario)
                if (date != null) {
                    SimpleDateFormat("HH", Locale.getDefault()).format(date).toInt()
                } else {
                    19 // Hora padrão
                }
            } catch (_: Exception) {
                19 // Hora padrão
            }
        } else {
            19 // Hora padrão
        },
        initialMinute = if (grupo != null) {
            try {
                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = formatter.parse(grupo.horario)
                if (date != null) {
                    SimpleDateFormat("mm", Locale.getDefault()).format(date).toInt()
                } else {
                    0 // Minuto padrão
                }
            } catch (_: Exception) {
                0 // Minuto padrão
            }
        } else {
            0 // Minuto padrão
        }
    )

    // Estado para controlar se o TimePicker está visível
    var mostrarTimePicker by remember { mutableStateOf(false) }

    // Estado para controlar o tipo de layout do TimePicker (true = relógio, false = input)
    var usarModoRelogio by remember { mutableStateOf(true) }

    var descricao by remember { mutableStateOf(grupo?.descricao ?: "") }

    // Estado para dia fixo
    var diaFixo by remember { mutableStateOf(grupo?.tipoRecorrencia == TipoRecorrencia.RECORRENTE) }

    // Estado para o dia da semana
    var diaSemana by remember { mutableStateOf(grupo?.diaSemana ?: DiaSemana.DOMINGO) }

    // Estado para a imagem selecionada
    var imagemSelecionada by remember { mutableStateOf(grupo?.imagemUrl ?: "ic_pelada_default_1") }

    // Dialog do TimePicker
    if (mostrarTimePicker) {
        AlertDialog(
            onDismissRequest = { mostrarTimePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.9f),
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Selecione o horário",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Controles para alternar entre modos Dial e Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ToggleButton(
                            selected = usarModoRelogio,
                            onClick = { usarModoRelogio = true },
                            content = { Text("Relógio") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ToggleButton(
                            selected = !usarModoRelogio,
                            onClick = { usarModoRelogio = false },
                            content = { Text("Teclado") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // TimePicker com o layout selecionado
                    if (usarModoRelogio) {
                        // Modo relógio
                        TimePicker(
                            state = timePickerState,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        // Modo entrada numérica usando o TimeInput conforme documentação do Android
                        TimeInput(
                            state = timePickerState,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botões de ação
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { mostrarTimePicker = false }) {
                            Text("Cancelar")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(onClick = {
                            // Confirmar horário e fechar dialog
                            mostrarTimePicker = false
                        }) {
                            Text("OK")
                        }
                    }
                }
            }
        )
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(500.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Colocando todo o conteúdo em uma coluna com scroll
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Seletor de imagem
                    Text(
                        text = "Selecione uma imagem:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Grade de opções de imagens
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("ic_pelada_default_1", "ic_pelada_default_2", "logo_boleiragem").forEach { imagem ->
                            ImagemOpcao(
                                imagemResource = imagem,
                                selecionada = imagemSelecionada == imagem,
                                onClick = { imagemSelecionada = imagem }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Nome
                    OutlinedTextField(
                        value = nome,
                        onValueChange = { nome = it },
                        label = { Text("Nome do Grupo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Campo Local
                    OutlinedTextField(
                        value = local,
                        onValueChange = { local = it },
                        label = { Text("Local da Pelada") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Opção de Dia Fixo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = diaFixo,
                            onCheckedChange = { diaFixo = it }
                        )
                        Text("Marcar Dia Fixo?")
                    }

                    // Seleção de dia da semana na horizontal (visível apenas quando diaFixo é true)
                    if (diaFixo) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Selecione o dia da semana:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Dias da semana na horizontal
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Mapeamento dos dias da semana para siglas
                            val diasSiglas = listOf(
                                DiaSemana.DOMINGO to "D",
                                DiaSemana.SEGUNDA to "S",
                                DiaSemana.TERCA to "T",
                                DiaSemana.QUARTA to "Q",
                                DiaSemana.QUINTA to "Q",
                                DiaSemana.SEXTA to "S",
                                DiaSemana.SABADO to "S"
                            )

                            diasSiglas.forEach { (dia, sigla) ->
                                DiaSemanaOpcao(
                                    sigla = sigla,
                                    selecionado = diaSemana == dia,
                                    onClick = { diaSemana = dia }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Seletor de Horário com TimePicker nativo do Material Design 3
                    Text(
                        text = "Horário da Pelada:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // TimePicker do Material Design 3
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { mostrarTimePicker = true },  // Torna o card clicável para abrir o TimePicker
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Exibir horário selecionado
                            Text(
                                text = "%02d:%02d".format(timePickerState.hour, timePickerState.minute),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Campo Descrição
                    OutlinedTextField(
                        value = descricao,
                        onValueChange = { descricao = it },
                        label = { Text("Descrição (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botões
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest
                    ) {
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            // Converte diaFixo para TipoRecorrencia
                            val tipoRecorrencia = if (diaFixo) TipoRecorrencia.RECORRENTE else TipoRecorrencia.ESPORADICA

                            // Formata o horário para o formato esperado pelo backend
                            val dataAtual = java.time.LocalDate.now()
                            val horarioCompleto = "$dataAtual" + "T" + "%02d:%02d:00".format(timePickerState.hour, timePickerState.minute)

                            onSalvar(
                                nome,
                                local,
                                horarioCompleto,
                                imagemSelecionada,
                                descricao,
                                tipoRecorrencia,
                                if (diaFixo) diaSemana else null
                            )
                        },
                        enabled = nome.isNotBlank() && local.isNotBlank()
                    ) {
                        Text("Salvar")
                    }
                }
            }
        }
    }
}

@Composable
fun ToggleButton(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
        color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun HorarioSelector(
    value: Int,
    range: IntRange,
    step: Int = 1,
    onValueChange: (Int) -> Unit,
    formatValue: (Int) -> String = { it.toString() }
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Botão para aumentar
        IconButton(
            onClick = {
                val nextValue = (value + step).coerceAtMost(range.last)
                onValueChange(nextValue)
            }
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Aumentar",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Valor atual
        Card(
            modifier = Modifier
                .width(60.dp)
                .height(48.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatValue(value),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Botão para diminuir
        IconButton(
            onClick = {
                val prevValue = (value - step).coerceAtLeast(range.first)
                onValueChange(prevValue)
            }
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Diminuir",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.rotate(180f) // Rotaciona o ícone + para virar -
            )
        }
    }
}

// Componente para exibir cada dia da semana como uma opção clicável
@Composable
fun DiaSemanaOpcao(
    sigla: String,
    selecionado: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                if (selecionado) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = 1.dp,
                color = if (selecionado) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = sigla,
            color = if (selecionado) MaterialTheme.colorScheme.onPrimary
                   else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun ImagemOpcao(
    imagemResource: String,
    selecionada: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .padding(4.dp)
    ) {
        // Obter o ID do recurso a partir do nome
        val resourceId = when(imagemResource) {
            "ic_pelada_default_1" -> R.drawable.ic_pelada_default_1
            "ic_pelada_default_2" -> R.drawable.ic_pelada_default_2
            "logo_boleiragem" -> R.drawable.logo_boleiragem
            else -> R.drawable.ic_pelada_default_1
        }

        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onClick)
                .border(
                    width = if (selecionada) 2.dp else 0.dp,
                    color = if (selecionada) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Image(
                painter = painterResource(id = resourceId),
                contentDescription = "Imagem $imagemResource",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        if (selecionada) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.BottomEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selecionada",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}
