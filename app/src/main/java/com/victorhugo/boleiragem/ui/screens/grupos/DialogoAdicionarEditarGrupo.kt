package com.victorhugo.boleiragem.ui.screens.grupos

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.victorhugo.boleiragem.R
import com.victorhugo.boleiragem.data.model.DiaSemana
import com.victorhugo.boleiragem.data.model.GrupoPelada
import com.victorhugo.boleiragem.data.model.TipoRecorrencia
import com.victorhugo.boleiragem.ui.common.HoraTextField
import com.victorhugo.boleiragem.ui.common.OutlinedTextFieldComAcentos
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DialogoAdicionarEditarGrupo(
    grupo: GrupoPelada? = null,
    onDismissRequest: () -> Unit,
    onSalvar: (nome: String, local: String, horario: String, imagemUrl: String?, descricao: String?, tipoRecorrencia: TipoRecorrencia, diaSemana: DiaSemana?, latitude: Double?, longitude: Double?, endereco: String?, localNome: String?, diasSemana: List<DiaSemana>) -> Unit,
    onAbrirMapaSeletor: () -> Unit
) {
    // Estado para os campos do formulário
    var nome by remember { mutableStateOf(grupo?.nome ?: "") }
    var local by remember { mutableStateOf(grupo?.local ?: "") } // Mantemos a variável para compatibilidade
    var horario by remember {
        mutableStateOf(
            if (grupo != null) {
                try {
                    // Tenta formatar a data do grupo se existir
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val date = inputFormat.parse(grupo.horario)
                    val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    outputFormat.format(date)
                } catch (e: Exception) {
                    // Se não conseguir formatar, retorna apenas o texto original
                    grupo.horario
                }
            } else {
                // Se for um novo grupo, usa o horário atual formatado
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            }
        )
    }
    var descricao by remember { mutableStateOf(grupo?.descricao ?: "") }

    // Estado para o tipo de recorrência
    var tipoRecorrencia by remember { mutableStateOf(grupo?.tipoRecorrencia ?: TipoRecorrencia.ESPORADICA) }

    // Estado para o dia da semana (versão antiga)
    var diaSemana by remember { mutableStateOf(grupo?.diaSemana ?: DiaSemana.DOMINGO) }

    // Estado para os dias da semana selecionados (suporte a múltiplos dias)
    var diasSemanaSelecionados by remember { mutableStateOf(grupo?.diasSemana ?: emptyList<DiaSemana>()) }

    // Estado para a imagem selecionada
    var imagemSelecionada by remember { mutableStateOf(grupo?.imagemUrl ?: "ic_pelada_default_1") }

    // Estados para localização do Google Maps
    var latitude by remember { mutableStateOf(grupo?.latitude) }
    var longitude by remember { mutableStateOf(grupo?.longitude) }
    var endereco by remember { mutableStateOf(grupo?.endereco) }
    var localNome by remember { mutableStateOf(grupo?.localNome) }

    // Opções de imagens predefinidas (apenas as que existem no projeto)
    val opcoesImagens = listOf(
        "ic_pelada_default_1",
        "ic_pelada_default_2",
        "logo_boleiragem"
    )

    // Cores disponíveis para o ícone do grupo
    val cores = listOf(
        Color(0xFF2196F3), // Azul
        Color(0xFF4CAF50), // Verde
        Color(0xFFF44336), // Vermelho
        Color(0xFFFF9800), // Laranja
        Color(0xFF9C27B0), // Roxo
        Color(0xFF795548), // Marrom
    )

    // Estado para a cor selecionada
    var corSelecionada by remember { mutableStateOf(cores[0]) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(500.dp)
            ) {
                Text(
                    text = if (grupo == null) "Novo Grupo de Pelada" else "Editar Grupo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Colocando todo o conteúdo em uma coluna com scroll
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Seletor de imagem
                    Text(
                        text = "Selecione uma imagem:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Grade de opções de imagens
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        opcoesImagens.take(3).forEach { imagem ->
                            SeletorImagemItem(
                                imagemResource = imagem,
                                selecionada = imagemSelecionada == imagem,
                                onClick = { imagemSelecionada = imagem }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        opcoesImagens.drop(3).forEach { imagem ->
                            SeletorImagemItem(
                                imagemResource = imagem,
                                selecionada = imagemSelecionada == imagem,
                                onClick = { imagemSelecionada = imagem }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Nome
                    OutlinedTextFieldComAcentos(
                        value = nome,
                        onValueChange = { nome = it },
                        label = { Text("Nome da Pelada") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Campos de localização
                    Text(
                        text = "Localização:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Campo para o nome do local (ex: nome do estabelecimento)
                    OutlinedTextFieldComAcentos(
                        value = localNome ?: "",
                        onValueChange = {
                            localNome = it
                            // Atualiza o campo local para manter compatibilidade
                            local = it
                        },
                        label = { Text("Nome do Local") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Campo para o endereço
                    OutlinedTextFieldComAcentos(
                        value = endereco ?: "",
                        onValueChange = { endereco = it },
                        label = { Text("Endereço") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Endereço"
                            )
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Exibir o mapa com a localização atual (se disponível)
                    if (latitude != null && longitude != null) {
                        Text(
                            text = "Localização atual:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Aqui você pode usar um Composable para exibir o mapa, passando a latitude e longitude
                        // Por exemplo: GoogleMapView(latitude = latitude, longitude = longitude)

                        // Placeholder para o mapa
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            // Exibir um marcador ou algo semelhante para indicar a localização
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Localização no Mapa",
                                modifier = Modifier
                                    .size(48.dp)
                                    .align(Alignment.Center),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botão para selecionar localização no mapa
                    OutlinedButton(
                        onClick = onAbrirMapaSeletor,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Selecionar no mapa"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Selecionar localização no mapa")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Horário com formatação automática
                    HoraTextField(
                        value = horario,
                        onValueChange = { horario = it },
                        label = { Text("Horário da Pelada") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Horário"
                            )
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tipo de Recorrência
                    Text(
                        text = "Tipo de Recorrência:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Opções de tipo de recorrência
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TipoRecorrencia.values().forEach { tipo ->
                            // Definindo os nomes aqui para evitar erros de propriedade 'nome' não encontrada
                            val nomeRecorrencia = when(tipo) {
                                TipoRecorrencia.RECORRENTE -> "Recorrente"
                                TipoRecorrencia.ESPORADICA -> "Esporádica"
                            }

                            // Ajustando o RadioButtonItem para evitar quebra de linha
                            RadioButtonItem(
                                texto = nomeRecorrencia,
                                selecionado = tipoRecorrencia == tipo,
                                onSelecionar = { tipoRecorrencia = tipo },
                                tamanhoFonteReduzido = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Se tipo de recorrência for RECORRENTE, exibir seleção de dias da semana
                    if (tipoRecorrencia == TipoRecorrencia.RECORRENTE) {
                        Text(
                            text = "Selecione os dias da semana:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Seleção múltipla de dias da semana
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DiaSemana.values().forEach { dia ->
                                DiaSemanaItem(
                                    diaSemana = dia,
                                    selecionado = diasSemanaSelecionados.contains(dia),
                                    onSelecionar = {
                                        // Alternar seleção do dia
                                        if (diasSemanaSelecionados.contains(dia)) {
                                            diasSemanaSelecionados = diasSemanaSelecionados.filter { it != dia }
                                        } else {
                                            diasSemanaSelecionados = diasSemanaSelecionados + dia
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        // Se não for recorrente, exibir seleção de um dia da semana usando o mesmo visual
                        Text(
                            text = "Selecione o dia da semana:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Usando a mesma visualização, mas com comportamento de seleção única
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DiaSemana.values().forEach { dia ->
                                DiaSemanaItem(
                                    diaSemana = dia,
                                    selecionado = diaSemana == dia,
                                    onSelecionar = {
                                        // Seleciona apenas o dia clicado (comportamento de radio button)
                                        diaSemana = dia
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Campo Descrição
                    OutlinedTextFieldComAcentos(
                        value = descricao,
                        onValueChange = { descricao = it },
                        label = { Text("Descrição (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Botões
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest
                    ) {
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            // Validar se pelo menos um dia da semana foi selecionado para peladas recorrentes
                            val validaDias = tipoRecorrencia != TipoRecorrencia.RECORRENTE || diasSemanaSelecionados.isNotEmpty()

                            if (validaDias) {
                                // Garantir que localNome não seja nulo para passar na validação
                                val localNomeFinal = localNome ?: local

                                // Correção para garantir que o método de salvar seja chamado corretamente
                                onSalvar(
                                    nome,
                                    local,
                                    horario,
                                    imagemSelecionada,
                                    descricao,
                                    tipoRecorrencia,
                                    // Para compatibilidade com código existente, mantemos um dia principal
                                    if (tipoRecorrencia == TipoRecorrencia.RECORRENTE && diasSemanaSelecionados.isNotEmpty())
                                        diasSemanaSelecionados.first() else diaSemana,
                                    latitude,
                                    longitude,
                                    endereco,
                                    localNomeFinal, // Usando a versão corrigida
                                    diasSemanaSelecionados
                                )

                                // Log para debug
                                println("DEBUG: Tentativa de salvar grupo - Nome: $nome, Dias: $diasSemanaSelecionados")
                            }
                        },
                        enabled = nome.isNotBlank() && horario.isNotBlank() &&
                                 (tipoRecorrencia != TipoRecorrencia.RECORRENTE || diasSemanaSelecionados.isNotEmpty())
                    ) {
                        Text("Salvar")
                    }
                }
            }
        }
    }
}

@Composable
fun SeletorImagemItem(
    imagemResource: String,
    selecionada: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .padding(4.dp)
    ) {
        // Obter o ID do recurso a partir do nome
        val resourceId = when(imagemResource) {
            "ic_pelada_default_1" -> R.drawable.ic_pelada_default_1
            "ic_pelada_default_2" -> R.drawable.ic_pelada_default_2
            "logo_boleiragem" -> R.drawable.logo_boleiragem
            else -> R.drawable.ic_pelada_default_1
        }

        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onClick)
                .border(
                    width = if (selecionada) 2.dp else 0.dp,
                    color = if (selecionada) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Image(
                painter = painterResource(id = resourceId),
                contentDescription = "Imagem $imagemResource",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        if (selecionada) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.BottomEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selecionada",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun SeletorImagemGrupo(
    imagemAtual: String?,
    onImagemSelecionada: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .size(120.dp)
                .clickable { /* Abrir seleção de imagem */ },
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (imagemAtual == null) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Selecionar Imagem",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    // Exibir a imagem se já houver uma selecionada
                    // Image(...)
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Selecionar Imagem",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Selecionar Imagem",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * Componente de item de botão de rádio para seleção de opções
 */
@Composable
fun RadioButtonItem(
    texto: String,
    selecionado: Boolean,
    onSelecionar: () -> Unit,
    tamanhoFonteReduzido: Boolean = false // Novo parâmetro para tamanho de fonte reduzido
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onSelecionar)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selecionado,
            onClick = onSelecionar
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = if (tamanhoFonteReduzido) MaterialTheme.typography.bodySmall.fontSize else MaterialTheme.typography.bodyMedium.fontSize // Ajusta o tamanho da fonte
        )
    }
}

/**
 * Componente para seleção de dia da semana
 */
@Composable
fun DiaSemanaItem(
    diaSemana: DiaSemana,
    selecionado: Boolean,
    onSelecionar: () -> Unit
) {
    val abreviacao = when(diaSemana) {
        DiaSemana.DOMINGO -> "D"
        DiaSemana.SEGUNDA -> "S"
        DiaSemana.TERCA -> "T"
        DiaSemana.QUARTA -> "Q"
        DiaSemana.QUINTA -> "Q"
        DiaSemana.SEXTA -> "S"
        DiaSemana.SABADO -> "S"
    }

    Box(
        modifier = Modifier
            .padding(2.dp)
            .size(36.dp)
            .clip(CircleShape)
            .background(
                if (selecionado) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onSelecionar),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = abreviacao,
            style = MaterialTheme.typography.bodySmall,
            color = if (selecionado) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
    }
}
