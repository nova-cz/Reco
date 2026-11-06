package com.fcc.reco.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorAccesoTest {
    private val dominios = listOf("alumno.buap.mx")

    @Test fun correoValido_aceptaCorreosNormales() {
        assertTrue(ValidadorAcceso.correoValido("miguel.cruz@alumno.buap.mx"))
        assertTrue(ValidadorAcceso.correoValido("  ana@gmail.com "))
    }

    @Test fun correoValido_rechazaMalFormados() {
        assertFalse(ValidadorAcceso.correoValido(""))
        assertFalse(ValidadorAcceso.correoValido("sin-arroba"))
        assertFalse(ValidadorAcceso.correoValido("a@b"))
    }

    @Test fun claveValida_pideSeisCaracteres() {
        assertFalse(ValidadorAcceso.claveValida("12345"))
        assertTrue(ValidadorAcceso.claveValida("123456"))
    }

    @Test fun esInstitucional_validaElDominio() {
        assertTrue(ValidadorAcceso.esInstitucional("a@alumno.buap.mx", dominios))
        assertTrue(ValidadorAcceso.esInstitucional("A@ALUMNO.BUAP.MX", dominios))
        assertFalse(ValidadorAcceso.esInstitucional("a@gmail.com", dominios))
        assertFalse(ValidadorAcceso.esInstitucional("a@otro.alumno.buap.mx.falso.com", dominios))
        assertFalse(ValidadorAcceso.esInstitucional("alumno.buap.mx", dominios))
    }

    @Test fun nombreDesdeCorreo_capitalizaPartes() {
        assertEquals("Miguel Cruz", ValidadorAcceso.nombreDesdeCorreo("miguel.cruz@alumno.buap.mx"))
        assertEquals("Estudiante", ValidadorAcceso.nombreDesdeCorreo("@x.com"))
    }
}
