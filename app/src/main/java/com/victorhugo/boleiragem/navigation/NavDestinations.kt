package com.victorhugo.boleiragem.navigation

sealed class NavDestinations(val route: String) {
    object Splash : NavDestinations("splash")
    object Home : NavDestinations("home")

    // Novas rotas para gerenciamento de peladas
    object Login : NavDestinations("login")
    object GruposPelada : NavDestinations("grupos_pelada")
    object GrupoDetalhe : NavDestinations("grupo_detalhe/{grupoId}") {
        fun createRoute(grupoId: Long) = "grupo_detalhe/$grupoId"
    }

    // Bottom Navigation Items
    object CadastroJogadores : NavDestinations("cadastro_jogadores")
    object ConfiguracaoTimes : NavDestinations("configuracao_times")
    object SorteioTimes : NavDestinations("sorteio_times")
    object TimesAtuais : NavDestinations("times_atuais")
    object Historico : NavDestinations("historico")
    object Estatisticas : NavDestinations("estatisticas") // Nova rota para estatísticas

    // Outras rotas
    object DetalheJogador : NavDestinations("detalhe_jogador/{jogadorId}") {
        fun createRoute(jogadorId: Long) = "detalhe_jogador/$jogadorId"
    }

    object ResultadoSorteio : NavDestinations("resultado_sorteio/{isSorteioRapido}") {
        fun createRoute(isSorteioRapido: Boolean) = "resultado_sorteio/$isSorteioRapido"
    }

     // Rota para gerenciamento de perfis de configuração com ID do grupo
    object GerenciadorPerfis : NavDestinations("gerenciador_perfis/{grupoId}") {
        fun createRoute(grupoId: Long) = "gerenciador_perfis/$grupoId"
    }
}
