package com.fcc.reco.ui.comun

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.ImageViewCompat
import com.fcc.reco.R
import com.fcc.reco.data.model.Categoria

/** Mosaico de categoría: degradado diagonal, círculo con ícono arriba y nombre abajo. */
class MosaicoCategoria @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val circulo = ImageView(context)
    private val nombre = TextView(context)

    init {
        clipToPadding = false
        circulo.scaleType = ImageView.ScaleType.CENTER
        circulo.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(0x38FFFFFF)
        }
        ImageViewCompat.setImageTintList(circulo, ColorStateList.valueOf(Color.WHITE))
        addView(circulo, LayoutParams(dp(36), dp(36), Gravity.TOP or Gravity.START).apply {
            topMargin = dp(12)
            marginStart = dp(12)
        })

        nombre.setTextAppearance(R.style.Reco_Texto_Boton)
        nombre.textSize = 13f
        nombre.setTextColor(Color.WHITE)
        nombre.maxLines = 1
        addView(nombre, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM or Gravity.START).apply {
            bottomMargin = dp(12)
            marginStart = dp(12)
            marginEnd = dp(8)
        })

        isClickable = true
        isFocusable = true
        val mascara = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpF(24f)
            setColor(Color.WHITE)
        }
        foreground = RippleDrawable(ColorStateList.valueOf(0x33FFFFFF), null, mascara)
    }

    fun configurar(categoria: Categoria) {
        setBackgroundResource(categoria.degradado)
        circulo.setImageResource(categoria.icono)
        nombre.setText(categoria.nombre)
        contentDescription = context.getString(categoria.nombre)
    }
}
