package com.fcc.reco.ui.comun

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fcc.reco.R
import com.fcc.reco.data.model.Producto
import com.fcc.reco.databinding.ItemProductoBinding

/** Tarjeta de producto de la cuadrícula (Inicio, Resultados, Favoritos). */
class ProductoAdapter(
    private val alAbrir: (Producto) -> Unit,
    private val alAlternarFavorito: (Producto) -> Unit
) : ListAdapter<Producto, ProductoAdapter.Holder>(Comparador) {

    var favoritos: Set<Int> = emptySet()
        set(valor) {
            field = valor
            notifyItemRangeChanged(0, itemCount, PAYLOAD_FAVORITO)
        }

    class Holder(val b: ItemProductoBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(padre: ViewGroup, tipo: Int) =
        Holder(ItemProductoBinding.inflate(LayoutInflater.from(padre.context), padre, false))

    override fun onBindViewHolder(h: Holder, posicion: Int) {
        val p = getItem(posicion)
        val ctx = h.b.root.context
        h.b.icono.setImageResource(p.categoria.icono)
        h.b.titulo.text = p.titulo
        h.b.precio.text = p.precio.comoPrecio()
        h.b.punto.text = p.puntoEntrega
        if (p.certificado) {
            h.b.chipCertificado.configurar(ChipInsignia.Tipo.CERTIFICADO, ctx.getString(R.string.certificado))
            h.b.chipCertificado.visibility = android.view.View.VISIBLE
        } else {
            h.b.chipCertificado.visibility = android.view.View.GONE
        }
        h.b.chipGrado.configurar(ChipInsignia.Tipo.GRADO, ctx.getString(R.string.grado_corto, p.grado.letra))
        val bateria = p.saludBateria
        if (bateria != null) {
            h.b.chipBateria.configurar(ChipInsignia.Tipo.NEUTRO_GRIS, "$bateria%", R.drawable.ic_bateria)
            h.b.chipBateria.visibility = android.view.View.VISIBLE
        } else {
            h.b.chipBateria.visibility = android.view.View.GONE
        }
        pintarFavorito(h, p)
        h.b.root.setOnClickListener { alAbrir(p) }
        h.b.botonFavorito.setOnClickListener { alAlternarFavorito(p) }
    }

    override fun onBindViewHolder(h: Holder, posicion: Int, cargas: MutableList<Any>) {
        if (cargas.contains(PAYLOAD_FAVORITO)) pintarFavorito(h, getItem(posicion))
        else onBindViewHolder(h, posicion)
    }

    private fun pintarFavorito(h: Holder, p: Producto) {
        val esFavorito = p.id in favoritos
        h.b.iconoFavorito.setImageResource(if (esFavorito) R.drawable.ic_favorito_lleno else R.drawable.ic_favorito)
    }

    private object Comparador : DiffUtil.ItemCallback<Producto>() {
        override fun areItemsTheSame(a: Producto, b: Producto) = a.id == b.id
        override fun areContentsTheSame(a: Producto, b: Producto) = a == b
    }

    private companion object {
        const val PAYLOAD_FAVORITO = "favorito"
    }
}
