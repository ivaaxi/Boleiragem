package com.victorhugo.boleiragem.navigation

sealed class NavDestinations(val route: String) {
    object Splash : NavDestinations("splash")
    object Home : NavDestinations("home")

    // Bottom Navigation Items
    object CadastroJogadores : NavDestinations("cadastro_jogadores")
    object ConfiguracaoTimes : NavDestinations("configuracao_times")
    object SorteioTimes : NavDestinations("sorteio_times")
    object TimesAtuais : NavDestinations("times_atuais")
    object Historico : NavDestinations("historico")

    // Outras rotas
    object DetalheJogador : NavDestinations("detalhe_jogador/{jogadorId}") {
        fun createRoute(jogadorId: Long) = "detalhe_jogador/$jogadorId"
    }

    object ResultadoSorteio : NavDestinations("resultado_sorteio")
}
