package com.fcc.reco.ui.comprador

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import com.fcc.reco.R
import com.fcc.reco.data.Repositorios
import com.fcc.reco.databinding.ActivityCompradorBinding
import com.fcc.reco.ui.comun.ItemBarra
import com.fcc.reco.ui.comun.edgeToEdgeClaro
import com.fcc.reco.ui.comun.sumarMargenInferiorDeBarras
import kotlinx.coroutines.launch

/** Modo comprador: NavHost con su grafo y la barra inferior flotante (Inicio, Favoritos, Carrito, Compras, Perfil). */
class CompradorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        edgeToEdgeClaro()
        val b = ActivityCompradorBinding.inflate(layoutInflater)
        setContentView(b.root)

        val nav = (supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
        b.barra.configurar(
            listOf(
                ItemBarra(R.id.inicio, R.drawable.ic_inicio, R.string.tab_inicio),
                ItemBarra(R.id.favoritos, R.drawable.ic_favorito, R.string.tab_favoritos),
                ItemBarra(R.id.carrito, R.drawable.ic_carrito, R.string.tab_carrito),
                ItemBarra(R.id.compras, R.drawable.ic_compras, R.string.tab_compras),
                ItemBarra(R.id.perfil, R.drawable.ic_perfil, R.string.tab_perfil)
            )
        )
        b.barra.vincularFondo(b.navHost)
        b.barra.vincular(nav)
        b.barra.sumarMargenInferiorDeBarras(16)

        // En estas pantallas la barra inferior se reemplaza por su propia barra (precio, total o pagar).
        val sinBarra = setOf(R.id.detalleProducto, R.id.carrito, R.id.entregaPago, R.id.compraConfirmada, R.id.detallePedido, R.id.registroVendedor)
        nav.addOnDestinationChangedListener { _, destino, _ ->
            b.barra.visibility = if (destino.id in sinBarra) View.GONE else View.VISIBLE
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                Repositorios.carrito.ids.collect { b.barra.ponerInsignia(R.id.carrito, it.size) }
            }
        }
    }
}
