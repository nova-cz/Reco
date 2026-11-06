package com.fcc.reco.ui.acceso

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import com.fcc.reco.R
import com.fcc.reco.data.model.Sesion
import com.fcc.reco.ui.comun.edgeToEdgeClaro

/**
 * Activity de acceso. Decide a dónde ir según la sesión guardada:
 * sin sesión -> Bienvenida; sin verificar -> Verificar; con todo listo -> abre el último modo.
 * También recibe el deep link `reco://verificado` de vuelta del login institucional.
 */
class AccesoActivity : AppCompatActivity() {

    private val viewModel: AccesoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        edgeToEdgeClaro()

        val sesion = viewModel.sesion()
        if (sesion?.completa == true && intent.data == null) {
            abrirModoYTerminar(sesion.ultimoModo!!)
            return
        }

        setContentView(R.layout.activity_acceso)
        if (savedInstanceState == null) {
            val navController = (supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
            val grafo = navController.navInflater.inflate(R.navigation.nav_acceso)
            grafo.setStartDestination(destinoInicial(sesion))
            navController.graph = grafo
            intent.data?.let { viewModel.procesarDeepLink(it) }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let { viewModel.procesarDeepLink(it) }
    }

    private fun destinoInicial(sesion: Sesion?): Int = when {
        sesion == null -> R.id.bienvenida
        !sesion.verificado || sesion.carrera == null -> R.id.verificarEstudiante
        else -> R.id.elegirModo
    }
}
