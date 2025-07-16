package com.victorhugo.boleiragem.ui.screens.sorteio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.victorhugo.boleiragem.data.model.Time

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultadoSorteioScreen(
    viewModel: ResultadoSorteioViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onConfirmarClick: () -> Unit = {
        // Este callback é chamado do MainActivity
        onBackClick()
    },
    onCancelarClick: () -> Unit = {
        // Este callback é chamado do MainActivity
        onBackClick()
    }
) {
    val resultadoSorteio by viewModel.resultadoSorteio.collectAsState()

    // Callbacks locais que chamam os métodos do ViewModel e depois os callbacks externos
    val handleConfirmarClick = {
        // Chama o método do ViewModel aqui, onde é seguro
        viewModel.confirmarSorteio()
        // Depois chama o callback externo que faz a navegação
        onConfirmarClick()
    }

    val handleCancelarClick = {
        // Chama o método do ViewModel aqui, onde é seguro
        viewModel.cancelarSorteio()
        // Depois chama o callback externo que faz a navegação
        onCancelarClick()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resultado do Sorteio") },
                navigationIcon = {
                    IconButton(onClick = { handleCancelarClick() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.compartilharResultado() }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartilhar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars), // Adiciona padding para evitar sobreposição com as barras de navegação
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 16.dp
                        ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { handleCancelarClick() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CANCELAR")
                    }

                    Button(
                        onClick = { handleConfirmarClick() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CONFIRMAR")
                    }
                }
            }
        }
    ) { paddingValues ->
        if (resultadoSorteio != null) {
            val times = resultadoSorteio?.times ?: emptyList()

            if (times.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Não foi possível formar times. Verifique a configuração e a quantidade de jogadores selecionados.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Text(
                        text = "Times Sorteados",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .weight(1f), // Importante para dar espaço ao bottomBar
                        contentPadding = PaddingValues(bottom = 16.dp) // Adiciona espaço no final da lista
                    ) {
                        items(times) { time ->
                            TimeCard(time)
                        }

                        // Espaço adicional no final da lista para melhorar a usabilidade
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum resultado disponível.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun TimeCard(time: Time) {
    val coresTimes = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        Color(0xFF388E3C), // verde
        Color(0xFFF57C00), // laranja
        Color(0xFF7B1FA2)  // roxo
    )

    val corTime = coresTimes[time.id.toInt() % coresTimes.size]

    // Calcular as médias do time
    val mediaEstrelas = time.jogadores.map { it.notaPosicaoPrincipal.toFloat() }.average().toFloat()
    val mediaPontuacao = time.jogadores.map { it.pontuacaoTotal.toFloat() }.average().toFloat()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                color = corTime,
                shape = MaterialTheme.shapes.small
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = time.nome.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Exibir média de estrelas
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "Média: ${String.format("%.1f", mediaEstrelas)}★",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Exibir média de pontuação
                        Text(
                            text = "Pontuação: ${String.format("%.0f", mediaPontuacao)}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            time.jogadores.forEach { jogador ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = jogador.nome,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )

                    // Exibir estrelas individuais para cada jogador
                    Text(
                        text = "${jogador.notaPosicaoPrincipal}★",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    Text(
                        text = jogador.posicaoPrincipal.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}
