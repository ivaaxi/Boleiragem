package com.victorhugo.boleiragem.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.victorhugo.boleiragem.data.model.CriterioSorteio
import com.victorhugo.boleiragem.data.model.HistoricoTimeSnapshot
import com.victorhugo.boleiragem.data.model.PosicaoJogador

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromPosicaoJogador(posicao: PosicaoJogador?): String? {
        return posicao?.name
    }

    @TypeConverter
    fun toPosicaoJogador(posicaoString: String?): PosicaoJogador? {
        return posicaoString?.let { PosicaoJogador.valueOf(it) }
    }

    @TypeConverter
    fun fromSetCriterios(criterios: Set<CriterioSorteio>): String {
        return criterios.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toSetCriterios(criteriosString: String): Set<CriterioSorteio> {
        return if (criteriosString.isEmpty() || criteriosString == "[]") {
            emptySet()
        } else {
            criteriosString.split(",")
                .filter { it.isNotEmpty() && it != "[]" }
                .map { CriterioSorteio.valueOf(it.trim()) }
                .toSet()
        }
    }

    @TypeConverter
    fun fromLongList(list: List<Long>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun toLongList(data: String): List<Long> {
        return if (data.isEmpty() || data == "[]") {
            emptyList()
        } else {
            data.split(",")
                .filter { it.isNotEmpty() && it != "[]" }
                .map { it.toLong() }
        }
    }

    @TypeConverter
    fun fromHistoricoTimeSnapshotList(value: List<HistoricoTimeSnapshot>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toHistoricoTimeSnapshotList(value: String): List<HistoricoTimeSnapshot> {
        val listType = object : TypeToken<List<HistoricoTimeSnapshot>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // Conversores para DiaSemana
    @TypeConverter
    fun fromDiaSemanaList(diasSemana: List<com.victorhugo.boleiragem.data.model.DiaSemana>): String {
        return diasSemana.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toDiaSemanaList(diasSemanaString: String): List<com.victorhugo.boleiragem.data.model.DiaSemana> {
        return if (diasSemanaString.isEmpty() || diasSemanaString == "[]") {
            emptyList()
        } else {
            diasSemanaString.split(",")
                .filter { it.isNotEmpty() && it != "[]" }
                .map { com.victorhugo.boleiragem.data.model.DiaSemana.valueOf(it.trim()) }
        }
    }
}
