package com.fcc.reco.ui.comun

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.fcc.reco.R

/**
 * Chip de filtro. Activo: negro con "x". Inactivo: blanco con borde suave
 * (con ícono opcional y color de acento opcional, p. ej. "Certificado" en verde).
 */
class ChipFiltro @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    var activo: Boolean = false
        set(valor) {
            field = valor
            aplicar()
        }

    /** Si es falso, al activarse conserva su propio ícono en vez de mostrar la "x". */
    var cerrarAlActivar = true
        set(valor) {
            field = valor
            aplicar()
        }

    private var iconoRes = 0
    private var colorInactivo = 0

    init {
        setTextAppearance(R.style.Reco_Texto_CuerpoMedio)
        textSize = 14f
        gravity = Gravity.CENTER
        minHeight = dp(36)
        setPadding(dp(14), 0, dp(14), 0)
        compoundDrawablePadding = dp(6)
        isClickable = true
        isFocusable = true

        colorInactivo = ContextCompat.getColor(context, R.color.tinta)
        val a = context.obtainStyledAttributes(attrs, R.styleable.ChipFiltro, defStyleAttr, 0)
        a.getString(R.styleable.ChipFiltro_filtroTexto)?.let { text = it }
        iconoRes = a.getResourceId(R.styleable.ChipFiltro_filtroIcono, 0)
        colorInactivo = a.getColor(R.styleable.ChipFiltro_filtroColor, colorInactivo)
        activo = a.getBoolean(R.styleable.ChipFiltro_filtroActivo, false)
        a.recycle()
        aplicar()
    }

    fun ponerIcono(@androidx.annotation.DrawableRes recurso: Int) {
        iconoRes = recurso
        aplicar()
    }

    private fun aplicar() {
        if (activo) {
            setBackgroundResource(R.drawable.bg_chip_negro)
            setTextColor(Color.WHITE)
            iconoInicio(if (cerrarAlActivar) R.drawable.ic_cerrar else iconoRes, 14, Color.WHITE)
        } else {
            setBackgroundResource(R.drawable.bg_chip_blanco)
            setTextColor(colorInactivo)
            iconoInicio(iconoRes, 14, colorInactivo)
        }
    }
}
