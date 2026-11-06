package com.fcc.reco.ui.comun

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.fcc.reco.R

/** Círculo con iniciales y degradado diagonal (como "MC" en Inicio o "AR" en el vendedor). */
class AvatarIniciales @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        setTextAppearance(R.style.Reco_Texto_Boton)
        setTextColor(Color.WHITE)
        gravity = Gravity.CENTER
        if (!isInEditMode) {
            configurar(
                text?.toString().orEmpty().ifEmpty { "RC" },
                ContextCompat.getColor(context, R.color.naranja),
                ContextCompat.getColor(context, R.color.rosa)
            )
        }
    }

    fun configurar(iniciales: String, colorInicio: Int, colorFin: Int) {
        text = iniciales.take(2).uppercase()
        background = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(colorInicio, colorFin)
        ).apply { shape = GradientDrawable.OVAL }
    }

    /** Toma las iniciales de un nombre completo ("Miguel Cruz" -> "MC"). */
    fun configurarConNombre(nombre: String, colorInicio: Int, colorFin: Int) {
        val iniciales = nombre.trim().split(" ").filter { it.isNotEmpty() }
            .take(2).joinToString("") { it.first().toString() }
        configurar(iniciales.ifEmpty { "?" }, colorInicio, colorFin)
    }
}
