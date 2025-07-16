package com.victorhugo.boleiragem.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Critérios combináveis
enum class CriterioSorteio {
    MEDIA_NOTAS,
    POSICAO,
    PONTUACAO
}

@Entity(tableName = "configuracao_sorteio")
data class ConfiguracaoSorteio(
    @PrimaryKey
    val id: Int = 1, // Singleton para configuração
    val qtdJogadoresPorTime: Int = 5,
    val qtdTimes: Int = 2,
    val aleatorio: Boolean = true, // Critério principal agora é um booleano
    val criteriosExtras: Set<CriterioSorteio> = emptySet() // Todos os outros são extras combináveis
)
