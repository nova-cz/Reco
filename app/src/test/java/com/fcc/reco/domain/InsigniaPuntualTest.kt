package com.fcc.reco.domain

import com.fcc.reco.data.model.Calificacion
import com.fcc.reco.data.model.Resena
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InsigniaPuntualTest {
    private fun resena(puntual: Boolean) =
        Resena("Luis P.", "", 5, if (puntual) listOf(Calificacion.PUNTUAL) else emptyList(), "", 0L)

    @Test fun sinResenas_noGanaLaInsignia() = assertFalse(InsigniaPuntual.gana(emptyList()))

    @Test fun setentaPorCiento_ganaLaInsignia() {
        val lista = List(7) { resena(true) } + List(3) { resena(false) }
        assertTrue(InsigniaPuntual.gana(lista))
    }

    @Test fun menosDeSetentaPorCiento_noLaGana() {
        val lista = List(2) { resena(true) } + List(1) { resena(false) }   // 66 %
        assertFalse(InsigniaPuntual.gana(lista))
    }
}
