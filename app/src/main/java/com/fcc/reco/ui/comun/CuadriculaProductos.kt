package com.fcc.reco.ui.comun

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Space
import com.fcc.reco.R
import com.fcc.reco.data.model.Especificacion
import com.fcc.reco.data.model.Producto
import com.fcc.reco.databinding.ItemEspecificacionBinding
import com.fcc.reco.databinding.ItemProductoBinding

/**
 * Arma cuadrículas de 2 columnas dentro de un LinearLayout vertical.
 * Se usa en vez de un RecyclerView anidado en un ScrollView (que solo mostraba la primera fila);
 * con pocas decenas de elementos es suficiente.
 */
object CuadriculaProductos {

    fun mostrar(
        contenedor: LinearLayout,
        productos: List<Producto>,
        favoritos: Set<Int>,
        vendidos: Set<Int> = emptySet(),
        alAbrir: (Producto) -> Unit,
        alAlternarFavorito: (Producto) -> Unit
    ) {
        contenedor.removeAllViews()
        val inflater = LayoutInflater.from(contenedor.context)
        armarFilas(contenedor, productos) { fila, producto ->
            val b = ItemProductoBinding.inflate(inflater, fila, false)
            enlazar(b, producto, producto.id in favoritos, producto.id in vendidos)
            b.root.setOnClickListener { alAbrir(producto) }
            b.botonFavorito.setOnClickListener { alAlternarFavorito(producto) }
            b.root
        }
    }

    fun mostrarEspecificaciones(contenedor: LinearLayout, especificaciones: List<Especificacion>) {
        contenedor.removeAllViews()
        val inflater = LayoutInflater.from(contenedor.context)
        armarFilas(contenedor, especificaciones, espacioDp = 10) { fila, e ->
            val b = ItemEspecificacionBinding.inflate(inflater, fila, false)
            b.etiqueta.text = e.etiqueta
            b.valor.text = e.valor
            b.icono.setImageResource(
                when (e.etiqueta) {
                    "Procesador" -> R.drawable.ic_procesador
                    "Memoria" -> R.drawable.ic_tarjeta
                    "Almacenamiento" -> R.drawable.ic_almacenamiento
                    "Pantalla" -> R.drawable.ic_pantalla
                    else -> R.drawable.ic_info
                }
            )
            b.root
        }
    }

    private fun <T> armarFilas(
        contenedor: LinearLayout,
        elementos: List<T>,
        espacioDp: Int = 12,
        crearVista: (ViewGroup, T) -> View
    ) {
        val ctx = contenedor.context
        val espacio = ctx.dp(espacioDp)
        elementos.chunked(2).forEachIndexed { indiceFila, grupo ->
            val fila = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL }
            grupo.forEachIndexed { columna, elemento ->
                fila.addView(crearVista(fila, elemento), LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1f
                ).apply { if (columna == 1) marginStart = espacio })
            }
            if (grupo.size == 1) {
                fila.addView(Space(ctx), LinearLayout.LayoutParams(0, 1, 1f).apply { marginStart = espacio })
            }
            contenedor.addView(fila, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { if (indiceFila > 0) topMargin = espacio })
        }
    }

    private fun enlazar(b: ItemProductoBinding, p: Producto, esFavorito: Boolean, vendido: Boolean = false) {
        val ctx = b.root.context
        b.icono.setImageResource(p.categoria.icono)
        b.titulo.text = p.titulo
        b.precio.text = p.precio.comoPrecio()
        b.punto.text = p.puntoEntrega
        // Un equipo vendido se muestra atenuado y con la insignia "Vendido" (Favoritos).
        b.root.alpha = if (vendido) 0.6f else 1f
        if (vendido) {
            b.chipCertificado.configurar(ChipInsignia.Tipo.GRADO, ctx.getString(R.string.vendido))
            b.chipCertificado.visibility = View.VISIBLE
        } else if (p.certificado) {
            b.chipCertificado.configurar(ChipInsignia.Tipo.CERTIFICADO, ctx.getString(R.string.certificado))
            b.chipCertificado.visibility = View.VISIBLE
        } else {
            b.chipCertificado.visibility = View.GONE
        }
        b.chipGrado.configurar(ChipInsignia.Tipo.GRADO, ctx.getString(R.string.grado_corto, p.grado.letra))
        val bateria = p.saludBateria
        if (bateria != null) {
            b.chipBateria.configurar(ChipInsignia.Tipo.NEUTRO_GRIS, "$bateria%", R.drawable.ic_bateria)
            b.chipBateria.visibility = View.VISIBLE
        } else {
            b.chipBateria.visibility = View.GONE
        }
        b.iconoFavorito.setImageResource(if (esFavorito) R.drawable.ic_favorito_lleno else R.drawable.ic_favorito)
    }
}
