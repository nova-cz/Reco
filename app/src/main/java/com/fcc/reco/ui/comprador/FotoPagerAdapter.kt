package com.fcc.reco.ui.comprador

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.fcc.reco.databinding.ItemFotoDetalleBinding

/** Galería simulada: repite el ícono de la categoría sobre gris, como en el Figma. */
class FotoPagerAdapter(@DrawableRes private val icono: Int, private val cantidad: Int) :
    RecyclerView.Adapter<FotoPagerAdapter.Holder>() {

    class Holder(val b: ItemFotoDetalleBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(padre: ViewGroup, tipo: Int) =
        Holder(ItemFotoDetalleBinding.inflate(LayoutInflater.from(padre.context), padre, false))

    override fun onBindViewHolder(h: Holder, posicion: Int) = h.b.icono.setImageResource(icono)

    override fun getItemCount() = cantidad
}
