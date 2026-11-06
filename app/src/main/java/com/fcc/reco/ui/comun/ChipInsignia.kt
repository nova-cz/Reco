package com.fcc.reco.ui.comun

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.fcc.reco.R

/**
 * Insignia chica en forma de píldora.
 * Tipos: Estudiante verificado (azul o verde), Certificado (verde con escudo),
 * grado (negro), y neutros (blanco o gris) para batería, color, etc.
 */
class ChipInsignia @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    enum class Tipo { VERIFICADO_AZUL, VERIFICADO_VERDE, CERTIFICADO, GRADO, NEUTRO, NEUTRO_GRIS }

    private var tipo = Tipo.NEUTRO
    private var iconoRes = 0

    init {
        setTextAppearance(R.style.Reco_Texto_Etiqueta)
        gravity = Gravity.CENTER_VERTICAL
        minHeight = dp(28)
        setPadding(dp(10), 0, dp(12), 0)
        compoundDrawablePadding = dp(6)

        val a = context.obtainStyledAttributes(attrs, R.styleable.ChipInsignia, defStyleAttr, 0)
        tipo = Tipo.entries[a.getInt(R.styleable.ChipInsignia_insigniaTipo, 4)]
        iconoRes = a.getResourceId(R.styleable.ChipInsignia_insigniaIcono, 0)
        a.getString(R.styleable.ChipInsignia_insigniaTexto)?.let { text = it }
        a.recycle()
        aplicar()
    }

    fun configurar(tipo: Tipo, texto: CharSequence, icono: Int = 0) {
        this.tipo = tipo
        this.iconoRes = icono
        text = texto
        aplicar()
    }

    private fun color(id: Int) = ContextCompat.getColor(context, id)

    private fun aplicar() {
        val fondo: Int
        val texto: Int
        var icono = iconoRes
        when (tipo) {
            Tipo.VERIFICADO_AZUL -> {
                fondo = color(R.color.azul_fondo); texto = color(R.color.azul)
                if (icono == 0) icono = R.drawable.ic_verificado
            }
            Tipo.VERIFICADO_VERDE -> {
                fondo = color(R.color.verde_insignia_fondo); texto = color(R.color.verde_insignia)
                if (icono == 0) icono = R.drawable.ic_verificado
            }
            Tipo.CERTIFICADO -> {
                fondo = color(R.color.verde_insignia_fondo); texto = color(R.color.verde_insignia)
                if (icono == 0) icono = R.drawable.ic_escudo_ok
            }
            Tipo.GRADO -> {
                fondo = color(R.color.tinta); texto = color(R.color.white)
            }
            Tipo.NEUTRO -> {
                fondo = color(R.color.superficie); texto = color(R.color.tinta)
            }
            Tipo.NEUTRO_GRIS -> {
                fondo = color(R.color.superficie_gris); texto = color(R.color.tinta)
            }
        }
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpF(999f)
            setColor(fondo)
        }
        setTextColor(texto)
        (this as TextView).iconoInicio(icono, 14, texto)
        // Sin ícono, el padding de inicio se iguala al final.
        setPadding(if (icono == 0) dp(12) else dp(10), 0, dp(12), 0)
    }
}
