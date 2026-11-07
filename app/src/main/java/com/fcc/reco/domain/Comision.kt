package com.fcc.reco.domain

import kotlin.math.roundToInt

/** Comisión de Reco (igual a la de eBay para electrónica): 13.6 % más $8 MXN por pedido. */
object Comision {
    const val PORCENTAJE = 0.136
    const val FIJA = 8

    fun calcular(monto: Int): Int = (monto * PORCENTAJE).roundToInt() + FIJA

    /** Lo que recibe el vendedor: monto menos comisión. */
    fun gananciaNeta(monto: Int): Int = monto - calcular(monto)
}
