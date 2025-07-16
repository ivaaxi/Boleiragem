package com.victorhugo.boleiragem.di

import android.content.Context
import androidx.room.Room
import com.victorhugo.boleiragem.data.dao.ConfiguracaoDao
import com.victorhugo.boleiragem.data.dao.ConfiguracaoPontuacaoDao
import com.victorhugo.boleiragem.data.dao.HistoricoTimeDao
import com.victorhugo.boleiragem.data.dao.JogadorDao
import com.victorhugo.boleiragem.data.db.BoleiragemDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideBoleiragemDatabase(@ApplicationContext context: Context): BoleiragemDatabase {
        return Room.databaseBuilder(
            context,
            BoleiragemDatabase::class.java,
            "boleiragem_database"
        )
        .addMigrations(
            BoleiragemDatabase.MIGRATION_1_2,
            BoleiragemDatabase.MIGRATION_2_3,
            BoleiragemDatabase.MIGRATION_3_4,
            BoleiragemDatabase.MIGRATION_4_5,
            BoleiragemDatabase.MIGRATION_5_6
        )
        .build()
    }

    @Provides
    fun provideJogadorDao(database: BoleiragemDatabase): JogadorDao {
        return database.jogadorDao()
    }

    @Provides
    fun provideConfiguracaoDao(database: BoleiragemDatabase): ConfiguracaoDao {
        return database.configuracaoDao()
    }

    @Provides
    fun provideHistoricoTimeDao(database: BoleiragemDatabase): HistoricoTimeDao {
        return database.historicoTimeDao()
    }

    @Provides
    fun provideConfiguracaoPontuacaoDao(database: BoleiragemDatabase): ConfiguracaoPontuacaoDao {
        return database.configuracaoPontuacaoDao()
    }
}
