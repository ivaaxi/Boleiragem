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
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // Agora é autogerado para permitir múltiplos perfis
    val nome: String = "Padrão", // Nome do perfil de configuração
    val qtdJogadoresPorTime: Int = 5,
    val qtdTimes: Int = 2,
    val aleatorio: Boolean = true, // Critério principal agora é um booleano
    val criteriosExtras: Set<CriterioSorteio> = emptySet(), // Todos os outros são extras combináveis
    val isPadrao: Boolean = false, // Indica se este é o perfil padrão atualmente selecionado
    val grupoId: Long = 0 // ID do grupo ao qual esta configuração pertence
)
