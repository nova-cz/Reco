package com.fcc.reco.ui.acceso

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fcc.reco.databinding.ItemCarreraBinding

class CarreraAdapter(
    private val carreras: List<String>,
    private val alElegir: (String) -> Unit
) : RecyclerView.Adapter<CarreraAdapter.Holder>() {

    class Holder(val binding: ItemCarreraBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemCarreraBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val carrera = carreras[position]
        holder.binding.nombre.text = carrera
        holder.binding.root.setOnClickListener { alElegir(carrera) }
    }

    override fun getItemCount() = carreras.size
}
