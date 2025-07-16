package com.victorhugo.boleiragem.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.victorhugo.boleiragem.data.model.ConfiguracaoPontuacao
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfiguracaoPontuacaoDao {
    @Query("SELECT * FROM configuracao_pontuacao WHERE id = 1")
    fun getConfiguracaoPontuacao(): Flow<ConfiguracaoPontuacao>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirConfiguracaoPontuacao(configuracaoPontuacao: ConfiguracaoPontuacao)

    @Update
    suspend fun atualizarConfiguracaoPontuacao(configuracaoPontuacao: ConfiguracaoPontuacao)
}
