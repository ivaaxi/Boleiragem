package com.victorhugo.boleiragem.data.dao

import androidx.room.*
import com.victorhugo.boleiragem.data.model.ConfiguracaoSorteio
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfiguracaoDao {
    // Obtém a configuração padrão atual
    @Query("SELECT * FROM configuracao_sorteio WHERE isPadrao = 1 LIMIT 1")
    fun getConfiguracaoPadrao(): Flow<ConfiguracaoSorteio?>

    // Obtém todos os perfis de configuração
    @Query("SELECT * FROM configuracao_sorteio ORDER BY nome ASC")
    fun getTodasConfiguracoes(): Flow<List<ConfiguracaoSorteio>>

    // Obtém uma configuração específica pelo ID
    @Query("SELECT * FROM configuracao_sorteio WHERE id = :id")
    suspend fun getConfiguracaoById(id: Long): ConfiguracaoSorteio?

    // Insere ou atualiza uma configuração
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarConfiguracao(configuracao: ConfiguracaoSorteio): Long

    // Remove uma configuração pelo ID
    @Query("DELETE FROM configuracao_sorteio WHERE id = :id")
    suspend fun deletarConfiguracao(id: Long)

    // Define uma configuração como padrão e remove o status padrão das outras
    @Transaction
    suspend fun definirConfiguracaoPadrao(id: Long) {
        // Remove o status padrão de todas as configurações
        resetTodasConfiguracoesNaoPadrao()

        // Define a configuração selecionada como padrão
        marcarConfiguracoComoPadrao(id)
    }

    @Query("UPDATE configuracao_sorteio SET isPadrao = 0")
    suspend fun resetTodasConfiguracoesNaoPadrao()

    @Query("UPDATE configuracao_sorteio SET isPadrao = 1 WHERE id = :id")
    suspend fun marcarConfiguracoComoPadrao(id: Long)
}
