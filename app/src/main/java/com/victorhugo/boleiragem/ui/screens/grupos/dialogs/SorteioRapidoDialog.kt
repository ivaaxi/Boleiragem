package com.victorhugo.boleiragem.ui.screens.grupos.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.victorhugo.boleiragem.data.model.ConfiguracaoSorteio
import com.victorhugo.boleiragem.data.model.GrupoPelada

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SorteioRapidoDialog(
    showDialog: Boolean,
    grupoPelada: GrupoPelada?,
    jogadoresAtivosNoGrupo: Int,
    perfisConfiguracao: List<ConfiguracaoSorteio>,
    usarPerfilExistente: Boolean,
    perfilSelecionado: ConfiguracaoSorteio?,
    jogadoresPorTimeManual: Int,
    numeroDeTimesManual: Int,
    erroAtual: String?,
    podeRealizarSorteio: Boolean, 
    onDismissRequest: () -> Unit,
    onUsarPerfilChanged: (Boolean) -> Unit,
    onPerfilSelecionadoChanged: (ConfiguracaoSorteio) -> Unit,
    onJogadoresPorTimeManualChanged: (String) -> Unit,
    onNumeroDeTimesManualChanged: (String) -> Unit,
    onConfirmSorteio: () -> Unit,
    onClearError: () -> Unit
) {
    if (!showDialog || grupoPelada == null) {
        return
    }

    var expandirDropdownPerfis by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Sorteio Rápido para: ${grupoPelada.nome}",
                    style = MaterialTheme.typography.titleLarge
                )
                HorizontalDivider() // Corrigido

                Text(
                    text = "Fonte dos Jogadores:",
                    style = MaterialTheme.typography.titleMedium
                )
                OutlinedTextField(
                    value = "Grupo: ${grupoPelada.nome} ($jogadoresAtivosNoGrupo jogadores)",
                    onValueChange = {},
                    label = { Text("Grupo Selecionado") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false
                )
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Colar Lista de Jogadores (Em breve)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )

                HorizontalDivider() // Corrigido

                Text(
                    text = "Configuração do Sorteio:",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = usarPerfilExistente,
                        onCheckedChange = {
                            onClearError() 
                            onUsarPerfilChanged(it)
                        },
                        enabled = perfisConfiguracao.isNotEmpty()
                    )
                    Text(
                        text = "Usar perfil existente?",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                if (usarPerfilExistente) {
                    if (perfisConfiguracao.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = expandirDropdownPerfis,
                            onExpandedChange = { expandirDropdownPerfis = !expandirDropdownPerfis },
                            modifier = Modifier.fillMaxWidth() // Adicionado para ocupar a largura
                        ) {
                            OutlinedTextField(
                                value = perfilSelecionado?.nome ?: "Nenhum perfil disponível",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Perfil de Configuração") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirDropdownPerfis) },
                                modifier = Modifier.fillMaxWidth(), // Removido .menuAnchor()
                                isError = erroAtual != null && perfilSelecionado == null && perfisConfiguracao.isNotEmpty() 
                            )
                            ExposedDropdownMenu(
                                expanded = expandirDropdownPerfis,
                                onDismissRequest = { expandirDropdownPerfis = false }
                            ) {
                                perfisConfiguracao.forEach { perfil ->
                                    DropdownMenuItem(
                                        text = { Text(perfil.nome) },
                                        onClick = {
                                            onClearError() 
                                            onPerfilSelecionadoChanged(perfil)
                                            expandirDropdownPerfis = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        Text("Nenhum perfil de configuração salvo para este grupo. Desmarque a opção acima para configurar manualmente.")
                    }
                } else {
                    OutlinedTextField(
                        value = if (jogadoresPorTimeManual == 0 && numeroDeTimesManual == 0 && perfilSelecionado == null) "" else jogadoresPorTimeManual.toString().takeIf { it != "0" } ?: "",
                        onValueChange = {
                            onClearError() 
                            onJogadoresPorTimeManualChanged(it)
                        },
                        label = { Text("Nº de Jogadores por Time") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = erroAtual?.contains("jogadores por time", ignoreCase = true) == true || erroAtual?.contains("deve ser maior que zero", ignoreCase = true) == true
                    )
                    OutlinedTextField(
                        value = if (numeroDeTimesManual == 0 && jogadoresPorTimeManual == 0 && perfilSelecionado == null) "" else numeroDeTimesManual.toString().takeIf { it != "0" } ?: "",
                        onValueChange = {
                            onClearError() 
                            onNumeroDeTimesManualChanged(it)
                        },
                        label = { Text("Nº de Times") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = erroAtual?.contains("número de times", ignoreCase = true) == true || erroAtual?.contains("deve ser maior que zero", ignoreCase = true) == true
                    )
                }

                if (erroAtual != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = erroAtual,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirmSorteio,
                        enabled = podeRealizarSorteio 
                    ) {
                        Text("Sortear")
                    }
                }
            }
        }
    }
}
