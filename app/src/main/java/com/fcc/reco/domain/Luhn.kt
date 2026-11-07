package com.fcc.reco.domain

/** Algoritmo de Luhn (se usa para tarjetas y, más adelante, para el IMEI de 15 dígitos). */
object Luhn {
    fun esValido(digitos: String): Boolean {
        if (digitos.isEmpty() || digitos.any { !it.isDigit() }) return false
        var suma = 0
        digitos.reversed().forEachIndexed { indice, c ->
            var d = c - '0'
            if (indice % 2 == 1) {
                d *= 2
                if (d > 9) d -= 9
            }
            suma += d
        }
        return suma % 10 == 0
    }
}
