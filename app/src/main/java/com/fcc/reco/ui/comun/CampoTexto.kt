package com.fcc.reco.ui.comun

import android.content.Context
import android.content.res.ColorStateList
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.ImageViewCompat
import com.fcc.reco.R

/**
 * Campo de texto del Figma: etiqueta arriba, caja blanca de 56 dp con ícono a la izquierda y,
 * si es contraseña, un ojo para mostrarla u ocultarla.
 */
class CampoTexto @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val editText: EditText = EditText(context)
    private val etiquetaVista = TextView(context)
    private val caja = LinearLayout(context)
    private val iconoVista = ImageView(context)
    private val ojoVista = ImageView(context)
    private var contrasenaVisible = false
    private var esContrasena = false

    var texto: String
        get() = editText.text?.toString().orEmpty()
        set(valor) = editText.setText(valor)

    init {
        orientation = VERTICAL
        val secundario = ContextCompat.getColor(context, R.color.texto_secundario)

        val a = context.obtainStyledAttributes(attrs, R.styleable.CampoTexto, defStyleAttr, 0)
        val etiqueta = a.getString(R.styleable.CampoTexto_campoEtiqueta)
        val hint = a.getString(R.styleable.CampoTexto_campoHint)
        val iconoRes = a.getResourceId(R.styleable.CampoTexto_campoIcono, 0)
        val tipo = a.getInt(R.styleable.CampoTexto_campoTipo, 0)
        a.recycle()

        // Etiqueta
        etiquetaVista.setTextAppearance(R.style.Reco_Texto_Etiqueta)
        etiquetaVista.textSize = 13f
        etiquetaVista.setTextColor(secundario)
        if (etiqueta.isNullOrEmpty()) {
            etiquetaVista.visibility = View.GONE
        } else {
            etiquetaVista.text = etiqueta
        }
        addView(etiquetaVista, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = dp(8)
        })

        // Caja
        caja.orientation = HORIZONTAL
        caja.gravity = Gravity.CENTER_VERTICAL
        caja.setBackgroundResource(R.drawable.bg_campo)
        caja.minimumHeight = dp(56)
        caja.setPadding(dp(16), 0, dp(8), 0)
        addView(caja, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        // Ícono
        if (iconoRes != 0) {
            iconoVista.setImageResource(iconoRes)
            ImageViewCompat.setImageTintList(iconoVista, ColorStateList.valueOf(secundario))
            caja.addView(iconoVista, LinearLayout.LayoutParams(dp(20), dp(20)).apply { marginEnd = dp(12) })
        } else {
            caja.setPadding(dp(16), 0, dp(8), 0)
        }

        // Campo
        editText.background = null
        editText.setTextAppearance(R.style.Reco_Texto)
        editText.setHintTextColor(ContextCompat.getColor(context, R.color.texto_secundario))
        editText.hint = hint
        editText.setPadding(0, dp(12), 0, dp(12))
        editText.maxLines = 1
        when (tipo) {
            1 -> editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            2 -> {
                esContrasena = true
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            else -> editText.inputType = InputType.TYPE_CLASS_TEXT
        }
        editText.typeface = ResourcesCompat.getFont(context, R.font.inter_regular)
        caja.addView(editText, LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))

        // Ojo (solo contraseña)
        if (esContrasena) {
            ojoVista.setImageResource(R.drawable.ic_ojo)
            ImageViewCompat.setImageTintList(ojoVista, ColorStateList.valueOf(secundario))
            ojoVista.setPadding(dp(8), dp(8), dp(8), dp(8))
            ojoVista.contentDescription = context.getString(R.string.mostrar_contrasena)
            ojoVista.setOnClickListener { alternarContrasena() }
            caja.addView(ojoVista, LinearLayout.LayoutParams(dp(40), dp(40)))
        }
    }

    private fun alternarContrasena() {
        contrasenaVisible = !contrasenaVisible
        val seleccion = editText.selectionEnd
        editText.transformationMethod =
            if (contrasenaVisible) HideReturnsTransformationMethod.getInstance()
            else PasswordTransformationMethod.getInstance()
        ojoVista.setImageResource(if (contrasenaVisible) R.drawable.ic_ojo_cerrado else R.drawable.ic_ojo)
        editText.setSelection(seleccion.coerceAtLeast(0))
    }
}
