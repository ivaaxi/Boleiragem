package com.victorhugo.boleiragem

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Classe de aplicação principal com suporte a injeção de dependências via Hilt
 */
@HiltAndroidApp
class BoleiragemApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializações globais do aplicativo podem ser feitas aqui
    }
}
