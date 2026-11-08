package com.fcc.reco.domain

/** Reglas del registro de vendedor. La CLABE es simulada: solo se revisa que tenga 18 dígitos. */
object ValidadorVendedor {
    fun telefonoValido(texto: String): Boolean = soloDigitos(texto).length == 10

    fun clabeValida(texto: String): Boolean = soloDigitos(texto).length == 18

    fun soloDigitos(texto: String): String = texto.filter { it.isDigit() }
}
