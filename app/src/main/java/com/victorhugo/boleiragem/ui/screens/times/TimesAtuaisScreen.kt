package com.victorhugo.boleiragem.ui.screens.times

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.victorhugo.boleiragem.data.model.HistoricoTime
import com.victorhugo.boleiragem.ui.common.Dimensions
import com.victorhugo.boleiragem.ui.screens.historico.HistoricoTimesViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimesAtuaisScreen(
    viewModel: HistoricoTimesViewModel = hiltViewModel()
) {
    val historicoTimes by viewModel.historicoTimes.collectAsState(initial = emptyList())
    val peladaFinalizada by viewModel.peladaFinalizada.collectAsState()
    val jogadoresPorTime by viewModel.jogadoresPorTime.collectAsState()
    val mostrarDialogoTransferencia by viewModel.mostrarDialogoTransferencia.collectAsState()
    val mostrarComponenteConfronto by viewModel.mostrarComponenteConfronto.collectAsState()

    // Estado para controlar se o diálogo de confirmação está sendo mostrado
    var showDialogConfirmacao by remember { mutableStateOf(false) }

    // Estado para controlar se o diálogo de confirmação para apagar está sendo mostrado
    var showDialogCancelar by remember { mutableStateOf(false) }

    // Diálogo de confirmação para finalizar a pelada
    if (showDialogConfirmacao) {
        AlertDialog(
            onDismissRequest = { showDialogConfirmacao = false },
            title = { Text("Finalizar Pelada") },
            text = {
                Text(
                    "Tem certeza que deseja finalizar a pelada? " +
                            "As estatísticas dos jogadores serão atualizadas com os resultados atuais " +
                            "e você não poderá mais editar os resultados desse sorteio."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.finalizarPelada()
                        showDialogConfirmacao = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDialogConfirmacao = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de confirmação para apagar a pelada
    if (showDialogCancelar) {
        AlertDialog(
            onDismissRequest = { showDialogCancelar = false },
            title = { Text("Cancelar Pelada") },
            text = {
                Text(
                    "Tem certeza que deseja cancelar esta pelada? " +
                            "Todos os times e resultados serão perdidos e não poderão ser recuperados."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelarPeladaAtual()
                        showDialogCancelar = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cancelar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDialogCancelar = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo para transferência de jogadores entre times
    if (mostrarDialogoTransferencia) {
        TransferenciaJogadorDialog(
            times = historicoTimes,
            jogadoresPorTime = jogadoresPorTime,
            onDismiss = { viewModel.fecharDialogoTransferencia() },
            onTransferirJogador = { jogador, timeOrigemId, timeDestinoId ->
                viewModel.transferirJogador(jogador, timeOrigemId, timeDestinoId)
            }
        )
    }

    // SnackBar para confirmar finalização da pelada
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(peladaFinalizada) {
        if (peladaFinalizada) {
            snackbarHostState.showSnackbar(
                message = "Pelada finalizada com sucesso!",
                duration = SnackbarDuration.Short
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Pelada Atual") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            ),
            actions = {
                // Botão para exibir o seletor de confrontos
                if (historicoTimes.size > 1) {
                    IconButton(onClick = { viewModel.mostrarComponenteConfronto() }) {
                        Icon(
                            imageVector = Icons.Default.Sports,
                            contentDescription = "Confronto entre Times",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    // Botão para transferir jogadores entre times
                    IconButton(onClick = { viewModel.mostrarDialogoTransferencia() }) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Transferir Jogadores",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (historicoTimes.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Nenhum time sorteado ainda.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Faça um sorteio na aba 'Sorteio' para começar a registrar o histórico dos times.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Exibe o componente de confronto se estiver ativado
                if (mostrarComponenteConfronto) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        ConfrontoTimesComponent(
                            times = historicoTimes,
                            onFinalizarConfronto = { timeA, timeB, resultado ->
                                viewModel.finalizarConfronto(timeA, timeB, resultado)
                            },
                            onCancelarConfronto = { viewModel.ocultarComponenteConfronto() }
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentPadding = PaddingValues(bottom = 140.dp), // Aumentando o padding inferior para não esconder o último item
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(historicoTimes) { time ->
                            TimeCard(
                                time = time,
                                jogadores = jogadoresPorTime[time.id] ?: emptyList(),
                                onVitoriaClick = { viewModel.adicionarVitoria(time) },
                                onDerrotaClick = { viewModel.adicionarDerrota(time) },
                                onEmpateClick = { viewModel.adicionarEmpate(time) },
                                onDiminuirVitoriaClick = { viewModel.diminuirVitoria(time) },
                                onDiminuirDerrotaClick = { viewModel.diminuirDerrota(time) },
                                onDiminuirEmpateClick = { viewModel.diminuirEmpate(time) }
                            )
                        }
                    }
                }

                // Botões fixos na parte inferior
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Só mostra os botões quando o componente de confronto não estiver visível
                    if (!mostrarComponenteConfronto) {
                        // Usando as constantes globais de tamanho de botão

                        // Row para alinhar os botões lado a lado
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Botão para cancelar a pelada
                            Button(
                                onClick = { showDialogCancelar = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(Dimensions.standardButtonHeight),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancelar Pelada",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onError
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "CANCELAR",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            // Botão para finalizar a pelada
                            Button(
                                onClick = { showDialogConfirmacao = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(Dimensions.standardButtonHeight),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Finalizar Pelada",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "FINALIZAR",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Host do Snackbar
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun TimeCard(
    time: HistoricoTime,
    jogadores: List<com.victorhugo.boleiragem.data.model.Jogador>,
    onVitoriaClick: () -> Unit,
    onDerrotaClick: () -> Unit,
    onEmpateClick: () -> Unit,
    onDiminuirVitoriaClick: () -> Unit = {},
    onDiminuirDerrotaClick: () -> Unit = {},
    onDiminuirEmpateClick: () -> Unit = {}
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val dataFormatada = remember(time.dataUltimoSorteio) {
        dateFormatter.format(Date(time.dataUltimoSorteio))
    }

    // Estado para controlar se o card está expandido
    var expandido by remember { mutableStateOf(false) }

    // Estado para controlar se o modo de edição está ativado
    var modoEdicao by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Cabeçalho do time - clicável para expandir/contrair
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandido = !expandido },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = time.nome,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = dataFormatada,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Informações sobre médias do time - sempre visíveis
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Estrelas: ${String.format("%.1f", time.mediaEstrelas)}★",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Pontuação",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Pontuação: ${String.format("%.1f", time.mediaPontuacao)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Estatísticas com botão de edição
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botão de edição
                IconButton(
                    onClick = { modoEdicao = !modoEdicao }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = if (modoEdicao) "Desativar Edição" else "Ativar Edição",
                        tint = if (modoEdicao) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Container para as estatísticas
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Vitórias
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Vitórias",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = onDiminuirVitoriaClick,
                                enabled = modoEdicao && time.vitorias > 0
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Diminuir Vitória",
                                    modifier = Modifier.size(24.dp),
                                    tint = if (modoEdicao && time.vitorias > 0)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                            Text(
                                text = time.vitorias.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = onVitoriaClick,
                                enabled = modoEdicao
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Adicionar Vitória",
                                    modifier = Modifier.size(24.dp),
                                    tint = if (modoEdicao)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }

                    // Empates
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Empates",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = onDiminuirEmpateClick,
                                enabled = modoEdicao && time.empates > 0
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Diminuir Empate",
                                    modifier = Modifier.size(24.dp),
                                    tint = if (modoEdicao && time.empates > 0)
                                        MaterialTheme.colorScheme.tertiary
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                            Text(
                                text = time.empates.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            IconButton(
                                onClick = onEmpateClick,
                                enabled = modoEdicao
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Adicionar Empate",
                                    modifier = Modifier.size(24.dp),
                                    tint = if (modoEdicao)
                                        MaterialTheme.colorScheme.tertiary
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }

                    // Derrotas
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Derrotas",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = onDiminuirDerrotaClick,
                                enabled = modoEdicao && time.derrotas > 0
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Diminuir Derrota",
                                    modifier = Modifier.size(24.dp),
                                    tint = if (modoEdicao && time.derrotas > 0)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                            Text(
                                text = time.derrotas.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            IconButton(
                                onClick = onDerrotaClick,
                                enabled = modoEdicao
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Adicionar Derrota",
                                    modifier = Modifier.size(24.dp),
                                    tint = if (modoEdicao)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }

            // Seção expansível - Lista de jogadores
            AnimatedVisibility(visible = expandido) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Divider(modifier = Modifier.padding(bottom = 8.dp))

                    Text(
                        text = "Jogadores",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Lista de jogadores
                    jogadores.forEach { jogador ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = jogador.nome,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${jogador.notaPosicaoPrincipal}★",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = "Pontuação",
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = "${jogador.pontuacaoTotal}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
