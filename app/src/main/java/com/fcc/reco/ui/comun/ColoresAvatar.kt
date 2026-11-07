package com.fcc.reco.ui.comun

import androidx.core.content.ContextCompat
import com.fcc.reco.R
import com.fcc.reco.data.model.VendedorResumen

private val paresDeColor = listOf(
    R.color.naranja to R.color.rosa,
    R.color.azul to R.color.verde_reco,
    R.color.rosa to R.color.azul,
    R.color.caramelo to R.color.naranja,
    R.color.azul to R.color.rosa
)

/** Avatar con las iniciales del vendedor y un degradado que depende de su nombre (siempre el mismo). */
fun AvatarIniciales.configurarVendedor(vendedor: VendedorResumen) {
    val (inicio, fin) = paresDeColor[Math.floorMod(vendedor.nombre.hashCode(), paresDeColor.size)]
    configurar(vendedor.iniciales, ContextCompat.getColor(context, inicio), ContextCompat.getColor(context, fin))
}
