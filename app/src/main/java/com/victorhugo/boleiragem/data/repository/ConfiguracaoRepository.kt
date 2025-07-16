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
    fun getConfiguracao(): Flow<ConfiguracaoSorteio> =
        configuracaoDao.getConfiguracao().map { it ?: ConfiguracaoSorteio() }

    suspend fun salvarConfiguracao(configuracao: ConfiguracaoSorteio) =
        configuracaoDao.salvarConfiguracao(configuracao)
}
