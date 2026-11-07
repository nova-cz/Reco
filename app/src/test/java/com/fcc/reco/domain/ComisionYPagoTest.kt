package com.fcc.reco.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ComisionYPagoTest {
    @Test fun comision_ejemploDelCLAUDE() {
        assertEquals(688, Comision.calcular(5000))
        assertEquals(4312, Comision.gananciaNeta(5000))
    }

    @Test fun comision_incluyeLosOchoPesosFijos() = assertEquals(8 + 14, Comision.calcular(100))

    @Test fun luhn_tarjetaDePrueba() {
        assertTrue(Luhn.esValido("4242424242424242"))
        assertFalse(Luhn.esValido("4242424242424241"))
        assertFalse(Luhn.esValido(""))
        assertFalse(Luhn.esValido("12ab"))
    }

    @Test fun numeroValido_aceptaEspacios() {
        assertTrue(ValidadorPago.numeroValido("4242 4242 4242 4242"))
        assertFalse(ValidadorPago.numeroValido("4242 4242 4242"))
    }

    @Test fun vencimiento_validaMesYVigencia() {
        assertTrue(ValidadorPago.vencimientoValido("12/30", 2026, 11))
        assertTrue(ValidadorPago.vencimientoValido("11/26", 2026, 11))
        assertFalse(ValidadorPago.vencimientoValido("10/26", 2026, 11))
        assertFalse(ValidadorPago.vencimientoValido("13/30", 2026, 11))
        assertFalse(ValidadorPago.vencimientoValido("1230", 2026, 11))
    }

    @Test fun cvc_tresDigitos() {
        assertTrue(ValidadorPago.cvcValido("123"))
        assertFalse(ValidadorPago.cvcValido("12"))
        assertFalse(ValidadorPago.cvcValido("12a"))
    }
}
