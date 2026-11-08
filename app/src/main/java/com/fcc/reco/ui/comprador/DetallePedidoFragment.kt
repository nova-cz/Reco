package com.fcc.reco.ui.comprador

import android.content.pm.ApplicationInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.data.model.EstadoPedido
import com.fcc.reco.databinding.FragmentDetallePedidoBinding
import com.fcc.reco.databinding.ItemPasoPedidoBinding
import com.fcc.reco.databinding.ItemProductoPedidoBinding
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.comun.comoFechaHora
import com.fcc.reco.ui.comun.comoPrecio
import com.fcc.reco.ui.comun.configurarVendedor
import com.fcc.reco.ui.comun.dp
import com.fcc.reco.ui.comun.dpF
import com.fcc.reco.ui.comun.estiloComprador
import com.fcc.reco.ui.comun.iconoInicio
import com.fcc.reco.ui.comun.ponerEstado
import com.fcc.reco.ui.comun.sumarMargenInferiorDeBarras
import com.fcc.reco.ui.comun.textoComprador
import kotlinx.coroutines.launch

/** Detalle de pedido: línea de tiempo, equipos, vendedor y entrega (Figma 33:713). */
class DetallePedidoFragment : Fragment(R.layout.fragment_detalle_pedido) {

    private val viewModel: DetallePedidoViewModel by viewModels()

    private enum class Paso { HECHO, ACTUAL, PENDIENTE }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentDetallePedidoBinding.bind(view)
        b.scroll.ajustarPorBarrasDelSistema(arriba = true, abajo = false)
        b.barra.sumarMargenInferiorDeBarras(16)
        b.barra.vincularFondo(b.scroll)
        val nav = findNavController()

        b.botonAtras.setOnClickListener { nav.popBackStack() }
        b.reportar.setOnClickListener {
            Toast.makeText(requireContext(), R.string.aviso_reporte, Toast.LENGTH_LONG).show()
        }
        val esDebug = (requireContext().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        b.botonDebug.setOnClickListener { viewModel.simularEntregaDelVendedor() }

        // Si viene de "Confirmar entrega" o "Calificar vendedor", abre la hoja una sola vez.
        if (savedInstanceState == null && arguments?.getBoolean("calificar") == true) {
            view.post { abrirCalificacion() }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estado.collect { estado ->
                    if (estado == null) {
                        nav.popBackStack()
                    } else {
                        pintar(b, estado, esDebug)
                    }
                }
            }
        }
    }

    private fun pintar(b: FragmentDetallePedidoBinding, e: EstadoDetallePedido, esDebug: Boolean) {
        val p = e.pedido
        val inflater = LayoutInflater.from(requireContext())

        b.titulo.text = getString(R.string.pedido_titulo, p.codigo)
        b.chipEstado.ponerEstado(getString(p.estado.textoComprador), p.estado.estiloComprador)

        // Línea de tiempo
        val liberado = p.estado == EstadoPedido.LIBERADO
        b.pasos.removeAllViews()
        listOf(
            Triple(getString(R.string.paso_pago_realizado), p.creado.comoFechaHora(), Paso.HECHO),
            Triple(
                getString(R.string.paso_pago_retenido),
                getString(if (liberado) R.string.paso_pago_retenido_liberado else R.string.paso_pago_retenido_desc),
                Paso.HECHO
            ),
            Triple(getString(R.string.paso_entrega), p.horario, if (liberado) Paso.HECHO else Paso.ACTUAL),
            Triple(
                getString(R.string.paso_pago_liberado),
                getString(if (liberado) R.string.paso_completado else R.string.paso_pendiente),
                if (liberado) Paso.HECHO else Paso.PENDIENTE
            )
        ).forEach { (titulo, detalle, paso) ->
            val item = ItemPasoPedidoBinding.inflate(inflater, b.pasos, false)
            item.pasoTitulo.text = titulo
            item.pasoDetalle.text = detalle
            pintarIcono(item, paso)
            if (paso == Paso.PENDIENTE) {
                item.pasoTitulo.setTextColor(ContextCompat.getColor(requireContext(), R.color.texto_secundario))
                item.pasoTitulo.alpha = 0.7f
            }
            b.pasos.addView(item.root)
        }

        // Equipos
        b.equipos.removeAllViews()
        e.productos.forEach { prod ->
            val fila = ItemProductoPedidoBinding.inflate(inflater, b.equipos, false)
            fila.icono.setImageResource(prod.categoria.icono)
            fila.titulo.text = prod.titulo
            fila.detalle.text = prod.saludBateria?.let { getString(R.string.detalle_grado_bateria, prod.grado.letra, it) }
                ?: getString(R.string.detalle_grado, prod.grado.letra)
            fila.precio.text = prod.precio.comoPrecio()
            b.equipos.addView(fila.root)
        }

        // Vendedor
        b.avatar.configurarVendedor(p.vendedor)
        b.vendedor.text = p.vendedor.nombre
        b.vendedorDatos.text = getString(
            R.string.vendedor_resumen, p.vendedor.reputacion, p.vendedor.ventas, p.vendedor.carrera
        )

        // Entrega
        b.punto.text = p.puntoEntrega
        b.punto.iconoInicio(R.drawable.ic_ubicacion, 20, ContextCompat.getColor(requireContext(), R.color.azul))
        b.horario.text = p.horario
        b.codigo.text = p.codigoEntrega

        pintarBarra(b, e)
        b.botonDebug.visibility = if (esDebug && p.estado == EstadoPedido.PAGADO_RETENIDO) View.VISIBLE else View.GONE
    }

    private fun pintarBarra(b: FragmentDetallePedidoBinding, e: EstadoDetallePedido) {
        val p = e.pedido
        val boton = b.botonPrincipal
        b.barraMonto.text = p.monto.comoPrecio()
        b.barraEtiqueta.setText(if (p.estado == EstadoPedido.LIBERADO) R.string.pago_liberado else R.string.estado_pago_retenido)
        boton.visibility = View.VISIBLE
        boton.isEnabled = true
        when (p.estado) {
            EstadoPedido.PAGADO_RETENIDO -> {
                boton.setText(R.string.esperando_entrega)
                boton.ponerIcono(R.drawable.ic_reloj)
                boton.isEnabled = false
            }
            EstadoPedido.ENTREGADO -> {
                boton.setText(R.string.confirmar_entrega)
                boton.ponerIcono(R.drawable.ic_check)
                boton.setOnClickListener {
                    if (viewModel.confirmarEntrega()) abrirCalificacion()
                }
            }
            EstadoPedido.LIBERADO -> {
                val estrellas = p.estrellas
                if (estrellas == null) {
                    boton.setText(R.string.calificar_vendedor)
                    boton.ponerIcono(R.drawable.ic_estrella_llena)
                    boton.setOnClickListener { abrirCalificacion() }
                } else {
                    boton.text = getString(R.string.calificado_con, estrellas)
                    boton.ponerIcono(0)
                    boton.isEnabled = false
                }
            }
            else -> boton.visibility = View.GONE
        }
    }

    private fun pintarIcono(item: ItemPasoPedidoBinding, paso: Paso) {
        val ctx = requireContext()
        val icono = item.icono
        fun circulo(color: Int) = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(color) }
        when (paso) {
            Paso.HECHO -> {
                icono.background = circulo(ContextCompat.getColor(ctx, R.color.estado_verde))
                icono.setImageResource(R.drawable.ic_check)
                ImageViewCompat.setImageTintList(icono, ColorStateList.valueOf(Color.WHITE))
            }
            Paso.ACTUAL -> {
                icono.background = LayerDrawable(
                    arrayOf(circulo(ContextCompat.getColor(ctx, R.color.estado_naranja)), circulo(Color.WHITE))
                ).apply { setLayerInset(1, icono.dp(8), icono.dp(8), icono.dp(8), icono.dp(8)) }
                icono.setImageDrawable(null)
            }
            Paso.PENDIENTE -> {
                icono.background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.TRANSPARENT)
                    setStroke(icono.dp(2), ContextCompat.getColor(ctx, R.color.borde_suave))
                }
                icono.setImageDrawable(null)
            }
        }
    }

    private fun abrirCalificacion() {
        val estado = viewModel.estado.value ?: return
        if (estado.pedido.estado != EstadoPedido.LIBERADO || estado.pedido.estrellas != null) return
        if (childFragmentManager.findFragmentByTag(CalificarBottomSheet.TAG) == null) {
            CalificarBottomSheet().show(childFragmentManager, CalificarBottomSheet.TAG)
        }
    }
}
