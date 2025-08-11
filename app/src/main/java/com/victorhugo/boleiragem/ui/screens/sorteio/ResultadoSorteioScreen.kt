package com.victorhugo.boleiragem.ui.screens.sorteio

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.victorhugo.boleiragem.data.model.Jogador
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
    val capitaesSelecionados by viewModel.capitaesSelecionados.collectAsState()
    val todosCapitaesDefinidos by viewModel.todosCapitaesDefinidos.collectAsState()
    val peladaEmAndamento by viewModel.peladaEmAndamento.collectAsState()
    val textoCompartilhamento by viewModel.textoCompartilhamento.collectAsState(initial = "")
    val modoSorteioRapido by viewModel.modoSorteioRapido.collectAsState()
    val context = LocalContext.current

    // Estado para diálogo de confirmação quando já existe uma pelada em andamento
    var mostrarDialogoConfirmacao by remember { mutableStateOf(false) }

    // Efeito para compartilhar quando o texto for gerado
    LaunchedEffect(textoCompartilhamento) {
        if (textoCompartilhamento.isNotBlank()) {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, textoCompartilhamento)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Compartilhar times")
            context.startActivity(shareIntent)
            // Limpa o texto após compartilhar para evitar múltiplos disparos
            viewModel.limparTextoCompartilhamento()
        }
    }

    // Callbacks locais que chamam os métodos do ViewModel e depois os callbacks externos
    val handleConfirmarClick = {
        if (peladaEmAndamento) {
            // Se existe uma pelada em andamento, mostrar diálogo de confirmação
            mostrarDialogoConfirmacao = true
        } else if (todosCapitaesDefinidos) {
            // Chama o método do ViewModel aqui, onde é seguro
            viewModel.confirmarSorteio()
            // Depois chama o callback externo que faz a navegação
            onConfirmarClick()
        }
    }

    val handleCancelarClick = {
        // Chama o método do ViewModel aqui, onde é seguro
        viewModel.cancelarSorteio()
        // Depois chama o callback externo que faz a navegação
        onCancelarClick()
    }

    // Diálogo de confirmação quando já existe uma pelada em andamento
    if (mostrarDialogoConfirmacao) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmacao = false },
            title = { Text("Pelada em andamento") },
            text = {
                Text(
                    "Já existe uma pelada em andamento. Se continuar, " +
                    "a pelada atual será apagada e as estatísticas não serão contabilizadas. " +
                    "Deseja continuar?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoConfirmacao = false
                        viewModel.confirmarSorteio()
                        onConfirmarClick()
                    }
                ) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { mostrarDialogoConfirmacao = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Título da barra superior - diferente dependendo do modo
    val tituloAppBar = if (modoSorteioRapido) "Sorteio Rápido" else "Resultado do Sorteio"

    // Determinar se estamos no modo rápido e já mostrar o botão de compartilhar em vez de confirmar
    val botaoConfirmar = modoSorteioRapido

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tituloAppBar) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            if (resultadoSorteio != null) {
                Surface(
                    tonalElevation = 3.dp,
                    shadowElevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        // No modo sorteio rápido, mostramos apenas o botão de compartilhar
                        if (modoSorteioRapido) {
                            // Botão de compartilhar no modo sorteio rápido
                            Button(
                                onClick = { viewModel.gerarTextoCompartilhamento() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Compartilhar"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("COMPARTILHAR")
                            }
                        } else {
                            // Modo normal - botões de confirmar e cancelar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Botão Cancelar (apenas no modo normal)
                                OutlinedButton(
                                    onClick = onCancelarClick,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("CANCELAR")
                                }

                                // Botão Confirmar (apenas no modo normal)
                                Button(
                                    onClick = handleConfirmarClick,
                                    enabled = todosCapitaesDefinidos,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Confirmar"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("CONFIRMAR")
                                }
                            }

                            // Botão de compartilhar no modo normal
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.gerarTextoCompartilhamento() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Compartilhar"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("COMPARTILHAR")
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (resultadoSorteio == null) {
            // Se não há resultado, mostra uma mensagem de que não houve sorteio
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum sorteio realizado ainda.\nVolte à tela de sorteio para iniciar.",
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!modoSorteioRapido) {
                    // Modo normal - Mostra instrução para selecionar capitães
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (!todosCapitaesDefinidos)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (!todosCapitaesDefinidos)
                                      "Selecione o capitão de cada time"
                                  else
                                      "Todos os capitães foram selecionados!",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (!todosCapitaesDefinidos)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            if (!todosCapitaesDefinidos) {
                                Text(
                                    text = "Você precisa escolher um capitão para cada time antes de continuar",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Todos capitães selecionados",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // Exibe os times sorteados com a opção de selecionar capitães
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        resultadoSorteio?.let { resultado ->
                            items(resultado.times) { time ->
                                val capitaoId = capitaesSelecionados[time.id.toLong()]
                                TimeCard(
                                    time = time,
                                    capitaoId = capitaoId,
                                    onJogadorClick = { jogador ->
                                        viewModel.selecionarCapitao(time.id.toLong(), jogador.id.toLong())
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Modo Sorteio Rápido - Mostra apenas os resultados sem opção de selecionar capitães
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Resultado do Sorteio Rápido",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            Text(
                                text = "Para compartilhar os times, use o botão abaixo",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Exibe os times sorteados sem a opção de selecionar capitães
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        resultadoSorteio?.let { resultado ->
                            items(resultado.times) { time ->
                                TimeCardSimples(time = time)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeCard(
    time: Time,
    capitaoId: Long?,
    onJogadorClick: (Jogador) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Verificamos se é o Time Reserva
            val isTimeReserva = time.nome == "Time Reserva"

            // Título do Time com estilo diferente para o Time Reserva
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = time.nome,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isTimeReserva) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                )
            }

            Divider()

            // Se for o Time Reserva, mostramos uma explicação sobre sua função
            if (isTimeReserva) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "Time de Revezamento",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "Os jogadores do Time Reserva farão revezamento com os times que perderem partidas. Um jogador reserva substitui um jogador do time perdedor após cada jogo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Jogadores para revezamento:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                // Lista de jogadores reserva sem opção de selecionar capitão
                time.jogadores.forEach { jogador ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = jogador.nome,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Text(
                                text = "${jogador.posicaoPrincipal.name} (${jogador.notaPosicaoPrincipal}⭐)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Text(
                            text = "${jogador.pontuacaoTotal} pts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Para times normais, mostra a opção de selecionar capitão
                Text(
                    text = "Selecione o capitão:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                // Lista de jogadores com opção de selecionar capitão
                time.jogadores.forEach { jogador ->
                    val isCapitao = jogador.id == capitaoId
                    val tint = if (isCapitao) MaterialTheme.colorScheme.primary else Color.LightGray

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onJogadorClick(jogador) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(
                                selected = isCapitao,
                                onClick = { onJogadorClick(jogador) }
                            )

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = jogador.nome,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isCapitao) FontWeight.Bold else FontWeight.Normal
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = "Pontuação",
                                            modifier = Modifier.size(12.dp),
                                            tint = tint
                                        )

                                        if (isCapitao) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Capitão",
                                                modifier = Modifier.size(12.dp),
                                                tint = tint
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "${jogador.posicaoPrincipal.name} (${jogador.notaPosicaoPrincipal}⭐)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Text(
                            text = "${jogador.pontuacaoTotal} pts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeCardSimples(
    time: Time
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Verificamos se é o Time Reserva
            val isTimeReserva = time.nome == "Time Reserva"

            // Título do Time com estilo diferente para o Time Reserva
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = time.nome,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isTimeReserva) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                )
            }

            Divider()

            // Se for o Time Reserva, mostramos uma explicação sobre sua função
            if (isTimeReserva) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "Time de Revezamento",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "Os jogadores do Time Reserva farão revezamento com os times que perderem partidas. Um jogador reserva substitui um jogador do time perdedor após cada jogo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Jogadores para revezamento:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                // Lista de jogadores reserva sem opção de selecionar capitão
                time.jogadores.forEach { jogador ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = jogador.nome,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Text(
                                text = "${jogador.posicaoPrincipal.name} (${jogador.notaPosicaoPrincipal}⭐)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Text(
                            text = "${jogador.pontuacaoTotal} pts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Para times normais, apenas mostra os jogadores
                time.jogadores.forEach { jogador ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = jogador.nome,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Text(
                                text = "${jogador.posicaoPrincipal.name} (${jogador.notaPosicaoPrincipal}⭐)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Text(
                            text = "${jogador.pontuacaoTotal} pts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
