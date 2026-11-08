package com.fcc.reco.ui.comun

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.fcc.reco.R

/** Barra del paso de precio: el tramo verde es el rango permitido y el círculo marca el precio del usuario. */
class BarraRangoPrecio @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val pincel = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private var minimo = 0f
    private var maximo = 1f
    private var valor = 0f
    private var dominioMax = 1f

    /** El dominio de la barra va de 0 a 130 % de la referencia, para que se vea cuando el precio se pasa. */
    fun configurar(rangoMin: Int, rangoMax: Int, precio: Int, referencia: Int) {
        minimo = rangoMin.toFloat(); maximo = rangoMax.toFloat(); valor = precio.toFloat()
        dominioMax = referencia * 1.3f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val alto = dp(8).toFloat()
        val centro = height / 2f
        val radio = dp(9).toFloat()
        val ancho = width - 2 * radio
        fun x(v: Float) = radio + ancho * (v / dominioMax).coerceIn(0f, 1f)

        pincel.style = Paint.Style.FILL
        pincel.color = ContextCompat.getColor(context, R.color.estado_gris_fondo)
        rect.set(radio, centro - alto / 2, radio + ancho, centro + alto / 2)
        canvas.drawRoundRect(rect, alto / 2, alto / 2, pincel)

        pincel.color = ContextCompat.getColor(context, R.color.verde_reco)
        pincel.alpha = 150
        rect.set(x(minimo), centro - alto / 2, x(maximo), centro + alto / 2)
        canvas.drawRoundRect(rect, alto / 2, alto / 2, pincel)
        pincel.alpha = 255

        if (valor > 0f) {
            val cx = x(valor)
            pincel.color = Color.WHITE
            canvas.drawCircle(cx, centro, radio, pincel)
            pincel.style = Paint.Style.STROKE
            pincel.strokeWidth = dp(3).toFloat()
            pincel.color = ContextCompat.getColor(context, R.color.tinta)
            canvas.drawCircle(cx, centro, radio - dp(1.5f), pincel)
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density + 0.5f).toInt()
    private fun dp(v: Float) = v * resources.displayMetrics.density
}
