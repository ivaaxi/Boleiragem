package com.victorhugo.boleiragem.data.repository

import com.victorhugo.boleiragem.data.dao.ConfiguracaoDao
import com.victorhugo.boleiragem.data.model.ConfiguracaoSorteio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfiguracaoRepository @Inject constructor(
    private val configuracaoDao: ConfiguracaoDao
) {
    // Obtém a configuração padrão atual
    fun getConfiguracao(): Flow<ConfiguracaoSorteio> =
        configuracaoDao.getConfiguracaoPadrao().map { it ?: criarConfiguracaoPadrao() }

    // Obtém todos os perfis de configuração
    fun getTodasConfiguracoes(): Flow<List<ConfiguracaoSorteio>> =
        configuracaoDao.getTodasConfiguracoes()

    // Obtém uma configuração específica pelo ID
    suspend fun getConfiguracaoById(id: Long): ConfiguracaoSorteio? =
        configuracaoDao.getConfiguracaoById(id)

    // Salva uma configuração (nova ou existente)
    suspend fun salvarConfiguracao(configuracao: ConfiguracaoSorteio): Long =
        configuracaoDao.salvarConfiguracao(configuracao)

    // Remove uma configuração pelo ID
    suspend fun deletarConfiguracao(id: Long) {
        configuracaoDao.deletarConfiguracao(id)
    }

    // Define uma configuração como padrão
    suspend fun definirConfiguracaoPadrao(id: Long) {
        configuracaoDao.definirConfiguracaoPadrao(id)
    }

    // Cria e salva uma configuração padrão se não existir nenhuma
    private suspend fun criarConfiguracaoPadrao(): ConfiguracaoSorteio {
        val configPadrao = ConfiguracaoSorteio(
            nome = "Padrão",
            qtdJogadoresPorTime = 5,
            qtdTimes = 2,
            aleatorio = true,
            isPadrao = true
        )
        configuracaoDao.salvarConfiguracao(configPadrao)
        return configPadrao
    }
}
