package com.victorhugo.boleiragem.ui.common

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType

/**
 * Campo de texto para entrada de horário que formata automaticamente o valor
 * de acordo com as regras de negócio.
 * - Se o usuário digitar 750, será formatado como 07:50
 * - Se o usuário digitar 0850, será formatado como 08:50
 */
@Composable
fun HoraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    singleLine: Boolean = true,
    enabled: Boolean = true,
) {
    var textoInterno by remember { mutableStateOf(value) }

    OutlinedTextField(
        value = textoInterno,
        onValueChange = { texto ->
            // Remove caracteres não numéricos (exceto ":")
            val apenasNumeros = texto.filter { it.isDigit() || it == ':' }

            // Formata o texto de acordo com as regras especificadas
            val formatado = when {
                // Se já contém ":", apenas mantém o texto (até o limite de 5 caracteres: HH:MM)
                apenasNumeros.contains(":") -> {
                    // Processa para garantir formato correto HH:MM
                    processarTextoComDoisPontos(apenasNumeros)
                }

                // Se tem 4 dígitos (como 0850)
                apenasNumeros.length == 4 -> {
                    val horas = apenasNumeros.substring(0, 2)
                    val minutos = apenasNumeros.substring(2, 4)

                    // Validar se os valores fazem sentido
                    if (horas.toInt() < 24 && minutos.toInt() < 60) {
                        "$horas:$minutos"
                    } else {
                        apenasNumeros
                    }
                }

                // Se tem 3 dígitos (como 750)
                apenasNumeros.length == 3 -> {
                    val hora = apenasNumeros.substring(0, 1)
                    val minutos = apenasNumeros.substring(1, 3)

                    // Se a hora for maior que 0 e menor que 10, adicionamos 0 à esquerda
                    if (hora.toInt() < 10 && minutos.toInt() < 60) {
                        "0$hora:$minutos"
                    } else {
                        apenasNumeros
                    }
                }

                // Se tem 1 ou 2 dígitos, apenas mantém para digitação contínua
                apenasNumeros.length <= 2 -> apenasNumeros

                // Qualquer outro caso, limita a 4 dígitos
                else -> apenasNumeros.take(4)
            }

            textoInterno = formatado
            onValueChange(formatado)
        },
        label = label,
        leadingIcon = leadingIcon,
        textStyle = textStyle,
        singleLine = singleLine,
        enabled = enabled,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

/**
 * Processa texto que já contém dois pontos para garantir formato HH:MM
 */
private fun processarTextoComDoisPontos(texto: String): String {
    // Se não tem o formato correto, ajusta
    if (!texto.matches(Regex("\\d{0,2}:\\d{0,2}"))) {
        // Tenta extrair apenas números e reformatar
        val numerosApenas = texto.filter { it.isDigit() }
        return when {
            numerosApenas.length == 1 -> "0${numerosApenas}:00"
            numerosApenas.length == 2 -> "${numerosApenas}:00"
            numerosApenas.length == 3 -> "0${numerosApenas[0]}:${numerosApenas.substring(1)}"
            numerosApenas.length >= 4 -> {
                val horas = numerosApenas.substring(0, 2)
                val minutos = numerosApenas.substring(2, 4).take(2)

                // Validar se são valores válidos
                if (horas.toInt() < 24 && minutos.toInt() < 60) {
                    "$horas:$minutos"
                } else {
                    // Tenta corrigir valores inválidos
                    val horasCorrigidas = minOf(horas.toInt(), 23).toString().padStart(2, '0')
                    val minutosCorrigidos = minOf(minutos.toInt(), 59).toString().padStart(2, '0')
                    "$horasCorrigidas:$minutosCorrigidos"
                }
            }
            else -> texto.take(5) // Limita a 5 caracteres (HH:MM)
        }
    }

    // Limita a 5 caracteres (HH:MM)
    return texto.take(5)
}
