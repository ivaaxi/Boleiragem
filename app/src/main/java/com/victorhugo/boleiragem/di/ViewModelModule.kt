package com.victorhugo.boleiragem.di

// Este arquivo não é mais necessário, pois agora estamos usando o SorteioRepository
// para compartilhar dados entre os ViewModels, em vez de injetar um ViewModel em outro.
// Mantido como comentário apenas para referência futura.

/*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.victorhugo.boleiragem.ui.screens.sorteio.ResultadoSorteioViewModel
import com.victorhugo.boleiragem.ui.screens.sorteio.SorteioTimesViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    @ViewModelScoped
    fun provideResultadoSorteioViewModel(
        sorteioTimesViewModel: SorteioTimesViewModel
    ): ResultadoSorteioViewModel {
        return ResultadoSorteioViewModel(sorteioTimesViewModel)
    }
}
*/
