package com.victorhugo.boleiragem

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.victorhugo.boleiragem.navigation.BoleiragemBottomNavigationBar
import com.victorhugo.boleiragem.navigation.NavDestinations
import com.victorhugo.boleiragem.ui.screens.cadastro.CadastroJogadoresScreen
import com.victorhugo.boleiragem.ui.screens.cadastro.DetalheJogadorScreen
import com.victorhugo.boleiragem.ui.screens.configuracao.ConfiguracaoPontuacaoScreen
import com.victorhugo.boleiragem.ui.screens.configuracao.ConfiguracaoTimesScreen
import com.victorhugo.boleiragem.ui.screens.configuracao.GerenciadorPerfisScreen
import com.victorhugo.boleiragem.ui.screens.estatisticas.EstatisticasScreen
import com.victorhugo.boleiragem.ui.screens.grupos.GruposPeladaScreen
import com.victorhugo.boleiragem.ui.screens.historico.HistoricoScreen
import com.victorhugo.boleiragem.ui.screens.login.LoginScreen
import com.victorhugo.boleiragem.ui.screens.sorteio.ResultadoSorteioScreen
import com.victorhugo.boleiragem.ui.screens.sorteio.SorteioTimesScreen
import com.victorhugo.boleiragem.ui.screens.splash.SplashScreen
import com.victorhugo.boleiragem.ui.screens.times.TimesAtuaisScreen
import com.victorhugo.boleiragem.ui.theme.BoleiragemTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Flag para acompanhar se as permissões já foram solicitadas
    private var permissionsRequested = false

    // Lançador para solicitar permissões
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        // Permissões solicitadas, independente do resultado
        permissionsRequested = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Reativando a splash screen agora que o problema dos ícones foi corrigido
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Solicitar permissões de localização ao iniciar o app
        requestLocationPermissions()

        setContent {
            BoleiragemTheme {
                BoleiragemApp()
            }
        }
    }

    private fun requestLocationPermissions() {
        if (!permissionsRequested) {
            val hasLocationPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            // Se ainda não temos permissão, solicitar
            if (!hasLocationPermission) {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
}

@Composable
fun BoleiragemApp() {
    // Estado para controlar a exibição da SplashScreen
    var showSplashScreen by remember { mutableStateOf(true) }
    // Estado para controlar a exibição da tela de login
    var showLoginScreen by remember { mutableStateOf(false) }
    // Estado para controlar a exibição da tela de grupos
    var showGruposScreen by remember { mutableStateOf(false) }
    // Estado para armazenar o ID do grupo selecionado
    var grupoSelecionadoId by remember { mutableStateOf(-1L) }
    // Estado para armazenar o nome do grupo selecionado
    var grupoSelecionadoNome by remember { mutableStateOf("") }

    when {
        showSplashScreen -> {
            // Mostrar a SplashScreen enquanto o aplicativo está inicializando
            SplashScreen(onNavigateToHome = {
                // Quando a SplashScreen terminar, mostrar a tela de login
                showSplashScreen = false
                showLoginScreen = true
            })
        }
        showLoginScreen -> {
            // Mostrar a tela de login
            LoginScreen(
                onLoginClick = {
                    // Quando o usuário fizer login, mostrar a tela de grupos
                    showLoginScreen = false
                    showGruposScreen = true
                },
                onEntrarSemContaClick = {
                    // Quando o usuário entrar sem conta, mostrar a tela de grupos
                    showLoginScreen = false
                    showGruposScreen = true
                }
            )
        }
        showGruposScreen -> {
            // Mostrar a tela de grupos
            val navController = rememberNavController()
            GruposPeladaScreen(
                onGrupoSelecionado = { grupoId, grupoNome ->
                    // Quando um grupo for selecionado, mostrar o conteúdo principal
                    grupoSelecionadoId = grupoId
                    grupoSelecionadoNome = grupoNome
                    showGruposScreen = false
                },
                navController = navController
            )
        }
        else -> {
            // Mostrar o conteúdo principal do aplicativo
            MainScreen(
                grupoId = grupoSelecionadoId,
                grupoNome = grupoSelecionadoNome,
                onVoltarParaGerenciamento = {
                    // Voltar para a tela de gerenciamento de peladas
                    showGruposScreen = true
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    grupoId: Long = -1L,
    grupoNome: String = "", // Adicionando parâmetro para o nome do grupo
    onVoltarParaGerenciamento: () -> Unit = {}
) {
    // Estados para controlar a navegação
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    // Estado para controlar qual tela principal está sendo exibida (0, 1, 2 ou 3)
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Configuração do pager
    val pagerState = rememberPagerState(
        initialPage = selectedTabIndex,
        pageCount = { 6 } // Agora 6 abas principais (incluindo a nova aba de estatísticas)
    )

    // Efeito para sincronizar o selectedTabIndex com a página atual do pager
    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }

    // Função para navegar programaticamente para uma aba específica
    val navigateToTab = { tabIndex: Int ->
        scope.launch {
            pagerState.animateScrollToPage(tabIndex)
        }
    }

    // Define se estamos em uma tela secundária (detalhes ou resultado)
    var isSecondaryScreen by remember { mutableStateOf(false) }
    var secondaryScreenContent by remember { mutableStateOf<@Composable () -> Unit>({}) }

    // Lista de destinos principais para navegação
    val mainScreenTabs = listOf(
        NavDestinations.CadastroJogadores.route,
        NavDestinations.ConfiguracaoTimes.route,
        NavDestinations.SorteioTimes.route,
        NavDestinations.TimesAtuais.route,
        NavDestinations.Historico.route,
        NavDestinations.Estatisticas.route
    )

    // Scaffold com a barra de navegação inferior
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (!isSecondaryScreen) {
                // Barra superior compacta como "Minhas Peladas"
                TopAppBar(
                    title = {
                        Text(
                            text = grupoNome.ifBlank { "Boleiragem" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Start // Alinhamento à esquerda
                        )
                    },
                    navigationIcon = {
                        androidx.compose.material3.IconButton(onClick = onVoltarParaGerenciamento) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Voltar para gerenciamento",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.height(56.dp) // Altura padrão reduzida
                )
            }
        },
        bottomBar = {
            if (!isSecondaryScreen) {
                BoleiragemBottomNavigationBar(
                    navController = navController,
                    currentTab = selectedTabIndex,
                    onTabSelected = { index ->
                        // Atualiza o índice e anima o pager
                        selectedTabIndex = index
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        if (isSecondaryScreen) {
            // Se estamos em uma tela secundária, mostrar o conteúdo correspondente
            secondaryScreenContent()
        } else {
            // Caso contrário, mostrar o ViewPager com as telas principais
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { page ->
                when (page) {
                    0 -> {
                        // Usando Box com contentAlignment para garantir que o FloatingActionButton apareça
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CadastroJogadoresScreen(
                                grupoId = grupoId, // Passando o ID do grupo selecionado
                                onNavigateToDetalheJogador = { jogadorId ->
                                    isSecondaryScreen = true
                                    secondaryScreenContent = {
                                        DetalheJogadorScreen(
                                            jogadorId = jogadorId,
                                            onBackClick = {
                                                isSecondaryScreen = false
                                            }
                                        )
                                    }
                                }
                            )
                        }
                    }
                    1 -> ConfiguracaoTimesScreen(
                        grupoId = grupoId, // Passando o ID do grupo selecionado
                        onNavigateToConfiguracaoPontuacao = {
                            isSecondaryScreen = true
                            secondaryScreenContent = {
                                ConfiguracaoPontuacaoScreen(
                                    onBackClick = {
                                        isSecondaryScreen = false
                                    }
                                )
                            }
                        },
                        onNavigateToGerenciadorPerfis = {
                            isSecondaryScreen = true
                            secondaryScreenContent = {
                                GerenciadorPerfisScreen(
                                    grupoId = grupoId, // Passando o ID do grupo selecionado
                                    onNavigateBack = {
                                        isSecondaryScreen = false
                                    }
                                )
                            }
                        }
                    )
                    2 -> SorteioTimesScreen(
                        grupoId = grupoId, // Passando o ID do grupo selecionado
                        onSorteioRealizado = {
                            // Mostra a tela de resultado do sorteio
                            isSecondaryScreen = true
                            secondaryScreenContent = {
                                ResultadoSorteioScreen(
                                    onBackClick = {
                                        isSecondaryScreen = false
                                    },
                                    onConfirmarClick = {
                                        // O ViewModel é fornecido diretamente na tela ResultadoSorteioScreen
                                        // através do parâmetro viewModel = hiltViewModel()

                                        // Fecha a tela de resultado e navega para a aba de Times
                                        isSecondaryScreen = false
                                        navigateToTab(3)
                                    },
                                    onCancelarClick = {
                                        // O ViewModel é fornecido diretamente na tela ResultadoSorteioScreen

                                        // Volta para a tela de sorteio
                                        isSecondaryScreen = false
                                    }
                                )
                            }
                        },
                        onNavigateToHistorico = {
                            // Esta função não será mais usada para navegação automática
                            // já que agora temos o botão de confirmar
                        }
                    )
                    3 -> TimesAtuaisScreen() // Tela de times atuais
                    4 -> HistoricoScreen() // Nova tela de histórico
                    5 -> EstatisticasScreen() // Tela de estatísticas
                }
            }
        }
    }
}