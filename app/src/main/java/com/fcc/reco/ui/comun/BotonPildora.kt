package com.fcc.reco.ui.comun

import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.fcc.reco.R

/**
 * Botón en forma de píldora.
 * - primario: negro con degradado sutil y texto blanco.
 * - secundario: blanco con borde suave y texto tinta.
 * Admite un ícono opcional al inicio (`app:botonIcono`).
 */
class BotonPildora @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    enum class Estilo { PRIMARIO, SECUNDARIO }

    var estilo: Estilo = Estilo.PRIMARIO
        set(valor) {
            field = valor
            aplicarEstilo()
        }

    private var iconoRes = 0

    init {
        setTextAppearance(R.style.Reco_Texto_Boton)
        minHeight = dp(56)
        gravity = Gravity.CENTER
        setPadding(dp(24), 0, dp(24), 0)
        compoundDrawablePadding = dp(10)
        isClickable = true
        isFocusable = true
        // Contorno de píldora explícito: así la sombra no sale rectangular.
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, view.height / 2f)
            }
        }

        val a = context.obtainStyledAttributes(attrs, R.styleable.BotonPildora, defStyleAttr, 0)
        estilo = Estilo.entries[a.getInt(R.styleable.BotonPildora_botonEstilo, 0)]
        iconoRes = a.getResourceId(R.styleable.BotonPildora_botonIcono, 0)
        a.recycle()
        aplicarEstilo()
    }

    /** Cambia el ícono de inicio (0 para quitarlo). */
    fun ponerIcono(recurso: Int) {
        iconoRes = recurso
        aplicarEstilo()
    }

    private fun aplicarEstilo() {
        val color: Int
        if (estilo == Estilo.PRIMARIO) {
            setBackgroundResource(R.drawable.bg_boton_primario)
            color = Color.WHITE
            elevation = dpF(8f)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                outlineSpotShadowColor = 0x66111114
                outlineAmbientShadowColor = 0x33111114
            }
        } else {
            setBackgroundResource(R.drawable.bg_boton_secundario)
            color = ContextCompat.getColor(context, R.color.tinta)
            elevation = 0f
        }
        setTextColor(color)
        iconoInicio(iconoRes, 20, color)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        alpha = if (enabled) 1f else 0.4f
    }
}
