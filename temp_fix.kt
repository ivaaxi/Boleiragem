// Código atualizado para a chamada do TimeHistoricoCard
items(historicoTimes) { time ->
    TimeHistoricoCard(
        time = time,
        onVitoriaClick = { viewModel.adicionarVitoria(time) },
        onDerrotaClick = { viewModel.adicionarDerrota(time) },
        onEmpateClick = { viewModel.adicionarEmpate(time) },
        onDiminuirVitoriaClick = { viewModel.diminuirVitoria(time) },
        onDiminuirDerrotaClick = { viewModel.diminuirDerrota(time) },
        onDiminuirEmpateClick = { viewModel.diminuirEmpate(time) },
        onSubstituicaoClick = { viewModel.abrirTransferenciaTimeReserva(time) }
    )
}

