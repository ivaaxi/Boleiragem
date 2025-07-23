package com.victorhugo.boleiragem.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.victorhugo.boleiragem.data.dao.ConfiguracaoDao
import com.victorhugo.boleiragem.data.dao.ConfiguracaoPontuacaoDao
import com.victorhugo.boleiragem.data.dao.HistoricoPeladaDao
import com.victorhugo.boleiragem.data.dao.HistoricoTimeDao
import com.victorhugo.boleiragem.data.dao.JogadorDao
import com.victorhugo.boleiragem.data.model.ConfiguracaoPontuacao
import com.victorhugo.boleiragem.data.model.ConfiguracaoSorteio
import com.victorhugo.boleiragem.data.model.HistoricoPelada
import com.victorhugo.boleiragem.data.model.HistoricoTime
import com.victorhugo.boleiragem.data.model.Jogador

@Database(
    entities = [
        Jogador::class,
        ConfiguracaoSorteio::class,
        HistoricoTime::class,
        ConfiguracaoPontuacao::class,
        HistoricoPelada::class
    ],
    version = 9, // Incrementado de 8 para 9
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BoleiragemDatabase : RoomDatabase() {
    abstract fun jogadorDao(): JogadorDao
    abstract fun configuracaoDao(): ConfiguracaoDao
    abstract fun historicoTimeDao(): HistoricoTimeDao
    abstract fun configuracaoPontuacaoDao(): ConfiguracaoPontuacaoDao
    abstract fun historicoPeladaDao(): HistoricoPeladaDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Criar a nova tabela historico_time com a estrutura correta
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `historico_time` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `nome` TEXT NOT NULL,
                        `vitorias` INTEGER NOT NULL,
                        `derrotas` INTEGER NOT NULL,
                        `empates` INTEGER NOT NULL,
                        `dataUltimoSorteio` INTEGER NOT NULL,
                        `jogadoresIds` TEXT NOT NULL
                    )
                    """
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. Adicionar novas colunas na tabela jogadores
                database.execSQL("ALTER TABLE jogadores ADD COLUMN totalJogos INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE jogadores ADD COLUMN vitorias INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE jogadores ADD COLUMN derrotas INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE jogadores ADD COLUMN empates INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE jogadores ADD COLUMN pontuacaoTotal INTEGER NOT NULL DEFAULT 0")

                // 2. Criar a tabela de configuração de pontuação
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `configuracao_pontuacao` (
                        `id` INTEGER PRIMARY KEY NOT NULL,
                        `pontosPorVitoria` INTEGER NOT NULL,
                        `pontosPorDerrota` INTEGER NOT NULL,
                        `pontosPorEmpate` INTEGER NOT NULL
                    )
                    """
                )

                // 3. Inserir a configuração de pontuação padrão
                database.execSQL(
                    """
                    INSERT INTO `configuracao_pontuacao` (`id`, `pontosPorVitoria`, `pontosPorDerrota`, `pontosPorEmpate`)
                    VALUES (1, 10, -10, -5)
                    """
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. Criar uma tabela temporária com a nova estrutura
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `configuracao_sorteio_temp` (
                        `id` INTEGER PRIMARY KEY NOT NULL,
                        `qtdJogadoresPorTime` INTEGER NOT NULL,
                        `qtdTimes` INTEGER NOT NULL,
                        `aleatorio` INTEGER NOT NULL DEFAULT 1,
                        `criteriosExtras` TEXT NOT NULL DEFAULT '[]'
                    )
                    """
                )

                // 2. Copiar os dados da tabela antiga para a nova,
                //    assumindo que aleatorio é true (1) por padrão
                database.execSQL(
                    """
                    INSERT INTO configuracao_sorteio_temp (id, qtdJogadoresPorTime, qtdTimes)
                    SELECT id, qtdJogadoresPorTime, qtdTimes FROM configuracao_sorteio
                    """
                )

                // 3. Apagar a tabela antiga
                database.execSQL("DROP TABLE configuracao_sorteio")

                // 4. Renomear a tabela temporária
                database.execSQL("ALTER TABLE configuracao_sorteio_temp RENAME TO configuracao_sorteio")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Migração da versão 4 para 5
                // Se não houver alterações estruturais necessárias, podemos deixar vazio
                // Esta migração apenas atualiza o número da versão para corresponder ao novo schema
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Adiciona as novas colunas à tabela historico_time
                database.execSQL("ALTER TABLE historico_time ADD COLUMN mediaEstrelas REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE historico_time ADD COLUMN mediaPontuacao REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE historico_time ADD COLUMN isUltimoPelada INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Criar a tabela de histórico de peladas
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `historico_pelada` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `dataFinalizacao` INTEGER NOT NULL,
                        `times` TEXT NOT NULL
                    )
                    """
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Adicionar coluna 'disponivel' à tabela 'jogadores' com valor padrão de 1 (true)
                database.execSQL("ALTER TABLE jogadores ADD COLUMN disponivel INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Adicionar a coluna ehTimeReserva à tabela historico_time com valor padrão 0 (false)
                database.execSQL("ALTER TABLE `historico_time` ADD COLUMN `ehTimeReserva` INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
