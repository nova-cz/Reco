package com.fcc.reco.ui.comun

import android.content.Context
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import android.content.res.ColorStateList
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.TextViewCompat

fun Context.dp(valor: Int): Int = (valor * resources.displayMetrics.density + 0.5f).toInt()

fun Context.dpF(valor: Float): Float = valor * resources.displayMetrics.density

fun View.dp(valor: Int): Int = context.dp(valor)

fun View.dpF(valor: Float): Float = context.dpF(valor)

/** Pone un ícono (con tamaño y color) al inicio del texto. Pasa 0 para quitarlo. */
fun TextView.iconoInicio(@DrawableRes recurso: Int, tamanoDp: Int, color: Int) {
    if (recurso == 0) {
        setCompoundDrawablesRelative(null, null, null, null)
        return
    }
    val icono = AppCompatResources.getDrawable(context, recurso)?.mutate() ?: return
    val tamano = context.dp(tamanoDp)
    icono.setBounds(0, 0, tamano, tamano)
    setCompoundDrawablesRelative(icono, null, null, null)
    TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(color))
}

/**
 * Aplica las barras del sistema como padding (arriba y/o abajo) a la vista.
 * Sirve para pantallas edge-to-edge.
 */
fun View.ajustarPorBarrasDelSistema(arriba: Boolean = true, abajo: Boolean = true) {
    val inicial = intArrayOf(paddingLeft, paddingTop, paddingRight, paddingBottom)
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val barras = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        v.setPadding(
            inicial[0] + barras.left,
            inicial[1] + if (arriba) barras.top else 0,
            inicial[2] + barras.right,
            inicial[3] + if (abajo) barras.bottom else 0
        )
        insets
    }
    ViewCompat.requestApplyInsets(this)
}

/** Edge-to-edge con barras transparentes e íconos oscuros (Reco siempre es claro, aunque el sistema esté en oscuro). */
fun ComponentActivity.edgeToEdgeClaro() {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
    )
}
