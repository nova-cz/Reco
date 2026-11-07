package com.fcc.reco.ui.comprador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.fcc.reco.data.DatosSimulados
import com.fcc.reco.databinding.FragmentEntregaPagoBinding
import com.fcc.reco.databinding.ItemPedidoEntregaBinding
import com.fcc.reco.databinding.ItemPuntoEntregaBinding
import com.fcc.reco.ui.comun.ChipFiltro
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.comun.comoPrecio
import com.fcc.reco.ui.comun.configurarVendedor
import com.fcc.reco.ui.comun.dp
import com.fcc.reco.ui.comun.irA
import com.fcc.reco.ui.comun.sumarMargenInferiorDeBarras
import kotlinx.coroutines.launch

/** Elegir punto y horario de entrega por vendedor, la tarjeta de prueba y pagar. */
class EntregaPagoFragment : Fragment(R.layout.fragment_entrega_pago) {

    private val viewModel: EntregaPagoViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentEntregaPagoBinding.bind(view)
        b.scroll.ajustarPorBarrasDelSistema(arriba = true, abajo = false)
        b.barraTotal.sumarMargenInferiorDeBarras(16)
        b.barraTotal.vincularFondo(b.scroll)
        val nav = findNavController()

        b.botonAtras.setOnClickListener { nav.popBackStack() }
        b.botonPagar.setOnClickListener { viewModel.pagar() }
        b.cambiarTarjeta.setOnClickListener {
            if (childFragmentManager.findFragmentByTag(TarjetaBottomSheet.TAG) == null) {
                TarjetaBottomSheet().show(childFragmentManager, TarjetaBottomSheet.TAG)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.estado.collect { pintar(b, it) } }
                launch {
                    viewModel.eventos.collect { evento ->
                        if (evento is EventoPago.PagoExitoso) {
                            nav.irA(
                                R.id.compraConfirmada,
                                Bundle().apply { putIntArray("pedidoIds", evento.pedidoIds) },
                                popHasta = R.id.inicio
                            )
                        }
                    }
                }
            }
        }
    }

    private fun pintar(b: FragmentEntregaPagoBinding, estado: EstadoPago) {
        b.barraTotalValor.text = estado.total.comoPrecio()
        b.tarjetaTexto.text = getString(R.string.tarjeta_terminacion, estado.tarjetaUltimos4)
        b.botonPagar.isEnabled = !estado.cargando && estado.grupos.isNotEmpty()
        b.botonPagar.setText(if (estado.cargando) R.string.pagando else R.string.pagar)
        estado.error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show() }

        val inflater = LayoutInflater.from(requireContext())
        b.contenedorPedidos.removeAllViews()
        estado.grupos.forEachIndexed { indice, grupo ->
            val nombre = grupo.vendedor.nombre
            val seleccion = estado.selecciones[nombre] ?: Seleccion()
            val p = ItemPedidoEntregaBinding.inflate(inflater, b.contenedorPedidos, false)
            p.avatar.configurarVendedor(grupo.vendedor)
            p.titulo.text = getString(R.string.pedido_vendedor, indice + 1, nombre)
            p.monto.text = grupo.subtotal.comoPrecio()

            DatosSimulados.puntosEntrega.forEachIndexed { i, punto ->
                val fila = ItemPuntoEntregaBinding.inflate(inflater, p.puntos, false)
                fila.nombre.text = punto.nombre
                fila.descripcion.text = punto.descripcion
                pintarPunto(fila, i == seleccion.punto)
                fila.root.setOnClickListener { viewModel.elegirPunto(nombre, i) }
                p.puntos.addView(fila.root)
            }
            DatosSimulados.horariosEntrega.forEachIndexed { i, horario ->
                p.horarios.addView(ChipFiltro(requireContext()).apply {
                    text = horario
                    cerrarAlActivar = false
                    ponerIcono(R.drawable.ic_reloj)
                    activo = i == seleccion.horario
                    setOnClickListener { viewModel.elegirHorario(nombre, i) }
                })
            }
            b.contenedorPedidos.addView(p.root, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { if (indice > 0) topMargin = b.root.dp(12) })
        }
    }

    private fun pintarPunto(f: ItemPuntoEntregaBinding, elegido: Boolean) {
        val ctx = requireContext()
        f.root.setBackgroundResource(if (elegido) R.drawable.bg_punto_seleccionado else R.drawable.bg_punto_normal)
        f.cajaIcono.setBackgroundResource(if (elegido) R.drawable.bg_icono_azul_chico else R.drawable.bg_icono_blanco)
        androidx.core.widget.ImageViewCompat.setImageTintList(
            f.icono,
            android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(ctx, if (elegido) R.color.white else R.color.texto_secundario)
            )
        )
        f.radio.setBackgroundResource(if (elegido) R.drawable.bg_circulo_azul else R.drawable.bg_radio_vacio)
        f.radioCheck.visibility = if (elegido) View.VISIBLE else View.GONE
    }
}
