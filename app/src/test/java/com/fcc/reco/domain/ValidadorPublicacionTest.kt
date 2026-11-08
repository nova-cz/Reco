package com.fcc.reco.domain

import com.fcc.reco.data.model.Categoria
import com.fcc.reco.domain.ValidadorPublicacion.ErrorIdentificador
import com.fcc.reco.domain.ValidadorPublicacion.ResultadoPrecio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorPublicacionTest {
    private val bloqueados = setOf("356918102345611", "BLOQUEADO001")

    @Test fun imei_pasaLuhnYTieneQuinceDigitos() {
        assertTrue(ValidadorPublicacion.validarImei("490154203237518"))
        assertFalse(ValidadorPublicacion.validarImei("490154203237519"))
        assertFalse(ValidadorPublicacion.validarImei("49015420323751"))
    }

    @Test fun identificador_celularRevisaFormatoLuhnBloqueoYDuplicado() {
        val c = Categoria.CELULARES
        assertEquals(ErrorIdentificador.VACIO, ValidadorPublicacion.validarIdentificador("", c, bloqueados, emptySet()))
        assertEquals(ErrorIdentificador.FORMATO, ValidadorPublicacion.validarIdentificador("12345", c, bloqueados, emptySet()))
        assertEquals(ErrorIdentificador.LUHN, ValidadorPublicacion.validarIdentificador("490154203237519", c, bloqueados, emptySet()))
        assertEquals(ErrorIdentificador.BLOQUEADO, ValidadorPublicacion.validarIdentificador("356918102345611", c, bloqueados, emptySet()))
        assertEquals(ErrorIdentificador.DUPLICADO, ValidadorPublicacion.validarIdentificador("490154203237518", c, bloqueados, setOf("490154203237518")))
        assertNull(ValidadorPublicacion.validarIdentificador("490 154 203 237 518", c, bloqueados, emptySet()))
    }

    @Test fun identificador_laptopAceptaNumeroDeSerie() {
        val l = Categoria.LAPTOPS
        assertNull(ValidadorPublicacion.validarIdentificador("c02fq6lr9abc", l, bloqueados, emptySet()))
        assertEquals(ErrorIdentificador.FORMATO, ValidadorPublicacion.validarIdentificador("ab", l, bloqueados, emptySet()))
        assertEquals(ErrorIdentificador.BLOQUEADO, ValidadorPublicacion.validarIdentificador("bloqueado001", l, bloqueados, emptySet()))
    }

    @Test fun fotos_entreDosYOcho() {
        assertFalse(ValidadorPublicacion.validarFotos(1))
        assertTrue(ValidadorPublicacion.validarFotos(2))
        assertTrue(ValidadorPublicacion.validarFotos(8))
        assertFalse(ValidadorPublicacion.validarFotos(9))
    }

    @Test fun precio_entreTreintaYCienPorCientoDeLaReferencia() {
        assertEquals(ResultadoPrecio.DENTRO_DE_RANGO, ValidadorPublicacion.evaluarPrecio(9800, 10500))
        assertEquals(ResultadoPrecio.DENTRO_DE_RANGO, ValidadorPublicacion.evaluarPrecio(3150, 10500))
        assertEquals(ResultadoPrecio.DENTRO_DE_RANGO, ValidadorPublicacion.evaluarPrecio(10500, 10500))
        assertEquals(ResultadoPrecio.REVISION_MANUAL, ValidadorPublicacion.evaluarPrecio(3000, 10500))
        assertEquals(ResultadoPrecio.REVISION_MANUAL, ValidadorPublicacion.evaluarPrecio(12000, 10500))
        assertEquals(ResultadoPrecio.INVALIDO, ValidadorPublicacion.evaluarPrecio(0, 10500))
    }

    @Test fun maximoTresAnunciosActivos() {
        assertTrue(ValidadorPublicacion.puedePublicarMas(2))
        assertFalse(ValidadorPublicacion.puedePublicarMas(3))
    }
}
