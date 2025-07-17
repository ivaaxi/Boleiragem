package com.victorhugo.boleiragem.ui.screens.times

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.victorhugo.boleiragem.data.model.HistoricoTime
import com.victorhugo.boleiragem.ui.common.Dimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfrontoTimesComponent(
    times: List<HistoricoTime>,
    onFinalizarConfronto: (HistoricoTime, HistoricoTime, ResultadoConfronto) -> Unit,
    onCancelarConfronto: () -> Unit
) {
    var timeSelecionado1 by remember { mutableStateOf<HistoricoTime?>(null) }
    var timeSelecionado2 by remember { mutableStateOf<HistoricoTime?>(null) }

    // Estado para controlar qual resultado está selecionado
    var resultadoSelecionado by remember { mutableStateOf<ResultadoConfronto?>(null) }

    // Estado para controlar se o diálogo de resultado está aberto
    var mostrarDialogoResultado by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Selecione os times para confronto",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Seleção do primeiro time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Time 1: ",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.width(60.dp)
                )

                // Dropdown para selecionar o primeiro time
                Box(modifier = Modifier.weight(1f)) {
                    var expanded1 by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded1,
                        onExpandedChange = { expanded1 = it }
                    ) {
                        TextField(
                            value = timeSelecionado1?.nome ?: "Selecione um time",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded1) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.textFieldColors()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded1,
                            onDismissRequest = { expanded1 = false }
                        ) {
                            times.filter { it != timeSelecionado2 }.forEach { time ->
                                DropdownMenuItem(
                                    text = { Text(time.nome) },
                                    onClick = {
                                        timeSelecionado1 = time
                                        expanded1 = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Informações do time 1 se selecionado
            timeSelecionado1?.let { time ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 60.dp, top = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "Overall: ${String.format("%.1f", time.mediaEstrelas)} | Pontuação: ${time.vitorias * 3 + time.empates}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Versus
            Text(
                text = "VS",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )

            // Seleção do segundo time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Time 2: ",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.width(60.dp)
                )

                // Dropdown para selecionar o segundo time
                Box(modifier = Modifier.weight(1f)) {
                    var expanded2 by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded2,
                        onExpandedChange = { expanded2 = it }
                    ) {
                        TextField(
                            value = timeSelecionado2?.nome ?: "Selecione um time",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded2) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.textFieldColors()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded2,
                            onDismissRequest = { expanded2 = false }
                        ) {
                            times.filter { it != timeSelecionado1 }.forEach { time ->
                                DropdownMenuItem(
                                    text = { Text(time.nome) },
                                    onClick = {
                                        timeSelecionado2 = time
                                        expanded2 = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Informações do time 2 se selecionado
            timeSelecionado2?.let { time ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 60.dp, top = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "Overall: ${String.format("%.1f", time.mediaEstrelas)} | Pontuação: ${time.vitorias * 3 + time.empates}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Botões de ação
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botão de cancelar
                OutlinedButton(
                    onClick = onCancelarConfronto,
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimensions.standardButtonHeight)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cancelar confronto"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cancelar")
                }

                // Botão de finalizar confronto - agora abre o diálogo de resultado
                Button(
                    onClick = {
                        if (timeSelecionado1 != null && timeSelecionado2 != null) {
                            mostrarDialogoResultado = true
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimensions.standardButtonHeight),
                    enabled = timeSelecionado1 != null && timeSelecionado2 != null
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Finalizar confronto"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Finalizar")
                }
            }
        }
    }

    // Diálogo para selecionar o resultado do confronto
    if (mostrarDialogoResultado) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoResultado = false },
            title = { Text("Resultado do Confronto") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Selecione o resultado do confronto entre:")
                    Text(
                        text = "${timeSelecionado1?.nome} vs ${timeSelecionado2?.nome}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Opções de resultado
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Opção 1: Time 1 venceu
                        Button(
                            onClick = {
                                resultadoSelecionado = ResultadoConfronto.VITORIA_TIME1
                                mostrarDialogoResultado = false

                                // Finaliza confronto com o resultado selecionado
                                onFinalizarConfronto(
                                    timeSelecionado1!!,
                                    timeSelecionado2!!,
                                    ResultadoConfronto.VITORIA_TIME1
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimensions.standardButtonHeight),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Vitória para ${timeSelecionado1?.nome}")
                        }

                        // Opção 2: Time 2 venceu
                        Button(
                            onClick = {
                                resultadoSelecionado = ResultadoConfronto.VITORIA_TIME2
                                mostrarDialogoResultado = false

                                // Finaliza confronto com o resultado selecionado
                                onFinalizarConfronto(
                                    timeSelecionado1!!,
                                    timeSelecionado2!!,
                                    ResultadoConfronto.VITORIA_TIME2
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimensions.standardButtonHeight),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("Vitória para ${timeSelecionado2?.nome}")
                        }

                        // Opção 3: Empate
                        Button(
                            onClick = {
                                resultadoSelecionado = ResultadoConfronto.EMPATE
                                mostrarDialogoResultado = false

                                // Finaliza confronto com o resultado selecionado
                                onFinalizarConfronto(
                                    timeSelecionado1!!,
                                    timeSelecionado2!!,
                                    ResultadoConfronto.EMPATE
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimensions.standardButtonHeight),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Text("Empate")
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = { }
        )
    }
}
