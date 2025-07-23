package com.victorhugo.boleiragem.ui.screens.times

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.victorhugo.boleiragem.data.model.HistoricoTime
import com.victorhugo.boleiragem.data.model.Jogador

@Composable
fun TransferenciaJogadorDialog(
    times: List<HistoricoTime>,
    jogadoresPorTime: Map<Long, List<Jogador>>,
    modoSubstituicaoTimeReserva: Boolean,
    onDismiss: () -> Unit,
    onTransferirJogador: (jogador: Jogador, timeOrigemId: Long, timeDestinoId: Long) -> Unit,
    timeOrigemPreSelecionado: HistoricoTime? = null // NOVO PARÂMETRO
) {
    var timeOrigemSelecionado by remember { mutableStateOf<HistoricoTime?>(timeOrigemPreSelecionado) }
    var timeDestinoSelecionado by remember { mutableStateOf<HistoricoTime?>(null) }
    var jogadorOrigemSelecionado by remember { mutableStateOf<Jogador?>(null) }
    var jogadorDestinoSelecionado by remember { mutableStateOf<Jogador?>(null) }

    // Verificar se existe um time reserva
    val timeReserva = remember(times) { times.find { it.ehTimeReserva } }

    // Configurar times disponíveis baseado no modo
    val timesOrigem = remember(times, modoSubstituicaoTimeReserva) {
        if (modoSubstituicaoTimeReserva && timeReserva != null) {
            listOf(timeReserva) // Apenas time reserva
        } else {
            times // Todos os times
        }
    }

    val timesDestino = remember(times, timeOrigemSelecionado) {
        times.filter { time ->
            time.id != timeOrigemSelecionado?.id && !time.ehTimeReserva
        }
    }

    // Auto-selecionar time reserva se em modo substituição
    LaunchedEffect(modoSubstituicaoTimeReserva, timeReserva) {
        if (modoSubstituicaoTimeReserva && timeReserva != null) {
            timeOrigemSelecionado = timeReserva
        }
    }

    // Estado para controlar o diálogo de confirmação
    var mostrarConfirmacao by remember { mutableStateOf(false) }

    // Fullscreen Dialog
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // TopBar customizada
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (modoSubstituicaoTimeReserva) "Substituição" else "Transferência de Jogadores",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                // Conteúdo principal
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Instruções
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = if (modoSubstituicaoTimeReserva)
                                    "Selecione um jogador do time reserva, depois escolha o time de destino e finalmente um jogador para fazer a substituição."
                                else
                                    "Escolha o time de origem, selecione o jogador, depois o time de destino e o jogador para substituição.",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // 1. Seleção do time de origem
                    item {
                        TimeSelectionSection(
                            title = "1. Time de Origem",
                            times = timesOrigem,
                            selectedTimeId = timeOrigemSelecionado?.id,
                            jogadoresPorTime = jogadoresPorTime,
                            onTimeSelected = { time ->
                                timeOrigemSelecionado = time
                                jogadorOrigemSelecionado = null
                                timeDestinoSelecionado = null
                                jogadorDestinoSelecionado = null
                            },
                            selectedJogadorId = jogadorOrigemSelecionado?.id,
                            onJogadorSelected = { jogador ->
                                jogadorOrigemSelecionado = jogador
                                timeDestinoSelecionado = null
                                jogadorDestinoSelecionado = null
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            onContainerColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    // 2. Seleção do time de destino
                    if (jogadorOrigemSelecionado != null) {
                        item {
                            TimeSelectionSection(
                                title = "2. Time de Destino",
                                times = timesDestino,
                                selectedTimeId = timeDestinoSelecionado?.id,
                                jogadoresPorTime = jogadoresPorTime,
                                onTimeSelected = { time ->
                                    timeDestinoSelecionado = time
                                    jogadorDestinoSelecionado = null
                                },
                                selectedJogadorId = jogadorDestinoSelecionado?.id,
                                onJogadorSelected = { jogador ->
                                    jogadorDestinoSelecionado = jogador
                                },
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                onContainerColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                showJogadorSelection = true,
                                isJogadorSelectionRequired = true
                            )
                        }
                    }
                }

                // Botões de ação fixos na parte inferior
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = { mostrarConfirmacao = true },
                            enabled = timeOrigemSelecionado != null &&
                                     jogadorOrigemSelecionado != null &&
                                     timeDestinoSelecionado != null &&
                                     jogadorDestinoSelecionado != null, // OBRIGATÓRIO escolher jogador destino
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (modoSubstituicaoTimeReserva) "Substituir" else "Transferir")
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmação
    if (mostrarConfirmacao) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacao = false },
            title = { Text("Confirmar ${if (modoSubstituicaoTimeReserva) "Substituição" else "Transferência"}") },
            text = {
                Text(
                    "${if (modoSubstituicaoTimeReserva) "Substituir" else "Transferir"} " +
                    "${jogadorOrigemSelecionado?.nome} (${timeOrigemSelecionado?.nome}) " +
                    "↔ ${jogadorDestinoSelecionado?.nome} (${timeDestinoSelecionado?.nome})?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Realizar a troca dupla
                        onTransferirJogador(
                            jogadorOrigemSelecionado!!,
                            timeOrigemSelecionado!!.id,
                            timeDestinoSelecionado!!.id
                        )
                        // Troca reversa
                        onTransferirJogador(
                            jogadorDestinoSelecionado!!,
                            timeDestinoSelecionado!!.id,
                            timeOrigemSelecionado!!.id
                        )
                        mostrarConfirmacao = false
                        onDismiss()
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { mostrarConfirmacao = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun TimeSelectionSection(
    title: String,
    times: List<HistoricoTime>,
    selectedTimeId: Long?,
    jogadoresPorTime: Map<Long, List<Jogador>>,
    onTimeSelected: (HistoricoTime) -> Unit,
    selectedJogadorId: Long?,
    onJogadorSelected: (Jogador) -> Unit,
    containerColor: Color,
    onContainerColor: Color,
    showJogadorSelection: Boolean = true,
    isJogadorSelectionRequired: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = onContainerColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Se é modo time reserva e tem apenas 1 time (Time Reserva), não mostra seleção de times
            if (times.size == 1 && times.first().ehTimeReserva) {
                // Apenas mostra que o Time Reserva está selecionado, sem possibilidade de mudança
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 8.dp
                ) {
                    Text(
                        text = "Time Reserva (selecionado)",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                // Modo normal - mostra todos os times para seleção
                times.forEach { time ->
                    TimeSelectionCard(
                        time = time,
                        isSelected = selectedTimeId == time.id,
                        onClick = { onTimeSelected(time) }
                    )
                }
            }

            // Jogadores (se um time estiver selecionado)
            if (showJogadorSelection && selectedTimeId != null) {
                val jogadoresDoTime = jogadoresPorTime[selectedTimeId] ?: emptyList()

                if (jogadoresDoTime.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isJogadorSelectionRequired) "Escolha um jogador (obrigatório):" else "Escolha um jogador:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = onContainerColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    jogadoresDoTime.forEach { jogador ->
                        JogadorSelectionCard(
                            jogador = jogador,
                            isSelected = selectedJogadorId == jogador.id,
                            onClick = { onJogadorSelected(jogador) }
                        )
                    }
                }
            }
        }
    }
}

// Componentes auxiliares para melhor organização
@Composable
fun TimeSelectionCard(
    time: HistoricoTime,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 8.dp else 2.dp,
        onClick = onClick
    ) {
        Text(
            text = if (time.ehTimeReserva) "Time Reserva" else time.nome,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun JogadorSelectionCard(
    jogador: Jogador,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 8.dp else 2.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = jogador.nome,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${jogador.posicaoPrincipal.name} (${jogador.notaPosicaoPrincipal}⭐)",
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.8f)
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
