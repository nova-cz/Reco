package com.fcc.reco.ui.comun

import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import coil3.load
import coil3.request.error
import coil3.request.placeholder
import java.io.File

/**
 * Muestra la primera foto: una URL de Supabase Storage (se carga con Coil) o un archivo interno del anuncio.
 * Si no hay foto, el ícono de su categoría (los productos simulados usan ese ícono sobre gris, como en el Figma).
 */
fun ImageView.mostrarFoto(fotos: List<String>, @DrawableRes icono: Int, tamanoIconoDp: Int? = null) {
    val primera = fotos.firstOrNull()
    when {
        primera != null && primera.startsWith("http") -> {
            scaleType = ImageView.ScaleType.CENTER_CROP
            alpha = 1f
            layoutParams = layoutParams.apply { width = -1; height = -1 }
            load(primera) {
                placeholder(icono)
                error(icono)
            }
        }
        primera != null && File(primera).exists() -> {
            scaleType = ImageView.ScaleType.CENTER_CROP
            alpha = 1f
            layoutParams = layoutParams.apply { width = -1; height = -1 }
            setImageURI(Uri.fromFile(File(primera)))
        }
        else -> {
            scaleType = ImageView.ScaleType.FIT_CENTER
            setImageResource(icono)
        }
    }
}
