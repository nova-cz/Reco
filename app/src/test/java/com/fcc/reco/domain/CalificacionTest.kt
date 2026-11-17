package com.fcc.reco.domain

import com.fcc.reco.data.model.Calificacion
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalificacionTest {

    @Test
    fun conTresEstrellasOMenosElComentarioEsObligatorio() {
        assertFalse(Calificacion.comentarioValido(3, ""))
        assertFalse(Calificacion.comentarioValido(1, "   "))
        assertTrue(Calificacion.comentarioValido(2, "No llegó a la hora"))
    }

    @Test
    fun conCuatroOMasEstrellasElComentarioEsOpcional() {
        assertTrue(Calificacion.comentarioValido(4, ""))
        assertTrue(Calificacion.comentarioValido(5, ""))
    }
}
