package com.fcc.reco.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.fcc.reco.R
import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.databinding.ActivityMainBinding
import com.fcc.reco.ui.comun.ItemBarra
import com.fcc.reco.ui.comun.edgeToEdgeClaro
import com.fcc.reco.ui.comun.irA
import com.fcc.reco.ui.comun.sumarMargenInferiorDeBarras

/**
 * Única Activity de la app (sin modo vendedor): NavHost con `nav_main`, barra inferior flotante
 * (Inicio, Favoritos, Compras, Cuenta) y el botón "Vender" aparte.
 * "Vender" abre "Vende en Reco" si el estudiante aún no es vendedor, o "Mis ventas" si ya lo es.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        edgeToEdgeClaro()
        val b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        val nav = (supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
        b.barra.configurar(
            listOf(
                ItemBarra(R.id.inicio, R.drawable.ic_inicio, R.string.tab_inicio),
                ItemBarra(R.id.favoritos, R.drawable.ic_favorito, R.string.tab_favoritos),
                ItemBarra(R.id.compras, R.drawable.ic_compras, R.string.tab_compras),
                ItemBarra(R.id.cuenta, R.drawable.ic_perfil, R.string.tab_cuenta)
            )
        )
        b.barra.vincularFondo(b.navHost)
        b.barra.vincular(nav)
        b.barraContenedor.sumarMargenInferiorDeBarras(16)

        b.botonVender.setOnClickListener {
            val esVendedor = SesionLocal(this).leer()?.esVendedor == true
            nav.irA(if (esVendedor) R.id.misVentas else R.id.vendeEnReco)
        }

        // Estas pantallas traen su propia barra inferior (precio, pagar, confirmar...) o son un asistente.
        val sinBarra = setOf(
            R.id.detalleProducto, R.id.comprar, R.id.detallePedido, R.id.vendeEnReco,
            R.id.elegirModelo, R.id.publicar1, R.id.publicar2, R.id.publicar3
        )
        nav.addOnDestinationChangedListener { _, destino, _ ->
            b.barraContenedor.visibility = if (destino.id in sinBarra) View.GONE else View.VISIBLE
        }
    }
}
