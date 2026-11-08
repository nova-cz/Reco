package com.fcc.reco.ui.comun

import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import java.io.File

/**
 * Muestra la primera foto real si el anuncio la tiene; si no, el ícono de su categoría
 * (los productos simulados usan ese ícono sobre gris, como en el Figma).
 */
fun ImageView.mostrarFoto(fotos: List<String>, @DrawableRes icono: Int, tamanoIconoDp: Int? = null) {
    val primera = fotos.firstOrNull()?.takeIf { File(it).exists() }
    if (primera != null) {
        scaleType = ImageView.ScaleType.CENTER_CROP
        alpha = 1f
        layoutParams = layoutParams.apply { width = -1; height = -1 }
        setImageURI(Uri.fromFile(File(primera)))
    } else {
        scaleType = ImageView.ScaleType.FIT_CENTER
        setImageResource(icono)
    }
}
