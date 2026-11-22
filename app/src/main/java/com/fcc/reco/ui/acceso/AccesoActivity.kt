package com.fcc.reco.ui.acceso

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.fcc.reco.R
import com.fcc.reco.data.Repositorios
import com.fcc.reco.data.model.Sesion
import com.fcc.reco.data.remote.Nube
import com.fcc.reco.ui.comun.edgeToEdgeClaro
import io.github.jan.supabase.auth.handleDeeplinks
import kotlinx.coroutines.launch

/**
 * Activity de acceso. Decide a dónde ir según la sesión guardada:
 * sin sesión -> Bienvenida; sin verificar -> Verificar; con todo listo -> abre la app.
 * También recibe el deep link `reco://verificado` de vuelta del login institucional.
 */
class AccesoActivity : AppCompatActivity() {

    private val viewModel: AccesoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        edgeToEdgeClaro()

        val sesion = viewModel.sesion()
        if (sesion?.completa == true && intent.data == null) {
            // Sesión guardada: se cargan los datos mientras se ve la pantalla de arranque.
            splash.setKeepOnScreenCondition { true }
            lifecycleScope.launch {
                if (viewModel.preparar()) {
                    abrirAppYTerminar()
                } else {
                    // Sin sesión de Supabase o sin red: se vuelve a la Bienvenida.
                    Repositorios.cerrarSesion()
                    startActivity(Intent(this@AccesoActivity, AccesoActivity::class.java))
                    finish()
                }
            }
            return
        }

        setContentView(R.layout.activity_acceso)
        if (savedInstanceState == null) {
            val navController = (supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
            val grafo = navController.navInflater.inflate(R.navigation.nav_acceso)
            grafo.setStartDestination(destinoInicial(sesion))
            navController.graph = grafo
            manejarEnlace(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        manejarEnlace(intent)
    }

    /** `reco://auth` = regreso de Microsoft (Supabase Auth); `reco://verificado` = portal simulado. */
    private fun manejarEnlace(intent: Intent) {
        val datos = intent.data ?: return
        if (datos.scheme == "reco" && datos.host == "auth") {
            val error = datos.getQueryParameter("error") ?: datos.fragment?.takeIf { "error=" in it }
            if (error != null) viewModel.errorDeRegreso()
            else Nube.cliente.handleDeeplinks(intent, onSessionSuccess = { viewModel.microsoftDeVuelta() }, onError = { viewModel.errorDeRegreso() })
        } else {
            viewModel.procesarDeepLink(datos)
        }
    }

    private fun destinoInicial(sesion: Sesion?): Int = when {
        sesion == null -> R.id.bienvenida
        else -> R.id.verificarEstudiante
    }
}
