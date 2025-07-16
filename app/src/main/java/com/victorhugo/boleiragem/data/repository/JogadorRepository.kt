package com.victorhugo.boleiragem.data.repository

import com.victorhugo.boleiragem.data.dao.JogadorDao
import com.victorhugo.boleiragem.data.model.Jogador
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JogadorRepository @Inject constructor(
    private val jogadorDao: JogadorDao
) {
    fun getJogadores(): Flow<List<Jogador>> = jogadorDao.getJogadores()

    fun getJogadoresAtivos(): Flow<List<Jogador>> = jogadorDao.getJogadoresAtivos()

    suspend fun inserirJogador(jogador: Jogador): Long = jogadorDao.inserirJogador(jogador)

    suspend fun atualizarJogador(jogador: Jogador) = jogadorDao.atualizarJogador(jogador)

    suspend fun deletarJogador(jogador: Jogador) = jogadorDao.deletarJogador(jogador)

    suspend fun atualizarStatusJogador(id: Long, ativo: Boolean) =
        jogadorDao.atualizarStatusJogador(id, ativo)

    suspend fun getJogadorPorId(id: Long): Jogador? = jogadorDao.getJogadorPorId(id)

    // Métodos para registrar estatísticas de jogadores após as peladas
    suspend fun registrarVitoria(jogadoresIds: List<Long>, pontuacao: Int) {
        jogadorDao.registrarVitoria(jogadoresIds, pontuacao)
    }

    suspend fun registrarDerrota(jogadoresIds: List<Long>) {
        jogadorDao.registrarDerrota(jogadoresIds)
    }

    suspend fun registrarEmpate(jogadoresIds: List<Long>, pontuacao: Int) {
        jogadorDao.registrarEmpate(jogadoresIds, pontuacao)
    }
}
