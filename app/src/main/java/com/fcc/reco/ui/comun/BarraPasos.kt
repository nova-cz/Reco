package com.fcc.reco.ui.comun

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.fcc.reco.R

/** Barra de progreso por pasos ("Paso 2 de 3"): segmentos negros (hechos) y grises (pendientes). */
class BarraPasos @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var total: Int = 3
        private set
    var actual: Int = 1
        private set

    init {
        orientation = HORIZONTAL
        val a = context.obtainStyledAttributes(attrs, R.styleable.BarraPasos, defStyleAttr, 0)
        val t = a.getInt(R.styleable.BarraPasos_pasosTotal, 3)
        val p = a.getInt(R.styleable.BarraPasos_pasosActual, 1)
        a.recycle()
        configurar(t, p)
    }

    /** [actual] es el paso en curso, empezando en 1. */
    fun configurar(total: Int, actual: Int) {
        this.total = total
        this.actual = actual
        removeAllViews()
        repeat(total) { indice ->
            val segmento = View(context)
            segmento.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpF(4f)
                setColor(
                    ContextCompat.getColor(
                        context,
                        if (indice < actual) R.color.tinta else R.color.borde_suave
                    )
                )
            }
            addView(segmento, LayoutParams(0, dp(4), 1f).apply {
                if (indice > 0) marginStart = dp(6)
            })
        }
    }
}
