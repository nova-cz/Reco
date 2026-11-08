package com.fcc.reco.ui.comun

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.databinding.FragmentPerfilPublicoBinding
import kotlinx.coroutines.launch

/** Perfil público del vendedor (Figma 38:456): pestañas "A la venta" y "Reseñas", sin datos de contacto. */
class PerfilPublicoFragment : Fragment(R.layout.fragment_perfil_publico) {

    private val viewModel: PerfilPublicoViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentPerfilPublicoBinding.bind(view)
        b.raiz.ajustarPorBarrasDelSistema(arriba = true, abajo = true)
        val nav = findNavController()

        b.botonAtras.setOnClickListener { nav.popBackStack() }
        b.reportar.setOnClickListener { Toast.makeText(requireContext(), R.string.aviso_reporte, Toast.LENGTH_LONG).show() }
        b.tabALaVenta.cerrarAlActivar = false
        b.tabResenas.cerrarAlActivar = false
        b.tabALaVenta.setOnClickListener { viewModel.ponerTab(TabPerfilPublico.A_LA_VENTA) }
        b.tabResenas.setOnClickListener { viewModel.ponerTab(TabPerfilPublico.RESENAS) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estado.collect { pintar(b, it) }
            }
        }
    }

    private fun pintar(b: FragmentPerfilPublicoBinding, e: EstadoPerfilPublico) {
        val ctx = requireContext()
        val v = e.vendedor
        if (v == null) {
            b.tarjeta.root.visibility = View.GONE
            b.vacio.visibility = View.VISIBLE
            b.vacio.setText(R.string.perfil_no_encontrado)
            return
        }
        b.tarjeta.root.visibility = View.VISIBLE
        val tarjeta = b.tarjeta
        tarjeta.pintarBase(v.nombre, getString(R.string.carrera_facultad, v.carrera, v.facultad), e.miembroDesde, ContextCompat.getColor(ctx, R.color.naranja), ContextCompat.getColor(ctx, R.color.rosa))
        tarjeta.iconoVerificado.visibility = View.VISIBLE
        tarjeta.insigniaVerificado.configurar(ChipInsignia.Tipo.VERIFICADO_AZUL, getString(R.string.perfil_estudiante_verificado))
        tarjeta.insigniaPuntual.visibility = if (e.puntual) View.VISIBLE else View.GONE
        if (e.puntual) tarjeta.insigniaPuntual.configurar(ChipInsignia.Tipo.VERIFICADO_VERDE, getString(R.string.perfil_entrega_puntual), R.drawable.ic_check)
        tarjeta.pintarCifras(
            (e.promedio?.let { getString(R.string.perfil_calificacion_valor, it) } ?: "—") to getString(R.string.perfil_n_resenas, e.resenas.size),
            e.ventas.toString() to getString(R.string.perfil_ventas),
            e.aLaVenta.size.toString() to getString(R.string.perfil_a_la_venta)
        )

        // Dónde entrega
        b.listaPuntos.removeAllViews()
        e.puntos.forEach { punto ->
            b.listaPuntos.addView(TextView(ctx).apply {
                text = punto
                setTextAppearance(R.style.Reco_Texto_Cuerpo)
                textSize = 14f
                compoundDrawablePadding = dp(8)
                iconoInicio(R.drawable.ic_ubicacion, 18, ContextCompat.getColor(ctx, R.color.azul))
                setPadding(0, dp(10), 0, 0)
            })
        }

        // Pestañas
        b.tabALaVenta.text = getString(R.string.perfil_a_la_venta_n, e.aLaVenta.size)
        b.tabResenas.text = getString(R.string.perfil_resenas_n, e.resenas.size)
        b.tabALaVenta.activo = e.tab == TabPerfilPublico.A_LA_VENTA
        b.tabResenas.activo = e.tab == TabPerfilPublico.RESENAS
        b.contenido.removeAllViews()
        when (e.tab) {
            TabPerfilPublico.A_LA_VENTA -> {
                b.vacio.visibility = if (e.aLaVenta.isEmpty()) View.VISIBLE else View.GONE
                b.vacio.setText(R.string.perfil_sin_a_la_venta)
                val nav = findNavController()
                CuadriculaProductos.mostrar(
                    b.contenido, e.aLaVenta, e.favoritos,
                    alAbrir = { p ->
                        // Desde modo vendedor no hay detalle de producto (es la vista del comprador).
                        if (nav.graph.findNode(R.id.detalleProducto) != null) nav.irA(R.id.detalleProducto, Bundle().apply { putInt("productoId", p.id) })
                    },
                    alAlternarFavorito = { viewModel.alternarFavorito(it) }
                )
            }
            TabPerfilPublico.RESENAS -> {
                b.vacio.visibility = if (e.resenas.isEmpty()) View.VISIBLE else View.GONE
                b.vacio.setText(R.string.perfil_sin_resenas_publico)
                b.contenido.llenarResenas(e.resenas)
            }
        }
    }
}
