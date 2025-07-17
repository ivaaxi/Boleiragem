package com.victorhugo.boleiragem.ui.screens.sorteio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.victorhugo.boleiragem.data.model.Jogador
import com.victorhugo.boleiragem.ui.common.Dimensions.standardButtonHeight
import com.victorhugo.boleiragem.ui.common.Dimensions.standardButtonWidth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SorteioTimesScreen(
    viewModel: SorteioTimesViewModel = hiltViewModel(),
    onSorteioRealizado: () -> Unit = {},
    onNavigateToHistorico: () -> Unit = {}
) {
    val jogadores by viewModel.jogadores.collectAsState(initial = emptyList())
    val configuracao by viewModel.configuracao.collectAsState(initial = null)
    val loading by viewModel.loading.collectAsState()
    val sorteioRealizado by viewModel.sorteioRealizado.collectAsState()
    val mostrarDialogConfirmacao by viewModel.mostrarDialogConfirmacao.collectAsState()

    LaunchedEffect(sorteioRealizado) {
        if (sorteioRealizado) {
            // Primeiro mostramos a tela de resultado
            onSorteioRealizado()
            // Depois navegamos para a aba de histórico (Times)
            onNavigateToHistorico()
            viewModel.resetSorteioRealizado()
        }
    }

    // Dialog de confirmação para novo sorteio
    if (mostrarDialogConfirmacao) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelarNovoSorteio() },
            title = { Text("Pelada em Andamento") },
            text = {
                Text(
                    "Existe uma pelada em andamento que ainda não foi registrada " +
                            "(vitórias/derrotas/empates ainda não foram contabilizados). " +
                            "\n\nSe continuar, o sorteio anterior será descartado e um novo será realizado. " +
                            "\n\nDeseja continuar mesmo assim?"
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmarNovoSorteio() }
                ) {
                    Text("Sim, fazer novo sorteio")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.cancelarNovoSorteio() }
                ) {
                    Text("Não, manter pelada atual")
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Sorteio de Times") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        Box(
            modifier = Modifier.weight(1f)
        ) {
            // Conteúdo principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Cabeçalho com informações sobre jogadores e times
                if (configuracao != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Configuração",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Times: ${configuracao!!.qtdTimes} | Jogadores por Time: ${configuracao!!.qtdJogadoresPorTime}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Jogadores selecionados: ${viewModel.jogadoresSelecionados.size} / ${jogadores.size}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Seletor de jogadores
                Text(
                    text = "Selecione os jogadores para o sorteio:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Botões para selecionar/desmarcar todos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.selecionarTodos() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Selecionar Todos")
                    }

                    OutlinedButton(
                        onClick = { viewModel.desmarcarTodos() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Desmarcar Todos")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Lista de jogadores com checkboxes
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 80.dp) // Espaço para o botão no final
                ) {
                    items(jogadores) { jogador ->
                        JogadorItemSelecao(
                            jogador = jogador,
                            selecionado = viewModel.jogadoresSelecionados.contains(jogador.id),
                            onSelecaoChange = { selecionado ->
                                viewModel.toggleJogadorSelecionado(jogador.id, selecionado)
                            }
                        )
                    }
                }
            }

            // Sobreposição de carregamento
            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .widthIn(max = 300.dp)
                            .wrapContentHeight()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(56.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Sorteando os times...",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Botão fixo na parte inferior
            Button(
                onClick = { viewModel.verificarESortearTimes() },
                enabled = viewModel.jogadoresSelecionados.isNotEmpty() && !loading,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .width(standardButtonWidth)
                    .height(standardButtonHeight)
            ) {
                Text("SORTEAR TIMES")
            }
        }
    }
}

@Composable
fun JogadorItemSelecao(
    jogador: Jogador,
    selecionado: Boolean,
    onSelecaoChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = jogador.nome,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${jogador.posicaoPrincipal.name} (${jogador.notaPosicaoPrincipal}⭐)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Checkbox(
                checked = selecionado,
                onCheckedChange = onSelecaoChange
            )
        }
    }
}
