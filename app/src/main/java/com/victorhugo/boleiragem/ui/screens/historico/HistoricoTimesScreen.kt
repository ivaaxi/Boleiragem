package com.victorhugo.boleiragem.ui.screens.historico

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.victorhugo.boleiragem.data.model.HistoricoTime
import com.victorhugo.boleiragem.data.model.Time
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoTimesScreen(
    viewModel: HistoricoTimesViewModel = hiltViewModel()
) {
    val historicoTimes by viewModel.historicoTimes.collectAsState(initial = emptyList())
    val peladaFinalizada by viewModel.peladaFinalizada.collectAsState()

    // Estado para controlar se o diálogo de confirmação está sendo mostrado
    var showDialogConfirmacao by remember { mutableStateOf(false) }

    // Estado para controlar se o diálogo de confirmação para apagar está sendo mostrado
    var showDialogApagar by remember { mutableStateOf(false) }

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
    if (showDialogApagar) {
        AlertDialog(
            onDismissRequest = { showDialogApagar = false },
            title = { Text("Apagar Pelada") },
            text = {
                Text(
                    "Tem certeza que deseja apagar esta pelada? " +
                    "Todos os times e resultados serão perdidos e não poderão ser recuperados."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.apagarPeladaAtual()
                        showDialogApagar = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Apagar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDialogApagar = false }
                ) {
                    Text("Cancelar")
                }
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
            title = { Text("Histórico de Times") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp), // Espaço para não ficar abaixo do botão
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(historicoTimes) { time ->
                        TimeHistoricoCard(
                            time = time,
                            onVitoriaClick = { viewModel.adicionarVitoria(time) },
                            onDerrotaClick = { viewModel.adicionarDerrota(time) },
                            onEmpateClick = { viewModel.adicionarEmpate(time) },
                            onDiminuirVitoriaClick = { viewModel.diminuirVitoria(time) },
                            onDiminuirDerrotaClick = { viewModel.diminuirDerrota(time) },
                            onDiminuirEmpateClick = { viewModel.diminuirEmpate(time) }
                        )
                    }
                }

                // Botão fixo na parte inferior
                if (historicoTimes.isNotEmpty()) {
                    Button(
                        onClick = { showDialogConfirmacao = true },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                            .height(56.dp),
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
                            "FINALIZAR PELADA",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
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
fun TimeHistoricoCard(
    time: HistoricoTime,
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

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Cabeçalho do time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = time.nome,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Último sorteio: $dataFormatada",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Adiciona informações sobre médias do time
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
                        text = "Média: ${String.format("%.1f", time.mediaEstrelas)}★",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "Pontuação: ${String.format("%.0f", time.mediaPontuacao)}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Estatísticas do time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EstatisticaItem(
                    label = "Vitórias",
                    valor = time.vitorias,
                    color = Color(0xFF4CAF50)
                )

                EstatisticaItem(
                    label = "Empates",
                    valor = time.empates,
                    color = Color(0xFFFFC107)
                )

                EstatisticaItem(
                    label = "Derrotas",
                    valor = time.derrotas,
                    color = Color(0xFFF44336)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botões para registrar resultado - agora em pares (adicionar/diminuir)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Vitória
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onVitoriaClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF4CAF50)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Adicionar vitória"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Vitória",
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }

                    OutlinedButton(
                        onClick = onDiminuirVitoriaClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF4CAF50)
                        ),
                        enabled = time.vitorias > 0,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Diminuir vitória"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Vitória",
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Empate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEmpateClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFFC107)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Adicionar empate"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Empate",
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }

                    OutlinedButton(
                        onClick = onDiminuirEmpateClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFFC107)
                        ),
                        enabled = time.empates > 0,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Diminuir empate"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Empate",
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Derrota
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDerrotaClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFF44336)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Adicionar derrota"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Derrota",
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }

                    OutlinedButton(
                        onClick = onDiminuirDerrotaClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFF44336)
                        ),
                        enabled = time.derrotas > 0,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Diminuir derrota"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Derrota",
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EstatisticaItem(
    label: String,
    valor: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = valor.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
