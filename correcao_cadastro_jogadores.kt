// Este é um arquivo temporário com as correções necessárias

// Modificação necessária no CadastroJogadoresScreen.kt
// Altere a definição da função CadastroJogadoresScreen para:

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroJogadoresScreen(
    viewModel: CadastroJogadoresViewModel = hiltViewModel(),
    grupoId: Long, // Adicionado este parâmetro
    onNavigateToDetalheJogador: (Long) -> Unit
) {
    // Adicione esta linha para configurar o grupoId no ViewModel
    viewModel.setGrupoId(grupoId)

    val jogadores by viewModel.jogadores.collectAsState(initial = emptyList())
    var showAddJogadorDialog by remember { mutableStateOf(false) }
    var showOrdenarDialog by remember { mutableStateOf(false) }

    // O resto do código permanece igual
}
