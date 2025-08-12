
package com.victorhugo.boleiragem

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    var showSplashScreen by remember { mutableStateOf(true) }
    var showLoginScreen by remember { mutableStateOf(false) }
    var showGruposScreen by remember { mutableStateOf(false) }
    var showResultadoSorteioRapido by remember { mutableStateOf(false) } // Nova flag de estado
    var grupoSelecionadoId by remember { mutableStateOf(-1L) }
    var grupoSelecionadoNome by remember { mutableStateOf("") }

    when {
        showSplashScreen -> {
            SplashScreen(onNavigateToHome = {
                showSplashScreen = false
                showLoginScreen = true
            })
        }
        showLoginScreen -> {
            LoginScreen(
                onLoginClick = {
                    showLoginScreen = false
                    showGruposScreen = true
                },
                onEntrarSemContaClick = {
                    showLoginScreen = false
                    showGruposScreen = true
                }
            )
        }
        showGruposScreen -> {
            // val navController = rememberNavController() // NavController local não é mais necessário aqui para este propósito
            GruposPeladaScreen(
                onGrupoSelecionado = { grupoId, grupoNome ->
                    grupoSelecionadoId = grupoId
                    grupoSelecionadoNome = grupoNome
                    showGruposScreen = false
                    // MainScreen será mostrada no 'else'
                },
                onNavigateToSorteioResultado = { isSorteioRapidoValue ->
                    if (isSorteioRapidoValue) {
                        showGruposScreen = false
                        showResultadoSorteioRapido = true
                    }
                    // Se precisar lidar com !isSorteioRapidoValue, adicione lógica aqui
                }
                // navController = navController // Removido
            )
        }
        showResultadoSorteioRapido -> {
            val tempNavController = rememberNavController()
            NavHost(
                navController = tempNavController,
                startDestination = "placeholder_resultado_sorteio" // Rota inicial temporária
            ) {
                composable("placeholder_resultado_sorteio") {
                    // Pode ser um Box vazio ou um indicador de carregamento,
                    // já que vamos navegar imediatamente.
                    Box(modifier = Modifier.fillMaxSize())
                }
                composable(
                    route = NavDestinations.ResultadoSorteio.route, // Rota real: "resultado_sorteio/{isSorteioRapido}"
                    arguments = listOf(navArgument("isSorteioRapido") { type = NavType.BoolType })
                ) {
                    // ResultadoSorteioViewModel pegará 'isSorteioRapido' do SavedStateHandle
                    ResultadoSorteioScreen(
                        onBackClick = {
                            showResultadoSorteioRapido = false
                            showGruposScreen = true // Voltar para a tela de grupos
                        }
                        // onConfirmarClick e onCancelarClick usam os padrões da tela,
                        // que chamam onBackClick. Para sorteio rápido, isso significa voltar para Grupos.
                    )
                }
            }
            // Navega para a tela de resultado após o NavHost ser composto
            LaunchedEffect(Unit) {
                tempNavController.navigate(NavDestinations.ResultadoSorteio.createRoute(isSorteioRapido = true)) {
                    popUpTo("placeholder_resultado_sorteio") { inclusive = true } // Remove o placeholder do backstack
                }
            }
        }
        else -> {
            // Mostrar o conteúdo principal do aplicativo (MainScreen)
            // Isso acontece se nenhuma das flags acima for true (ex: grupo foi selecionado)
            MainScreen(
                grupoId = grupoSelecionadoId,
                grupoNome = grupoSelecionadoNome,
                onVoltarParaGerenciamento = {
                    showGruposScreen = true
                    // Outras flags como showResultadoSorteioRapido devem ser false aqui
                    showResultadoSorteioRapido = false
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    grupoId: Long = -1L,
    grupoNome: String = "",
    onVoltarParaGerenciamento: () -> Unit = {}
) {
    val navController = rememberNavController() // NavController para o Pager e navegação interna do MainScreen
    val scope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(
        initialPage = selectedTabIndex,
        pageCount = { 6 }
    )

    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }

    val navigateToTab = { tabIndex: Int ->
        scope.launch {
            pagerState.animateScrollToPage(tabIndex)
        }
    }

    var isSecondaryScreen by remember { mutableStateOf(false) }
    var secondaryScreenContent by remember { mutableStateOf<@Composable () -> Unit>({}) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (!isSecondaryScreen) {
                TopAppBar(
                    title = {
                        Text(
                            text = grupoNome.ifBlank { "Boleiragem" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
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
                    modifier = Modifier.height(56.dp)
                )
            }
        },
        bottomBar = {
            if (!isSecondaryScreen) {
                BoleiragemBottomNavigationBar(
                    navController = navController, // Este navController é para as abas
                    currentTab = selectedTabIndex,
                    onTabSelected = { index ->
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
            secondaryScreenContent()
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { page ->
                when (page) {
                    0 -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CadastroJogadoresScreen(
                                grupoId = grupoId,
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
                        grupoId = grupoId,
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
                                    grupoId = grupoId,
                                    onNavigateBack = {
                                        isSecondaryScreen = false
                                    }
                                )
                            }
                        }
                    )
                    2 -> SorteioTimesScreen( // Sorteio normal (não rápido)
                        grupoId = grupoId,
                        onSorteioRealizado = {
                            isSecondaryScreen = true
                            secondaryScreenContent = {
                                // ViewModel é pego via hiltViewModel() e SavedStateHandle (isSorteioRapido = false por padrão)
                                ResultadoSorteioScreen(
                                    onBackClick = {
                                        isSecondaryScreen = false
                                    },
                                    onConfirmarClick = {
                                        isSecondaryScreen = false
                                        navigateToTab(3) // Navega para a aba Times Atuais
                                    },
                                    onCancelarClick = {
                                        isSecondaryScreen = false // Volta para SorteioTimesScreen
                                    }
                                )
                            }
                        },
                        onNavigateToHistorico = { /* Não usado para navegação automática */ }
                    )
                    3 -> TimesAtuaisScreen()
                    4 -> HistoricoScreen()
                    5 -> EstatisticasScreen()
                }
            }
        }
    }
}
