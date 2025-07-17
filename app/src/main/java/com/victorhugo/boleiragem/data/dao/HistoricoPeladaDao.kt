package com.victorhugo.boleiragem.data.dao

import androidx.room.*
import com.victorhugo.boleiragem.data.model.HistoricoPelada
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoricoPeladaDao {
    @Query("SELECT * FROM historico_pelada ORDER BY dataFinalizacao DESC")
    fun getHistoricoPartidas(): Flow<List<HistoricoPelada>>

    @Insert
    suspend fun inserirHistoricoPelada(historicoPelada: HistoricoPelada): Long

    @Query("DELETE FROM historico_pelada WHERE id = :peladaId")
    suspend fun deletarHistoricoPelada(peladaId: Long)

    @Query("DELETE FROM historico_pelada")
    suspend fun limparHistorico()
}
