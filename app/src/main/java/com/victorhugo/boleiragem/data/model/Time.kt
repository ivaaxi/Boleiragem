package com.victorhugo.boleiragem.data.model

data class Time(
    val id: Int,
    val nome: String,
    val jogadores: List<Jogador>
)

data class ResultadoSorteio(
    val times: List<Time>,
    val dataHoraSorteio: Long = System.currentTimeMillis()
)
