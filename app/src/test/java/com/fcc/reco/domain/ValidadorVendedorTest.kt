package com.fcc.reco.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorVendedorTest {
    @Test fun telefono_pideDiezDigitos() {
        assertTrue(ValidadorVendedor.telefonoValido("2221234567"))
        assertTrue(ValidadorVendedor.telefonoValido("222 123 4567"))
        assertFalse(ValidadorVendedor.telefonoValido("222123456"))
        assertFalse(ValidadorVendedor.telefonoValido(""))
    }

    @Test fun clabe_pideDieciochoDigitos() {
        assertTrue(ValidadorVendedor.clabeValida("002180001234567834"))
        assertTrue(ValidadorVendedor.clabeValida("0021 8000 1234 5678 34"))
        assertFalse(ValidadorVendedor.clabeValida("00218000123456783"))
    }

    @Test fun soloDigitos_quitaLoDemas() {
        assertEquals("2221234567", ValidadorVendedor.soloDigitos("(222) 123-4567"))
    }
}
