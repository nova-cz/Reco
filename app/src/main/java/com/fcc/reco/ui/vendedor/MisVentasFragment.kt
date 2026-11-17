package com.fcc.reco.ui.vendedor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.data.Repositorios
import com.fcc.reco.data.model.EstadoAnuncio
import com.fcc.reco.data.model.EstadoPedido
import com.fcc.reco.data.model.Pedido
import com.fcc.reco.databinding.FragmentMisVentasBinding
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

/**
 * Mis ventas (Figma 48:1552): ganancias, contadores, "Publicar un equipo", próxima entrega y Mis anuncios.
 * Tocar un anuncio abre sus acciones (pausar, reactivar, corregir, continuar, eliminar).
 */
class MisVentasFragment : Fragment(R.layout.fragment_mis_ventas) {

    private val viewModel: MisVentasViewModel by viewModels()

    private val motivosDeRechazo = listOf("Fotos borrosas", "Precio fuera de rango", "Información incompleta")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentMisVentasBinding.bind(view)
        b.raiz.ajustarPorBarrasDelSistema(arriba = true, abajo = true)
        val nav = findNavController()
        b.botonNotificaciones.setOnClickListener { Toast.makeText(requireContext(), R.string.proximamente, Toast.LENGTH_SHORT).show() }
        b.botonPublicar.setOnClickListener { nav.irA(R.id.elegirModelo, Bundle().apply { putBoolean("nuevo", true) }) }
        b.verTodo.setOnClickListener { viewModel.alternarVerTodo() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estado.collect { e ->
                    b.avatar.configurarConNombre(e.nombre, ContextCompat.getColor(requireContext(), R.color.naranja), ContextCompat.getColor(requireContext(), R.color.rosa))
                    b.saludo.setText(R.string.mis_ventas)
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
                        val categoria = proxima.productoIds.firstOrNull()?.let { Repositorios.productos.obtenerPorId(it)?.categoria }
                        categoria?.let { b.proximaIcono.setImageResource(it.icono) }
                        b.tarjetaProxima.setOnClickListener { abrirEntrega(proxima) }
                    }

                    b.verTodo.visibility = if (e.hayMas) View.VISIBLE else View.GONE
                    b.verTodo.setText(if (e.verTodo) R.string.ver_menos else R.string.panel_ver_todo)
                    b.lista.removeAllViews()
                    b.vacio.visibility = if (e.anuncios.isEmpty()) View.VISIBLE else View.GONE
                    val inflater = LayoutInflater.from(requireContext())
                    CuadriculaProductos.armarFilas(b.lista, e.anuncios, igualarAlturas = false) { fila, item ->
                        val c = ItemAnuncioPanelBinding.inflate(inflater, fila, false)
                        c.titulo.text = item.anuncio.titulo
                        c.precio.text = item.anuncio.precio.comoPrecio()
                        c.foto.mostrarFoto(item.anuncio.fotos, item.anuncio.categoria.icono)
                        c.chipEstado.ponerEstado(item.estado.etiqueta, item.estado.estilo)
                        c.root.setOnClickListener { mostrarAcciones(c.root, item) }
                        c.root
                    }
                }
            }
        }
    }

    /** Marcar entregado (vendedor); con el pago ya entregado solo se espera al comprador. */
    private fun abrirEntrega(p: Pedido) {
        when (p.estado) {
            EstadoPedido.PAGADO_RETENIDO -> AlertDialog.Builder(requireContext())
                .setTitle(R.string.entrega_pregunta)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.entrega_marcar) { _, _ -> viewModel.marcarEntregado(p.id) }
                .show()
            EstadoPedido.ENTREGADO ->
                if (esDebug()) {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.entrega_esperando)
                        .setNegativeButton(R.string.cancelar, null)
                        .setPositiveButton(R.string.debug_simular_confirmacion) { _, _ -> viewModel.simularConfirmacionDelComprador(p.id) }
                        .show()
                } else {
                    Toast.makeText(requireContext(), R.string.entrega_esperando, Toast.LENGTH_SHORT).show()
                }
            else -> Unit
        }
    }

    /** Menú de acciones de un anuncio según su estado; en debug añade moderación y compra simulada. */
    private fun mostrarAcciones(ancla: View, item: AnuncioConEstado) {
        val a = item.anuncio
        val acciones = mutableListOf<Pair<Int, () -> Unit>>()
        when (item.estado) {
            EstadoAnuncio.PUBLICADO -> acciones += R.string.anuncio_pausar to { viewModel.pausar(a.id) }
            EstadoAnuncio.PAUSADO -> acciones += R.string.anuncio_reactivar to { viewModel.reanudar(a.id) }
            EstadoAnuncio.RECHAZADO -> acciones += R.string.anuncio_corregir to { abrirAsistente(a.id) }
            EstadoAnuncio.BORRADOR -> acciones += R.string.anuncio_continuar to { abrirAsistente(a.id) }
            else -> Unit
        }
        if (esDebug()) {
            when (item.estado) {
                EstadoAnuncio.EN_REVISION -> {
                    acciones += R.string.debug_aprobar to { viewModel.aprobar(a.id) }
                    acciones += R.string.debug_rechazar to { elegirMotivo(ancla, a.id) }
                }
                EstadoAnuncio.PUBLICADO -> acciones += R.string.debug_simular_compra to { viewModel.simularCompra(a) }
                else -> Unit
            }
        }
        if (item.estado != EstadoAnuncio.VENDIDO) acciones += R.string.eliminar to { confirmarEliminar(a.id) }
        if (acciones.isEmpty()) return
        PopupMenu(requireContext(), ancla).apply {
            acciones.forEachIndexed { i, (texto, _) -> menu.add(0, i, i, texto) }
            setOnMenuItemClickListener { acciones[it.itemId].second(); true }
            show()
        }
    }

    private fun abrirAsistente(anuncioId: Int) {
        findNavController().irA(R.id.elegirModelo, Bundle().apply { putInt("anuncioId", anuncioId) })
    }

    private fun elegirMotivo(ancla: View, id: Int) {
        PopupMenu(requireContext(), ancla).apply {
            motivosDeRechazo.forEachIndexed { i, m -> menu.add(0, i, i, m) }
            setOnMenuItemClickListener { viewModel.rechazar(id, motivosDeRechazo[it.itemId]); true }
            show()
        }
    }

    private fun confirmarEliminar(id: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.anuncio_eliminar_titulo)
            .setMessage(R.string.anuncio_eliminar_desc)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.eliminar) { _, _ -> viewModel.eliminar(id) }
            .show()
    }
}
