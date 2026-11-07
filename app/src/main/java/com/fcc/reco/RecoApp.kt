package com.fcc.reco

import android.app.Application
import com.fcc.reco.data.Repositorios

class RecoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Repositorios.iniciar(this)
    }
}
