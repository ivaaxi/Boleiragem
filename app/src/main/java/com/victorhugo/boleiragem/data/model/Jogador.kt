package com.victorhugo.boleiragem.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PosicaoJogador {
    GOLEIRO, DEFESA, MEIO_CAMPO, ALA, PIVO
}

@Entity(tableName = "jogadores")
data class Jogador(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val posicaoPrincipal: PosicaoJogador,
    val posicaoSecundaria: PosicaoJogador?,
    val notaPosicaoPrincipal: Int, // 1 a 5
    val notaPosicaoSecundaria: Int?, // 1 a 5
    val ativo: Boolean = true,
    val totalJogos: Int = 0,
    val vitorias: Int = 0,
    val derrotas: Int = 0,
    val empates: Int = 0,
    val pontuacaoTotal: Int = 0
)
