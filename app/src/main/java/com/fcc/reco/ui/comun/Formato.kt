package com.fcc.reco.ui.comun

import java.text.NumberFormat
import java.util.Locale

private val formatoPeso: NumberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX")).apply {
    maximumFractionDigits = 0
    minimumFractionDigits = 0
}

/** 9800 -> "$9,800". */
fun Int.comoPrecio(): String = formatoPeso.format(this)
