package com.victorhugo.boleiragem.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.victorhugo.boleiragem.data.db.Converters
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Enumeração dos dias da semana
 */
enum class DiaSemana {
    DOMINGO, SEGUNDA, TERCA, QUARTA, QUINTA, SEXTA, SABADO;

    companion object {
        fun fromOrdinal(ordinal: Int): DiaSemana {
            return values()[ordinal]
        }

        fun getNome(diaSemana: DiaSemana): String {
            return when(diaSemana) {
                DOMINGO -> "Domingo"
                SEGUNDA -> "Segunda-feira"
                TERCA -> "Terça-feira"
                QUARTA -> "Quarta-feira"
                QUINTA -> "Quinta-feira"
                SEXTA -> "Sexta-feira"
                SABADO -> "Sábado"
            }
        }

        fun getAll(): List<DiaSemana> {
            return values().toList()
        }
    }
}

/**
 * Tipo de recorrência da pelada
 */
enum class TipoRecorrencia {
    RECORRENTE,  // Pelada acontece regularmente no mesmo dia da semana
    ESPORADICA   // Pelada com data variável, não segue um padrão semanal
}

/**
 * Entidade que representa um grupo de pelada
 * Cada grupo terá sua própria configuração, jogadores, times, etc.
 */
@Entity(tableName = "grupo_pelada")
data class GrupoPelada(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val local: String,
    val horario: String,
    val dataCriacao: Long = System.currentTimeMillis(),
    val ultimaModificacao: Long = System.currentTimeMillis(),
    val imagemUrl: String? = null,
    val descricao: String? = null,
    val ativo: Boolean = true,
    // ID dos jogadores associados a este grupo
    @TypeConverters(Converters::class)
    val jogadoresIds: List<Long> = emptyList(),
    // Dados para controle de sessão (quando implementarmos login real)
    val usuarioId: String = "local", // Padrão para uso sem conta
    val compartilhado: Boolean = false, // Se o grupo é compartilhado com outros usuários
    // Novos campos para recorrência
    val tipoRecorrencia: TipoRecorrencia = TipoRecorrencia.ESPORADICA,
    val diaSemana: DiaSemana? = null, // Só usado quando tipoRecorrencia é RECORRENTE
    @TypeConverters(Converters::class)
    val diasSemana: List<DiaSemana> = emptyList(), // Lista de dias para peladas recorrentes
    // Novos campos para integração com Google Maps
    val latitude: Double? = null,
    val longitude: Double? = null,
    val endereco: String? = null,
    val localNome: String? = null
) {
    fun getHorarioFormatado(): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            val dateTime = LocalDateTime.parse(horario)
            dateTime.format(formatter)
        } catch (e: Exception) {
            horario // Retorna o horário original se não puder ser formatado
        }
    }

    fun getDescricaoRecorrencia(): String {
        return when (tipoRecorrencia) {
            TipoRecorrencia.RECORRENTE -> {
                if (diasSemana.isNotEmpty()) {
                    val diasFormatados = diasSemana.joinToString(", ") {
                        DiaSemana.getNome(it).uppercase()
                    }
                    "TODA(S): $diasFormatados"
                } else if (diaSemana != null) {
                    "TODA ${DiaSemana.getNome(diaSemana).uppercase()}"
                } else {
                    "RECORRENTE"
                }
            }
            TipoRecorrencia.ESPORADICA -> "Pelada Esporádica"
        }
    }

    /**
     * Retorna a URL do Google Maps para este local, se houver coordenadas disponíveis
     */
    fun getMapUrl(): String? {
        return if (latitude != null && longitude != null) {
            "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
        } else {
            null
        }
    }

    /**
     * Retorna o texto formatado para compartilhamento
     */
    fun getTextoCompartilhamento(mensagemAdicional: String? = null): String {
        val sb = StringBuilder()

        sb.append("🏆 Pelada: $nome\n\n")

        // Adicionar informação de recorrência
        sb.append(when (tipoRecorrencia) {
            TipoRecorrencia.RECORRENTE -> "📅 Todo(a) ${DiaSemana.getNome(diaSemana ?: DiaSemana.DOMINGO)}\n"
            TipoRecorrencia.ESPORADICA -> "📅 Dias a combinar\n"
        })

        // Adicionar horário
        sb.append("🕒 Horário: ")
        try {
            if (horario.contains("T")) {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                val dateTime = LocalDateTime.parse(horario, formatter)
                sb.append(dateTime.format(DateTimeFormatter.ofPattern("HH:mm")))
            } else {
                sb.append(horario)
            }
        } catch (e: Exception) {
            sb.append(horario)
        }
        sb.append("\n")

        // Adicionar local
        sb.append("📍 Local: $local\n")

        // Se tiver coordenadas, adicionar link do Google Maps
        if (latitude != null && longitude != null) {
            sb.append("🗺️ Mapa: ${getMapUrl()}\n")
        }

        // Se tiver descrição, adicionar
        if (!descricao.isNullOrBlank()) {
            sb.append("\n📝 $descricao\n")
        }

        // Adicionar mensagem adicional personalizada, se houver
        if (!mensagemAdicional.isNullOrBlank()) {
            sb.append("\n$mensagemAdicional\n")
        }

        return sb.toString()
    }

    companion object {
        // Imagens padrão para grupos sem imagem personalizada
        val IMAGENS_PADRAO = listOf(
            "ic_pelada_default_1",
            "ic_pelada_default_2",
            "ic_pelada_default_3",
            "ic_pelada_default_4",
            "ic_pelada_default_5"
        )
    }
}
