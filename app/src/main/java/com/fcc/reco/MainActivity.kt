package com.fcc.reco

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.comun.edgeToEdgeClaro

/** Marcador temporal. En la Fase 2 pasa a ser AccesoActivity. */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        edgeToEdgeClaro()
        setContentView(R.layout.activity_main)
        findViewById<android.view.View>(R.id.main).ajustarPorBarrasDelSistema()
    }
}
