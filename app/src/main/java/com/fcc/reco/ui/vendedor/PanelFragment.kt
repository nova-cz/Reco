package com.fcc.reco.ui.vendedor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.databinding.FragmentPanelBinding
import com.fcc.reco.databinding.ItemAnuncioPanelBinding
import com.fcc.reco.ui.comun.CuadriculaProductos
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.comun.comoPrecio
import com.fcc.reco.ui.comun.estilo
import com.fcc.reco.ui.comun.iconoInicio
import com.fcc.reco.ui.comun.irA
import com.fcc.reco.ui.comun.mostrarFoto
import com.fcc.reco.ui.comun.ponerEstado
import kotlinx.coroutines.launch

/** Panel del vendedor: ganancias, contadores, próxima entrega y productos en venta (Figma 7:307). */
class PanelFragment : Fragment(R.layout.fragment_panel) {

    private val viewModel: PanelViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentPanelBinding.bind(view)
        b.raiz.ajustarPorBarrasDelSistema(arriba = true, abajo = true)
        val nav = findNavController()
        b.botonNotificaciones.setOnClickListener { Toast.makeText(requireContext(), R.string.proximamente, Toast.LENGTH_SHORT).show() }
        b.verTodo.setOnClickListener { nav.irA(R.id.anuncios) }
        b.tarjetaProxima.setOnClickListener { nav.irA(R.id.ventas) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estado.collect { e ->
                    b.avatar.configurarConNombre(e.nombre, ContextCompat.getColor(requireContext(), R.color.naranja), ContextCompat.getColor(requireContext(), R.color.rosa))
                    b.saludo.text = getString(R.string.panel_hola, e.nombre.substringBefore(' '))
                    b.subtitulo.text = e.reputacion?.let { getString(R.string.panel_vendedor_verificado_rep, it) } ?: getString(R.string.panel_vendedor_verificado)
                    b.gananciasTitulo.text = getString(R.string.panel_ganancias, e.mes)
                    b.chipVentas.text = getString(R.string.panel_mas_ventas, e.ventasMes)
                    b.chipVentas.visibility = if (e.ventasMes > 0) View.VISIBLE else View.GONE
                    b.gananciasTotal.text = e.ganancias.comoPrecio()
                    b.disponible.text = e.disponible.comoPrecio()
                    b.retenido.text = e.retenido.comoPrecio()
                    b.nActivos.text = e.activos.toString()
                    b.nRevision.text = e.enRevision.toString()
                    b.nVendidos.text = e.vendidos.toString()

                    val proxima = e.proxima
                    b.tarjetaProxima.visibility = if (proxima == null) View.GONE else View.VISIBLE
                    if (proxima != null) {
                        b.proximaTitulo.text = getString(R.string.panel_proxima, e.proximaTitulo)
                        b.proximaDetalle.text = getString(R.string.pedido_resumen_entrega, proxima.puntoEntrega, proxima.horario)
                        b.proximaDetalle.iconoInicio(R.drawable.ic_ubicacion, 14, ContextCompat.getColor(requireContext(), R.color.naranja))
                        val categoria = proxima.productoIds.firstOrNull()?.let { com.fcc.reco.data.Repositorios.productos.obtenerPorId(it)?.categoria }
                        categoria?.let { b.proximaIcono.setImageResource(it.icono) }
                    }

                    b.lista.removeAllViews()
                    b.vacio.visibility = if (e.recientes.isEmpty()) View.VISIBLE else View.GONE
                    val inflater = LayoutInflater.from(requireContext())
                    CuadriculaProductos.armarFilas(b.lista, e.recientes, igualarAlturas = false) { fila, item ->
                        val c = ItemAnuncioPanelBinding.inflate(inflater, fila, false)
                        c.titulo.text = item.anuncio.titulo
                        c.precio.text = item.anuncio.precio.comoPrecio()
                        c.foto.mostrarFoto(item.anuncio.fotos, item.anuncio.categoria.icono)
                        c.chipEstado.ponerEstado(item.estado.etiqueta, item.estado.estilo)
                        c.root.setOnClickListener { nav.irA(R.id.anuncios) }
                        c.root
                    }
                }
            }
        }
    }
}
