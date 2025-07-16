package com.victorhugo.boleiragem.ui.screens.configuracao

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.victorhugo.boleiragem.data.model.CriterioSorteio

/**
 * Tela para configuração dos critérios de sorteio
 */
@Composable
fun CriteriosSorteioScreen(
    aleatorio: Boolean,
    criteriosExtras: Set<CriterioSorteio>,
    onAleatorioChange: (Boolean) -> Unit,
    onCriterioExtraToggle: (CriterioSorteio) -> Unit
) {
    // Box externo para controlar a largura do componente
    Box(
        modifier = Modifier
            .fillMaxWidth() // Preenche toda a largura disponível
            .padding(horizontal = 4.dp), // Pequena margem nas laterais
        contentAlignment = Alignment.Center
    ) {
        // Coluna principal com largura fixa mais larga
        Column(
            modifier = Modifier
                .widthIn(min = 320.dp) // Largura mínima fixa
                .fillMaxWidth(0.98f) // Preenche 98% da largura disponível
                .padding(8.dp) // Padding interno reduzido
        ) {
            // Título
            Text(
                text = "Critérios para Sorteio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Critério Principal: Aleatório (Switch)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp), // Reduzindo padding para dar mais espaço
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Coluna com o texto principal
                Column(
                    modifier = Modifier
                        .weight(0.65f) // Peso reduzido para dar mais espaço ao switch
                ) {
                    Text(
                        text = "Sorteio Aleatório",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Distribui jogadores aleatoriamente",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(
                    modifier = Modifier.width(32.dp) // Mais espaço para o switch
                )

                // Switch com espaço ampliado
                Box(
                    modifier = Modifier
                        .width(72.dp) // Largura maior para o switch
                        .padding(end = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Switch(
                        checked = aleatorio,
                        onCheckedChange = { isChecked ->
                            onAleatorioChange(isChecked)

                            // Se desativou o aleatório e não tem critérios selecionados, seleciona o primeiro
                            if (!isChecked && criteriosExtras.isEmpty()) {
                                onCriterioExtraToggle(CriterioSorteio.PONTUACAO)
                            }
                        }
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Seção de critérios extras - Título sempre visível
            Text(
                text = "Critérios Avançados",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            // Texto explicativo que muda conforme o modo
            Text(
                text = if (aleatorio)
                    "Desative o Sorteio Aleatório para usar critérios avançados"
                else
                    "Selecione um ou mais critérios para o sorteio",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Área de critérios avançados - só mostra se NÃO for aleatório
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

                Spacer(modifier = Modifier.height(12.dp))

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
            }
        }
    }
}
