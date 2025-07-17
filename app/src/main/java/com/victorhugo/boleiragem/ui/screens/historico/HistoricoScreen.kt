package com.victorhugo.boleiragem.ui.screens.historico

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.victorhugo.boleiragem.data.model.HistoricoPelada
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoScreen(
    viewModel: HistoricoViewModel = hiltViewModel()
) {
    val historicoPartidas by viewModel.historicoPartidas.collectAsState(initial = emptyList())
    val ordenacaoAtual by viewModel.ordenacaoAtual.collectAsState()

    // Estado para controlar a exibição do diálogo de confirmação para exclusão do histórico
    var showConfirmacaoExclusao by remember { mutableStateOf(false) }

    // Diálogo de confirmação para excluir o histórico
    if (showConfirmacaoExclusao) {
        AlertDialog(
            onDismissRequest = { showConfirmacaoExclusao = false },
            title = { Text("Apagar Histórico") },
            text = {
                Text(
                    "Tem certeza que deseja apagar todo o histórico de peladas? " +
                    "Esta ação não pode ser desfeita."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.apagarHistorico()
                        showConfirmacaoExclusao = false
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
                    onClick = { showConfirmacaoExclusao = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Histórico de Peladas") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            ),
            actions = {
                // Botão de ordenação
                IconButton(
                    onClick = { viewModel.alternarOrdenacao() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Ordenar por data",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                // Botão para limpar o histórico
                IconButton(
                    onClick = { showConfirmacaoExclusao = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Limpar Histórico",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        )

        // Mostra a ordem de classificação atual
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ordenação: " +
                        if (ordenacaoAtual == OrdenacaoHistorico.MAIS_RECENTES)
                            "Mais recentes primeiro"
                        else
                            "Mais antigas primeiro",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (historicoPartidas.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Nenhuma pelada finalizada ainda.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Finalize uma pelada na tela 'Times Atuais' para registrar o histórico.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(historicoPartidas) { pelada ->
                        HistoricoPeladaCard(pelada = pelada)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoricoPeladaCard(pelada: HistoricoPelada) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dataFormatada = dateFormatter.format(Date(pelada.dataFinalizacao))

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Cabeçalho da pelada
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val peladaTitulo = "Pelada ${pelada.id}"
                Text(
                    text = peladaTitulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = dataFormatada,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Lista de times que participaram
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pelada.times.forEach { time ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = time.nome,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Sempre exibir o contador de vitórias
                                val vitoriasTxt = "${time.vitorias}V"
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = vitoriasTxt,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }

                                // Sempre exibir o contador de empates
                                val empatesTxt = "${time.empates}E"
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                ) {
                                    Text(
                                        text = empatesTxt,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }

                                // Sempre exibir o contador de derrotas
                                val derrotasTxt = "${time.derrotas}D"
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error
                                ) {
                                    Text(
                                        text = derrotasTxt,
                                        modifier = Modifier.padding(horizontal = 4.dp)
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
