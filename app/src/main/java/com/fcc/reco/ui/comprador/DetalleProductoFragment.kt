package com.fcc.reco.ui.comprador

import android.content.Intent
import android.os.Bundle
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
import androidx.viewpager2.widget.ViewPager2
import com.fcc.reco.R
import com.fcc.reco.data.model.Producto
import com.fcc.reco.databinding.FragmentDetalleProductoBinding
import com.fcc.reco.ui.comun.ChipFiltro
import com.fcc.reco.ui.comun.ChipInsignia
import com.fcc.reco.ui.comun.CuadriculaProductos
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.comun.comoPrecio
import com.fcc.reco.ui.comun.dp
import com.fcc.reco.ui.comun.irA
import com.fcc.reco.ui.comun.sumarMargenInferiorDeBarras
import kotlinx.coroutines.launch

class DetalleProductoFragment : Fragment(R.layout.fragment_detalle_producto) {

    private val viewModel: DetalleViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentDetalleProductoBinding.bind(view)
        val producto = viewModel.producto
        if (producto == null) {
            findNavController().popBackStack()
            return
        }
        b.scroll.ajustarPorBarrasDelSistema(arriba = true, abajo = false)
        b.barraPrecio.sumarMargenInferiorDeBarras(16)
        b.barraPrecio.vincularFondo(b.scroll)

        b.botonAtras.setOnClickListener { findNavController().popBackStack() }
        b.botonCompartir.setOnClickListener { compartir(producto) }
        b.botonFavorito.setOnClickListener { viewModel.alternarFavorito() }
        b.tarjetaVendedor.setOnClickListener {
            findNavController().irA(R.id.perfilPublico, Bundle().apply { putString("vendedor", producto.vendedor.nombre) })
        }
        // Compra directa de un solo artículo (sin carrito). Nadie puede comprar sus propios anuncios.
        b.botonComprar.isEnabled = !producto.esPropio
        b.botonComprar.setText(if (producto.esPropio) R.string.anuncio_propio else R.string.comprar)
        b.botonComprar.setOnClickListener {
            findNavController().irA(R.id.comprar, Bundle().apply { putInt("productoId", producto.id) })
        }
        pintarProducto(b, producto)
        armarPreguntasRapidas(b, producto)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.favoritos.collect { ids ->
                        b.iconoFavorito.setImageResource(
                            if (producto.id in ids) R.drawable.ic_favorito_lleno else R.drawable.ic_favorito
                        )
                    }
                }
            }
        }
    }

    private fun pintarProducto(b: FragmentDetalleProductoBinding, p: Producto) {
        val ctx = requireContext()
        b.categoriaMarca.text = getString(R.string.categoria_marca, getString(p.categoria.nombre), p.marca)
        b.titulo.text = p.tituloDetalle
        b.precio.text = p.precio.comoPrecio()
        b.puntoEntrega.text = p.puntoEntrega

        if (p.certificado) {
            b.chipCertificado.configurar(ChipInsignia.Tipo.CERTIFICADO, getString(R.string.certificado))
            b.chipCertificado.visibility = View.VISIBLE
        }

        // Chips: grado, batería y color
        b.chips.removeAllViews()
        b.chips.addView(ChipInsignia(ctx).apply {
            configurar(ChipInsignia.Tipo.GRADO, getString(R.string.grado_etiqueta, p.grado.letra, p.grado.etiqueta))
        })
        p.saludBateria?.let {
            b.chips.addView(ChipInsignia(ctx).apply {
                configurar(ChipInsignia.Tipo.NEUTRO, getString(R.string.bateria_valor, it), R.drawable.ic_bateria)
            })
        }
        b.chips.addView(ChipInsignia(ctx).apply { configurar(ChipInsignia.Tipo.NEUTRO, p.color) })

        // Vendedor
        val v = p.vendedor
        b.avatarVendedor.configurar(
            v.iniciales,
            ContextCompat.getColor(ctx, R.color.naranja),
            ContextCompat.getColor(ctx, R.color.rosa)
        )
        b.vendedorNombre.text = v.nombre
        b.vendedorCarrera.text = getString(R.string.carrera_facultad, v.carrera, v.facultad)
        b.vendedorReputacion.text = getString(R.string.reputacion_ventas, v.reputacion.toString(), v.ventas)

        // Especificaciones
        CuadriculaProductos.mostrarEspecificaciones(b.listaSpecs, p.especificaciones)

        // Galería simulada con indicador de puntos
        val fotos = if (p.fotos.isNotEmpty()) p.fotos.size else 4
        b.pager.adapter = FotoPagerAdapter(p.categoria.icono, fotos, p.fotos)
        crearPuntos(b, fotos)
        marcarPunto(b, 0)
        b.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(posicion: Int) = marcarPunto(b, posicion)
        })
    }

    /** Chips con la respuesta del vendedor (sin chat): la primera pregunta arranca elegida. */
    private fun armarPreguntasRapidas(b: FragmentDetalleProductoBinding, p: Producto) {
        val ctx = requireContext()
        val vendedor = p.vendedor.nombre.substringBefore(' ')
        val preguntas = listOf(
            // Con datos reales (Supabase) se usan las respuestas que escribió el vendedor; si faltan, la plantilla.
            R.string.pregunta_donde to (p.vendedor.dondeEntregas?.let { getString(R.string.respuesta_donde_real, vendedor, it) }
                ?: getString(R.string.respuesta_donde, vendedor, p.puntoEntrega)),
            R.string.pregunta_horario to (p.vendedor.horario?.let { getString(R.string.respuesta_horario_real, vendedor, it) }
                ?: getString(R.string.respuesta_horario, vendedor)),
            R.string.pregunta_tarjeta to when (p.vendedor.aceptaTarjeta) {
                true -> getString(R.string.respuesta_tarjeta, vendedor)
                false -> getString(R.string.respuesta_tarjeta_no, vendedor)
                null -> getString(R.string.respuesta_tarjeta, vendedor)
            }
        )
        val chips = preguntas.map { (pregunta, _) ->
            ChipFiltro(ctx).apply {
                text = getString(pregunta)
                cerrarAlActivar = false
            }
        }
        fun elegir(indice: Int) {
            chips.forEachIndexed { i, chip -> chip.activo = i == indice }
            b.respuestaRapida.text = preguntas[indice].second
        }
        b.preguntasRapidas.removeAllViews()
        chips.forEachIndexed { i, chip ->
            chip.setOnClickListener { elegir(i) }
            b.preguntasRapidas.addView(chip)
        }
        elegir(0)
    }

    private fun crearPuntos(b: FragmentDetalleProductoBinding, cantidad: Int) {
        b.puntos.removeAllViews()
        repeat(cantidad) {
            b.puntos.addView(View(requireContext()), LinearLayout.LayoutParams(b.root.dp(6), b.root.dp(6)).apply {
                marginStart = b.root.dp(3)
                marginEnd = b.root.dp(3)
            })
        }
    }

    private fun marcarPunto(b: FragmentDetalleProductoBinding, activo: Int) {
        for (i in 0 until b.puntos.childCount) {
            val punto = b.puntos.getChildAt(i)
            val esActivo = i == activo
            punto.layoutParams = (punto.layoutParams as LinearLayout.LayoutParams).apply {
                width = b.root.dp(if (esActivo) 18 else 6)
            }
            punto.background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = b.root.dp(3).toFloat()
                setColor(if (esActivo) ContextCompat.getColor(requireContext(), R.color.tinta) else 0x4D111114)
            }
        }
    }

    private fun compartir(p: Producto) {
        val texto = "${p.titulo} · ${p.precio.comoPrecio()} en Reco"
        startActivity(Intent.createChooser(
            Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, texto), null
        ))
    }
}
