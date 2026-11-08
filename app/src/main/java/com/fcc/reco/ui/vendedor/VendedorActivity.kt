package com.fcc.reco.ui.vendedor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.fcc.reco.R
import com.fcc.reco.databinding.ActivityVendedorBinding
import com.fcc.reco.ui.comun.ItemBarra
import com.fcc.reco.ui.comun.edgeToEdgeClaro
import com.fcc.reco.ui.comun.irA
import com.fcc.reco.ui.comun.sumarMargenInferiorDeBarras

/**
 * Modo vendedor (cascarón): barra con Panel, Anuncios, "+" naranja, Ventas y Perfil.
 * Las pantallas reales (Panel, Publicar, Anuncios, Ventas) llegan en una fase posterior.
 */
class VendedorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        edgeToEdgeClaro()
        val b = ActivityVendedorBinding.inflate(layoutInflater)
        setContentView(b.root)

        val nav = (supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
        b.barra.configurar(
            listOf(
                ItemBarra(R.id.panel, R.drawable.ic_panel, R.string.tab_panel),
                ItemBarra(R.id.anuncios, R.drawable.ic_anuncios, R.string.tab_anuncios),
                ItemBarra(R.id.accionPublicar, R.drawable.ic_mas, R.string.tab_publicar, esAccion = true),
                ItemBarra(R.id.ventas, R.drawable.ic_ventas, R.string.tab_ventas),
                ItemBarra(R.id.perfilVendedor, R.drawable.ic_perfil, R.string.tab_perfil)
            )
        )
        b.barra.vincularFondo(b.navHost)
        b.barra.vincular(nav)
        b.barra.sumarMargenInferiorDeBarras(16)
        // El asistente de publicar y el registro traen su propio botón inferior.
        val sinBarra = setOf(R.id.publicar1, R.id.publicar2, R.id.publicar3, R.id.publicar4, R.id.publicar5)
        nav.addOnDestinationChangedListener { _, destino, _ ->
            b.barra.visibility = if (destino.id in sinBarra) android.view.View.GONE else android.view.View.VISIBLE
        }
        b.barra.alSeleccionar = { item ->
            if (item.esAccion) nav.irA(R.id.publicar1, android.os.Bundle().apply { putBoolean("nuevo", true) })
        }
    }
}
