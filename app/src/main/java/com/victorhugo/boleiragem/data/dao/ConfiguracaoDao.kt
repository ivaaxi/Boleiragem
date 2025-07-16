package com.victorhugo.boleiragem.data.dao

import androidx.room.*
import com.victorhugo.boleiragem.data.model.ConfiguracaoSorteio
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfiguracaoDao {
    @Query("SELECT * FROM configuracao_sorteio WHERE id = 1")
    fun getConfiguracao(): Flow<ConfiguracaoSorteio?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarConfiguracao(configuracao: ConfiguracaoSorteio)
}
