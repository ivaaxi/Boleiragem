package com.victorhugo.boleiragem.ui.screens.cadastro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.victorhugo.boleiragem.data.model.CriterioOrdenacao

@Composable
fun OrdenarDialog(
    onDismiss: () -> Unit,
    criterioAtual: CriterioOrdenacao,
    onOrdenar: (CriterioOrdenacao) -> Unit
) {
    var criterioSelecionado by remember { mutableStateOf(criterioAtual) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ordenar Jogadores") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Selecione o critério de ordenação:",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Opção ALFABETICO
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = criterioSelecionado == CriterioOrdenacao.ALFABETICO,
                            onClick = {
                                criterioSelecionado = CriterioOrdenacao.ALFABETICO
                            }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = criterioSelecionado == CriterioOrdenacao.ALFABETICO,
                        onClick = {
                            criterioSelecionado = CriterioOrdenacao.ALFABETICO
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nome (Ordem Alfabética)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Opção PONTUACAO
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = criterioSelecionado == CriterioOrdenacao.PONTUACAO,
                            onClick = {
                                criterioSelecionado = CriterioOrdenacao.PONTUACAO
                            }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = criterioSelecionado == CriterioOrdenacao.PONTUACAO,
                        onClick = {
                            criterioSelecionado = CriterioOrdenacao.PONTUACAO
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pontuação (Maior para Menor)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Opção JOGOS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = criterioSelecionado == CriterioOrdenacao.JOGOS,
                            onClick = {
                                criterioSelecionado = CriterioOrdenacao.JOGOS
                            }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = criterioSelecionado == CriterioOrdenacao.JOGOS,
                        onClick = {
                            criterioSelecionado = CriterioOrdenacao.JOGOS
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Número de Jogos",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Opção POSICAO (agora separada)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = criterioSelecionado == CriterioOrdenacao.POSICAO,
                            onClick = {
                                criterioSelecionado = CriterioOrdenacao.POSICAO
                            }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = criterioSelecionado == CriterioOrdenacao.POSICAO,
                        onClick = {
                            criterioSelecionado = CriterioOrdenacao.POSICAO
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Posição",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Nova Opção OVERAL
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = criterioSelecionado == CriterioOrdenacao.OVERAL,
                            onClick = {
                                criterioSelecionado = CriterioOrdenacao.OVERAL
                            }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = criterioSelecionado == CriterioOrdenacao.OVERAL,
                        onClick = {
                            criterioSelecionado = CriterioOrdenacao.OVERAL
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Estrelas (Overal)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onOrdenar(criterioSelecionado)
                    onDismiss()
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
