package com.fcc.reco.ui.comprador

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fcc.reco.R
import com.fcc.reco.data.model.Especificacion
import com.fcc.reco.databinding.ItemEspecificacionBinding

class EspecificacionAdapter(private val items: List<Especificacion>) :
    RecyclerView.Adapter<EspecificacionAdapter.Holder>() {

    class Holder(val b: ItemEspecificacionBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(padre: ViewGroup, tipo: Int) =
        Holder(ItemEspecificacionBinding.inflate(LayoutInflater.from(padre.context), padre, false))

    override fun onBindViewHolder(h: Holder, posicion: Int) {
        val e = items[posicion]
        h.b.etiqueta.text = e.etiqueta
        h.b.valor.text = e.valor
        h.b.icono.setImageResource(
            when (e.etiqueta) {
                "Procesador" -> R.drawable.ic_procesador
                "Memoria" -> R.drawable.ic_tarjeta
                "Almacenamiento" -> R.drawable.ic_almacenamiento
                "Pantalla" -> R.drawable.ic_pantalla
                else -> R.drawable.ic_info
            }
        )
    }

    override fun getItemCount() = items.size
}
