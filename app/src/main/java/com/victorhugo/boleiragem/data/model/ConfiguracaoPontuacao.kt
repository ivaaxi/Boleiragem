package com.victorhugo.boleiragem.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configuracao_pontuacao")
data class ConfiguracaoPontuacao(
    @PrimaryKey
    val id: Long = 1, // Usaremos sempre o ID 1 para a única configuração
    val pontosPorVitoria: Int = 10,
    val pontosPorDerrota: Int = -10,
    val pontosPorEmpate: Int = -5
)
