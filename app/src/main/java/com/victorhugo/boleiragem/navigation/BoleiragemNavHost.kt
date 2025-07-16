package com.victorhugo.boleiragem.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.victorhugo.boleiragem.ui.screens.cadastro.DetalheJogadorScreen
import com.victorhugo.boleiragem.ui.screens.sorteio.ResultadoSorteioScreen
import com.victorhugo.boleiragem.ui.screens.splash.SplashScreen

@Composable
fun BoleiragemNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    showMainScreens: Boolean = true
) {
    NavHost(
        navController = navController,
        startDestination = NavDestinations.Splash.route,
        modifier = modifier
    ) {
        // SplashScreen - Temporário até a navegação para o Home
        composable(
            route = NavDestinations.Splash.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Companion.Left,
                    animationSpec = tween(700)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Companion.Left,
                    animationSpec = tween(700)
                )
            }
        ) {
            SplashScreen(onNavigateToHome = {
                navController.navigate(NavDestinations.CadastroJogadores.route) {
                    popUpTo(NavDestinations.Splash.route) { inclusive = true }
                }
            })
        }

        // Telas principais com navegação por abas - renderizadas apenas quando showMainScreens é true
        // Estas telas agora são gerenciadas pelo ViewPager na MainActivity
        if (showMainScreens) {
            composable(route = NavDestinations.CadastroJogadores.route) { /* Implementação vazia, gerenciada pelo ViewPager */ }
            composable(route = NavDestinations.ConfiguracaoTimes.route) { /* Implementação vazia, gerenciada pelo ViewPager */ }
            composable(route = NavDestinations.SorteioTimes.route) { /* Implementação vazia, gerenciada pelo ViewPager */ }
        }

        // Tela de detalhes do jogador (sem bottom navigation)
        composable(
            route = NavDestinations.DetalheJogador.route,
            arguments = listOf(
                navArgument("jogadorId") { type = NavType.LongType }
            )
        ) {
            val jogadorId = it.arguments?.getLong("jogadorId") ?: -1L
            DetalheJogadorScreen(
                jogadorId = jogadorId,
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }

        // Tela de resultado do sorteio (sem bottom navigation)
        composable(
            route = NavDestinations.ResultadoSorteio.route
        ) {
            ResultadoSorteioScreen(
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }
    }
}
