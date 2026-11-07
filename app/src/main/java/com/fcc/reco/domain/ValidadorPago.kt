package com.fcc.reco.domain

/** Validación de la tarjeta (simulada, modo de prueba). Tarjeta de prueba: 4242 4242 4242 4242. */
object ValidadorPago {
    fun soloDigitos(texto: String) = texto.filter { it.isDigit() }

    fun numeroValido(numero: String): Boolean {
        val d = soloDigitos(numero)
        return d.length == 16 && Luhn.esValido(d)
    }

    /** "MM/AA" con mes 01-12 y que no esté vencida respecto a [anio] (4 dígitos) y [mes]. */
    fun vencimientoValido(texto: String, anio: Int, mes: Int): Boolean {
        val partes = texto.trim().split("/")
        if (partes.size != 2) return false
        val m = partes[0].toIntOrNull() ?: return false
        val a = partes[1].toIntOrNull() ?: return false
        if (partes[1].length != 2 || m !in 1..12) return false
        val anioCompleto = 2000 + a
        return anioCompleto > anio || (anioCompleto == anio && m >= mes)
    }

    fun cvcValido(cvc: String) = cvc.length == 3 && cvc.all { it.isDigit() }

    fun ultimos4(numero: String) = soloDigitos(numero).takeLast(4)
}
