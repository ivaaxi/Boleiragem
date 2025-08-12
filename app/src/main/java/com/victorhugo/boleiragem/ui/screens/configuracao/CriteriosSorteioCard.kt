package com.victorhugo.boleiragem.ui.screens.configuracao

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.victorhugo.boleiragem.data.model.ConfiguracaoSorteio
import com.victorhugo.boleiragem.data.model.CriterioSorteio

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

/**
 * Card para configuração dos critérios de sorteio
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CriteriosSorteioCard(
    aleatorio: Boolean,
    criteriosExtras: Set<CriterioSorteio>,
    onAleatorioChange: (Boolean) -> Unit,
    onCriterioExtraToggle: (CriterioSorteio) -> Unit,
    onGerenciarPerfisClick: () -> Unit = {}, // Para navegação para a tela de gerenciamento de perfis
    configuracoesDisponiveis: List<ConfiguracaoSorteio> = emptyList(), // Nova propriedade para lista de configurações
    configSelecionada: ConfiguracaoSorteio? = null, // Nova propriedade para a configuração selecionada
    onConfiguracaoSelecionada: (ConfiguracaoSorteio) -> Unit = {} // Callback para quando uma nova configuração é selecionada
) {
    // Estado para controlar a expansão do dropdown
    var dropdownExpandido by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Cabeçalho com título
            Text(
                text = "Critérios para Sorteio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Spinner de seleção de perfis (novo componente)
            if (configuracoesDisponiveis.isNotEmpty()) {
                Text(
                    text = "Perfil de Configuração",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                // Dropdown para seleção de configuração
                ExposedDropdownMenuBox(
                    expanded = dropdownExpandido,
                    onExpandedChange = { dropdownExpandido = !dropdownExpandido }
                ) {
                    OutlinedTextField(
                        value = configSelecionada?.nome ?: "Selecione um perfil",
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpandido)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = dropdownExpandido,
                        onDismissRequest = { dropdownExpandido = false }
                    ) {
                        // Mostra os perfis reais de configuração
                        configuracoesDisponiveis.forEach { config ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(config.nome)
                                        if (config.isPadrao) {
                                            Text(
                                                text = "(Padrão)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onConfiguracaoSelecionada(config)
                                    dropdownExpandido = false
                                }
                            )
                        }

                        // Opção de gerenciar perfis
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Gerenciar perfis...",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                dropdownExpandido = false
                                onGerenciarPerfisClick()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Critério Principal: Aleatório (Switch) - Tornando toda a área clicável
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAleatorioChange(!aleatorio) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = aleatorio,
                    onCheckedChange = { onAleatorioChange(it) },
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column {
                    Text(
                        text = "Sorteio Aleatório",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Distribui jogadores de forma totalmente aleatória",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            // Seção de critérios extras
            Text(
                text = "Critérios Avançados",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (aleatorio)
                    "Desative o Sorteio Aleatório para usar critérios avançados"
                else
                    "Selecione um ou mais critérios para o sorteio",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Apenas exibir os critérios quando aleatorio estiver desativado
            if (!aleatorio) {
                // Checkbox para PONTUACAO
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = criteriosExtras.contains(CriterioSorteio.PONTUACAO),
                        onCheckedChange = { onCriterioExtraToggle(CriterioSorteio.PONTUACAO) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Pontuação",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Distribui jogadores com base na pontuação acumulada",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Checkbox para MEDIA_NOTAS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = criteriosExtras.contains(CriterioSorteio.MEDIA_NOTAS),
                        onCheckedChange = { onCriterioExtraToggle(CriterioSorteio.MEDIA_NOTAS) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Média das Notas (Estrelas)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Considera a habilidade de cada jogador pelo número de estrelas",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Checkbox para POSICAO
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = criteriosExtras.contains(CriterioSorteio.POSICAO),
                        onCheckedChange = { onCriterioExtraToggle(CriterioSorteio.POSICAO) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Posição do Jogador",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tenta distribuir jogadores de diferentes posições entre os times",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Texto explicativo
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "Quanto mais critérios selecionados, mais equilibrado será o sorteio. " +
                              "Pelo menos um critério deve estar selecionado quando o sorteio aleatório está desativado.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                // Card de informação quando aleatorio estiver ativado
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "Os critérios avançados só estão disponíveis quando o Sorteio Aleatório está desativado.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
