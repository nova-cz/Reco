package com.fcc.reco.ui.comun

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val local = Locale.forLanguageTag("es-MX")

/** 1731283320000 -> "10 nov". */
fun Long.comoFechaCorta(): String = SimpleDateFormat("d MMM", local).format(Date(this)).replace(".", "")

/** 1731283320000 -> "10 nov · 18:42". */
fun Long.comoFechaHora(): String = SimpleDateFormat("d MMM · HH:mm", local).format(Date(this)).replace(".", "")
