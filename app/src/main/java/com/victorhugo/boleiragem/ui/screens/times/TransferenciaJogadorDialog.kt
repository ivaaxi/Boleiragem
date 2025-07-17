package com.victorhugo.boleiragem.ui.screens.times

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.victorhugo.boleiragem.data.model.HistoricoTime
import com.victorhugo.boleiragem.data.model.Jogador
import com.victorhugo.boleiragem.ui.common.Dimensions

@Composable
fun TransferenciaJogadorDialog(
    times: List<HistoricoTime>,
    jogadoresPorTime: Map<Long, List<Jogador>>,
    onDismiss: () -> Unit,
    onTransferirJogador: (jogador: Jogador, timeOrigemId: Long, timeDestinoId: Long) -> Unit
) {
    var timeOrigemSelecionado by remember { mutableStateOf<HistoricoTime?>(null) }
    var timeDestinoSelecionado by remember { mutableStateOf<HistoricoTime?>(null) }
    var jogadorSelecionado by remember { mutableStateOf<Jogador?>(null) }

    // Estado para controlar se o diálogo de confirmação deve ser mostrado
    var mostrarConfirmacao by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Título do diálogo
                Text(
                    text = "Transferir Jogador",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Passo 1: Selecionar time de origem
                Text(
                    text = "1. Selecione o time de origem:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(0.3f)
                        .fillMaxWidth()
                ) {
                    items(times) { time ->
                        TimeSelectionItem(
                            time = time,
                            selected = time == timeOrigemSelecionado,
                            onClick = {
                                timeOrigemSelecionado = time
                                jogadorSelecionado = null // Reset jogador selecionado
                            }
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Passo 2: Selecionar jogador (apenas habilitado se um time de origem foi selecionado)
                Text(
                    text = "2. Selecione o jogador para transferir:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (timeOrigemSelecionado != null) {
                    val jogadores = jogadoresPorTime[timeOrigemSelecionado!!.id] ?: emptyList()

                    if (jogadores.isEmpty()) {
                        Text(
                            text = "Nenhum jogador disponível neste time.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(0.3f)
                                .fillMaxWidth()
                        ) {
                            items(jogadores) { jogador ->
                                JogadorSelectionItem(
                                    jogador = jogador,
                                    selected = jogador == jogadorSelecionado,
                                    onClick = { jogadorSelecionado = jogador }
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(0.3f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Selecione um time de origem primeiro",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Passo 3: Selecionar time de destino (apenas habilitado se um jogador foi selecionado)
                Text(
                    text = "3. Selecione o time de destino:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (jogadorSelecionado != null) {
                    val timesDestino = times.filter { it.id != timeOrigemSelecionado?.id }

                    if (timesDestino.isEmpty()) {
                        Text(
                            text = "Nenhum outro time disponível para transferência.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(0.3f)
                                .fillMaxWidth()
                        ) {
                            items(timesDestino) { time ->
                                TimeSelectionItem(
                                    time = time,
                                    selected = time == timeDestinoSelecionado,
                                    onClick = { timeDestinoSelecionado = time }
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(0.3f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Selecione um jogador primeiro",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botões de ação
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            if (timeOrigemSelecionado != null &&
                                jogadorSelecionado != null &&
                                timeDestinoSelecionado != null) {

                                if (timeOrigemSelecionado!!.vitorias > 0 ||
                                    timeOrigemSelecionado!!.empates > 0 ||
                                    timeOrigemSelecionado!!.derrotas > 0) {

                                    // Mostrar diálogo de confirmação
                                    mostrarConfirmacao = true
                                } else {
                                    // Transferir sem confirmação
                                    onTransferirJogador(
                                        jogadorSelecionado!!,
                                        timeOrigemSelecionado!!.id,
                                        timeDestinoSelecionado!!.id
                                    )
                                    onDismiss()
                                }
                            }
                        },
                        enabled = timeOrigemSelecionado != null &&
                                  jogadorSelecionado != null &&
                                  timeDestinoSelecionado != null,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .height(Dimensions.standardButtonHeight)
                    ) {
                        Text("Transferir")
                    }
                }
            }
        }
    }

    // Diálogo de confirmação para transferência com estatísticas
    if (mostrarConfirmacao) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacao = false },
            title = { Text("Atenção") },
            text = {
                Text(
                    "Este time já possui estatísticas registradas (vitórias, empates ou derrotas). " +
                    "Ao transferir um jogador, as estatísticas atuais serão salvas para todos os jogadores do time " +
                    "e os contadores serão zerados para o novo time formado. Deseja continuar?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Realizar a transferência
                        onTransferirJogador(
                            jogadorSelecionado!!,
                            timeOrigemSelecionado!!.id,
                            timeDestinoSelecionado!!.id
                        )
                        mostrarConfirmacao = false
                        onDismiss()
                    },
                    modifier = Modifier.height(Dimensions.standardButtonHeight)
                ) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { mostrarConfirmacao = false },
                    modifier = Modifier.height(Dimensions.standardButtonHeight)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun TimeSelectionItem(
    time: HistoricoTime,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        onClick = onClick,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = time.nome,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.weight(1f))

            val quantidadeJogadores = time.jogadoresIds.size.toString() + " jogadores"
            Text(
                text = quantidadeJogadores,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun JogadorSelectionItem(
    jogador: Jogador,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        onClick = onClick,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = jogador.nome,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = jogador.posicaoPrincipal.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
