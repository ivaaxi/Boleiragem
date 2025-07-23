package com.victorhugo.boleiragem.ui.screens.configuracao

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.victorhugo.boleiragem.data.model.CriterioSorteio

/**
 * Card para configuração dos critérios de sorteio
 */
@Composable
fun CriteriosSorteioCard(
    aleatorio: Boolean,
    criteriosExtras: Set<CriterioSorteio>,
    onAleatorioChange: (Boolean) -> Unit,
    onCriterioExtraToggle: (CriterioSorteio) -> Unit,
    onGerenciarPerfisClick: () -> Unit = {} // Mantém o parâmetro por compatibilidade, mas não usa mais
) {
    Card(
        modifier = Modifier.fillMaxWidth() // Removido o padding externo para igualar ao outro card
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Cabeçalho apenas com título, sem o botão de engrenagem
            Text(
                text = "Critérios para Sorteio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Critério Principal: Aleatório (Switch) - Tornando toda a área clicável
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAleatorioChange(!aleatorio) } // Tornando toda a Row clicável
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
