package com.victorhugo.boleiragem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.victorhugo.boleiragem.navigation.BoleiragemBottomNavigationBar
import com.victorhugo.boleiragem.navigation.NavDestinations
import com.victorhugo.boleiragem.ui.screens.cadastro.CadastroJogadoresScreen
import com.victorhugo.boleiragem.ui.screens.cadastro.DetalheJogadorScreen
import com.victorhugo.boleiragem.ui.screens.configuracao.ConfiguracaoTimesScreen
import com.victorhugo.boleiragem.ui.screens.configuracao.ConfiguracaoPontuacaoScreen
import com.victorhugo.boleiragem.ui.screens.historico.HistoricoTimesScreen
import com.victorhugo.boleiragem.ui.screens.sorteio.ResultadoSorteioScreen
import com.victorhugo.boleiragem.ui.screens.sorteio.ResultadoSorteioViewModel
import com.victorhugo.boleiragem.ui.screens.sorteio.SorteioTimesScreen
import com.victorhugo.boleiragem.ui.screens.splash.SplashScreen
import com.victorhugo.boleiragem.ui.theme.BoleiragemTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Instalar SplashScreen antes de chamar super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BoleiragemTheme {
                BoleiragemApp()
            }
        }
    }
}

@Composable
fun BoleiragemApp() {
    // Estado para controlar a exibição da SplashScreen
    var showSplashScreen by remember { mutableStateOf(true) }

    if (showSplashScreen) {
        // Mostrar a SplashScreen enquanto o aplicativo está inicializando
        SplashScreen(onNavigateToHome = {
            // Quando a SplashScreen terminar, apenas altera o estado
            showSplashScreen = false
        })
    } else {
        // Depois da SplashScreen, mostra o conteúdo principal
        MainScreen()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen() {
    // Estados para controlar a navegação
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    // Estado para controlar qual tela principal está sendo exibida (0, 1, 2 ou 3)
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Configuração do pager
    val pagerState = rememberPagerState(
        initialPage = selectedTabIndex,
        pageCount = { 4 } // 4 abas principais agora
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
        NavDestinations.HistoricoTimes.route
    )

    // Scaffold com a barra de navegação inferior
    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
                        onNavigateToConfiguracaoPontuacao = {
                            isSecondaryScreen = true
                            secondaryScreenContent = {
                                ConfiguracaoPontuacaoScreen(
                                    onBackClick = {
                                        isSecondaryScreen = false
                                    }
                                )
                            }
                        }
                    )
                    2 -> SorteioTimesScreen(
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
                    3 -> HistoricoTimesScreen() // Nova tela de histórico de times
                }
            }
        }
    }
}