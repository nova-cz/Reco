package com.fcc.reco.ui.vendedor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.fcc.reco.R
import com.fcc.reco.data.Repositorios
import com.fcc.reco.data.model.EstadoPedido
import com.fcc.reco.data.model.Pedido
import com.fcc.reco.databinding.FragmentVentasBinding
import com.fcc.reco.databinding.ItemVentaBinding
import com.fcc.reco.ui.comun.AvatarIniciales
import com.fcc.reco.ui.comun.EstiloEstado
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.comun.comoFechaCorta
import com.fcc.reco.ui.comun.comoPrecio
import com.fcc.reco.ui.comun.iconoInicio
import com.fcc.reco.ui.comun.mostrarFoto
import com.fcc.reco.ui.comun.ponerEstado
import kotlinx.coroutines.launch

/** Ventas: resumen, filtros y cada venta con su ganancia neta y "Marcar como entregado" (Figma 35:439). */
class VentasFragment : Fragment(R.layout.fragment_ventas) {

    private val viewModel: VentasViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentVentasBinding.bind(view)
        b.raiz.ajustarPorBarrasDelSistema(arriba = true, abajo = true)

        val chips = mapOf(
            FiltroVentas.POR_ENTREGAR to b.filtroPorEntregar,
            FiltroVentas.ENTREGADAS to b.filtroEntregadas,
            FiltroVentas.COMPLETADAS to b.filtroCompletadas
        )
        chips.forEach { (f, chip) -> chip.cerrarAlActivar = false; chip.setOnClickListener { viewModel.ponerFiltro(f) } }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estado.collect { e ->
                    chips.forEach { (f, chip) -> chip.activo = f == e.filtro }
                    b.filtroPorEntregar.text = getString(R.string.ventas_por_entregar, e.porEntregar)
                    b.porCobrar.text = e.resumen.porCobrar.comoPrecio()
                    b.disponible.text = e.resumen.disponible.comoPrecio()
                    b.comisiones.text = e.resumen.comisiones.comoPrecio()

                    b.lista.removeAllViews()
                    val inflater = LayoutInflater.from(requireContext())
                    e.ventas.forEach { b.lista.addView(crearVenta(inflater, b, it)) }
                    b.estadoVacio.visibility = if (e.ventas.isEmpty()) View.VISIBLE else View.GONE
                    b.vacioTitulo.setText(if (e.totalVentas == 0) R.string.ventas_vacio else R.string.ventas_filtro_vacio)
                    b.vacioDesc.visibility = if (e.totalVentas == 0) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun crearVenta(inflater: LayoutInflater, b: FragmentVentasBinding, p: Pedido): View {
        val c = ItemVentaBinding.inflate(inflater, b.lista, false)
        val producto = p.productoIds.firstOrNull()?.let { Repositorios.productos.obtenerPorId(it) }
        val colorAvatar = listOf(R.color.naranja to R.color.rosa, R.color.azul to R.color.verde_reco, R.color.rosa to R.color.azul)[p.comprador.length % 3]
        c.avatar.configurarConNombre(p.comprador, ContextCompat.getColor(requireContext(), colorAvatar.first), ContextCompat.getColor(requireContext(), colorAvatar.second))
        c.comprador.text = getString(R.string.venta_comprador, p.comprador)
        c.pedido.text = getString(R.string.pedido_codigo_fecha, p.codigo, p.creado.comoFechaCorta())
        when (p.estado) {
            EstadoPedido.PAGADO_RETENIDO -> c.chipEstado.ponerEstado(getString(R.string.estado_pago_retenido), EstiloEstado.AZUL)
            EstadoPedido.ENTREGADO -> c.chipEstado.ponerEstado(getString(R.string.venta_esperando), EstiloEstado.NARANJA)
            EstadoPedido.LIBERADO -> c.chipEstado.ponerEstado(getString(R.string.venta_completada), EstiloEstado.VERDE)
            else -> c.chipEstado.ponerEstado(getString(R.string.estado_cancelado), EstiloEstado.GRIS)
        }
        producto?.let {
            c.foto.mostrarFoto(it.fotos, it.categoria.icono)
            c.titulo.text = it.titulo
            c.detalle.text = it.saludBateria?.let { bat -> getString(R.string.detalle_grado_bateria, it.grado.letra, bat) } ?: getString(R.string.detalle_grado, it.grado.letra)
        }
        c.entrega.text = getString(R.string.pedido_resumen_entrega, p.puntoEntrega, p.horario)
        c.entrega.iconoInicio(R.drawable.ic_ubicacion, 16, ContextCompat.getColor(requireContext(), R.color.texto_secundario))
        c.precio.text = p.monto.comoPrecio()
        c.comision.text = "−" + p.comision.comoPrecio()
        c.ganancia.text = p.gananciaVendedor.comoPrecio()

        when (p.estado) {
            EstadoPedido.PAGADO_RETENIDO -> {
                c.botonAccion.visibility = View.VISIBLE
                c.botonAccion.setOnClickListener { viewModel.marcarEntregado(p.id) }
                c.nota.visibility = View.VISIBLE
                c.nota.setText(R.string.venta_nota_pago)
            }
            EstadoPedido.ENTREGADO -> {
                c.nota.visibility = View.VISIBLE
                c.nota.setText(R.string.venta_nota_pago)
                if (esDebug()) {
                    c.botonAccion.visibility = View.VISIBLE
                    c.botonAccion.estilo = com.fcc.reco.ui.comun.BotonPildora.Estilo.SECUNDARIO
                    c.botonAccion.setText(R.string.debug_simular_confirmacion)
                    c.botonAccion.ponerIcono(0)
                    c.botonAccion.setOnClickListener { viewModel.simularConfirmacionDelComprador(p.id) }
                }
            }
            else -> Unit
        }
        return c.root
    }
}
