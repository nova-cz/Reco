package com.fcc.reco.ui.comprador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.databinding.FragmentCarritoBinding
import com.fcc.reco.databinding.ItemGrupoCarritoBinding
import com.fcc.reco.databinding.ItemLineaCarritoBinding
import com.fcc.reco.ui.comun.ChipInsignia
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.comun.comoPrecio
import com.fcc.reco.ui.comun.configurarVendedor
import com.fcc.reco.ui.comun.irA
import com.fcc.reco.ui.comun.sumarMargenInferiorDeBarras
import kotlinx.coroutines.launch

/** Carrito agrupado por vendedor (un pedido por vendedor). */
class CarritoFragment : Fragment(R.layout.fragment_carrito) {

    private val viewModel: CarritoViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentCarritoBinding.bind(view)
        b.scroll.ajustarPorBarrasDelSistema(arriba = true, abajo = false)
        b.barraTotal.sumarMargenInferiorDeBarras(16)
        b.barraTotal.vincularFondo(b.scroll)
        val nav = findNavController()

        b.botonAtras.setOnClickListener { if (!nav.popBackStack()) nav.irA(R.id.inicio) }
        b.botonExplorar.setOnClickListener { nav.irA(R.id.inicio, popHasta = R.id.inicio) }
        b.botonContinuar.setOnClickListener { nav.irA(R.id.entregaPago) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estado.collect { pintar(b, it) }
            }
        }
    }

    private fun pintar(b: FragmentCarritoBinding, estado: EstadoCarrito) {
        val vacio = estado.grupos.isEmpty()
        b.estadoVacio.visibility = if (vacio) View.VISIBLE else View.GONE
        b.tarjetaTotales.visibility = if (vacio) View.GONE else View.VISIBLE
        b.barraTotal.visibility = if (vacio) View.GONE else View.VISIBLE
        b.contador.visibility = if (vacio) View.INVISIBLE else View.VISIBLE
        b.contador.text = estado.cantidad.toString()

        b.subtotalEtiqueta.text = if (estado.cantidad == 1) getString(R.string.subtotal_producto)
        else getString(R.string.subtotal_productos, estado.cantidad)
        b.subtotalValor.text = estado.total.comoPrecio()
        b.totalValor.text = estado.total.comoPrecio()
        b.barraTotalValor.text = estado.total.comoPrecio()

        val inflater = LayoutInflater.from(requireContext())
        b.contenedorGrupos.removeAllViews()
        estado.grupos.forEachIndexed { indice, grupo ->
            val g = ItemGrupoCarritoBinding.inflate(inflater, b.contenedorGrupos, false)
            g.avatar.configurarVendedor(grupo.vendedor)
            g.vendedor.text = grupo.vendedor.nombre
            g.subtitulo.text = getString(
                R.string.pedido_numero, indice + 1, grupo.vendedor.carrera, grupo.vendedor.facultad
            )
            grupo.productos.forEach { p ->
                val l = ItemLineaCarritoBinding.inflate(inflater, g.lineas, false)
                l.icono.setImageResource(p.categoria.icono)
                l.titulo.text = p.titulo
                l.precio.text = p.precio.comoPrecio()
                l.chipGrado.configurar(ChipInsignia.Tipo.GRADO, getString(R.string.grado_corto, p.grado.letra))
                val bateria = p.saludBateria
                if (bateria != null) {
                    l.chipBateria.configurar(ChipInsignia.Tipo.NEUTRO_GRIS, "$bateria%", R.drawable.ic_bateria)
                } else {
                    l.chipBateria.visibility = View.GONE
                }
                l.botonQuitar.setOnClickListener { viewModel.quitar(p.id) }
                g.lineas.addView(l.root)
            }
            b.contenedorGrupos.addView(g.root, android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { if (indice > 0) topMargin = b.root.resources.getDimensionPixelSize(R.dimen.espacio_m) })
        }
    }
}
