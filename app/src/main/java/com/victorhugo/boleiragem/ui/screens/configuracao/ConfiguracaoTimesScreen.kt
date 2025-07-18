package com.victorhugo.boleiragem.ui.screens.configuracao

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.victorhugo.boleiragem.ui.common.Dimensions.standardButtonHeight
import com.victorhugo.boleiragem.ui.common.Dimensions.standardButtonWidth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracaoTimesScreen(
    viewModel: ConfiguracaoTimesViewModel = hiltViewModel(),
    onNavigateToConfiguracaoPontuacao: () -> Unit = {}
) {
    val configuracao by viewModel.configuracao.collectAsState(null)
    val scrollState = rememberScrollState()
    val configSalva by viewModel.configSalva.collectAsState()

    // SnackBar para confirmação de salvamento
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(configSalva) {
        if (configSalva) {
            snackbarHostState.showSnackbar(
                message = "Configurações salvas com sucesso!",
                duration = SnackbarDuration.Short
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Configuração de Times") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        if (configuracao != null) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Estrutura dos Times",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Jogadores por Time", fontWeight = FontWeight.Medium)

                            Slider(
                                value = viewModel.jogadoresPorTime.toFloat(),
                                onValueChange = { viewModel.jogadoresPorTime = it.toInt() },
                                valueRange = 3f..11f,
                                steps = 7,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = "${viewModel.jogadoresPorTime} jogadores",
                                modifier = Modifier.align(Alignment.End)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Quantidade de Times", fontWeight = FontWeight.Medium)

                            Slider(
                                value = viewModel.quantidadeTimes.toFloat(),
                                onValueChange = { viewModel.quantidadeTimes = it.toInt() },
                                valueRange = 2f..6f,
                                steps = 3,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = "${viewModel.quantidadeTimes} times",
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Critérios de Sorteio",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Usando o novo componente de critérios de sorteio
                    CriteriosSorteioCard(
                        aleatorio = viewModel.aleatorio,
                        criteriosExtras = viewModel.criteriosExtras,
                        onAleatorioChange = { viewModel.toggleAleatorio(it) },
                        onCriterioExtraToggle = { viewModel.toggleCriterioExtra(it) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Configurações Adicionais",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateToConfiguracaoPontuacao
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Configuração de Pontuação",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Defina pontos por vitória, derrota e empate",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Configurar pontuação",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Adicionar um espaço no final para garantir que todos os elementos sejam visíveis após o scroll
                    // e não fiquem embaixo do botão
                    Spacer(modifier = Modifier.height(80.dp))
                }

                // Botão flutuante no canto inferior da tela, mas dentro da Box
                Button(
                    onClick = {
                        viewModel.salvarConfiguracoes()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .width(com.victorhugo.boleiragem.ui.common.Dimensions.standardButtonWidth)
                        .height(com.victorhugo.boleiragem.ui.common.Dimensions.standardButtonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("SALVAR CONFIGURAÇÕES")
                }

                // Host do Snackbar
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        } else {
            // Estado de carregamento
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
