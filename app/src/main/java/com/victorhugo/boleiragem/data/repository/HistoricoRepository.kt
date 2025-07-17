package com.victorhugo.boleiragem.data.repository

import com.victorhugo.boleiragem.data.dao.HistoricoPeladaDao
import com.victorhugo.boleiragem.data.model.HistoricoPelada
import com.victorhugo.boleiragem.data.model.HistoricoTime
import com.victorhugo.boleiragem.data.model.toSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoricoRepository @Inject constructor(
    private val historicoPeladaDao: HistoricoPeladaDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun getHistoricoPartidas(): Flow<List<HistoricoPelada>> {
        return historicoPeladaDao.getHistoricoPartidas()
    }

    fun salvarPeladaFinalizada(times: List<HistoricoTime>) {
        scope.launch {
            // Criamos um snapshot dos times para armazenar no histórico
            val timeSnapshots = times.map { it.toSnapshot() }

            // Criamos um novo registro de pelada finalizada
            val novaPelada = HistoricoPelada(
                dataFinalizacao = System.currentTimeMillis(),
                times = timeSnapshots
            )

            // Salvamos no banco de dados
            historicoPeladaDao.inserirHistoricoPelada(novaPelada)
        }
    }

    suspend fun deletarPelada(peladaId: Long) {
        historicoPeladaDao.deletarHistoricoPelada(peladaId)
    }
}
