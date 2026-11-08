package com.fcc.reco.domain

import com.fcc.reco.data.model.Calificacion
import com.fcc.reco.data.model.Resena

/** La insignia "Entrega puntual" la gana quien tiene al menos 70 % de sus reseñas marcadas como "Puntual". */
object InsigniaPuntual {
    const val UMBRAL = 0.70

    fun gana(resenas: List<Resena>): Boolean {
        if (resenas.isEmpty()) return false
        val puntuales = resenas.count { Calificacion.PUNTUAL in it.etiquetas }
        return puntuales.toDouble() / resenas.size >= UMBRAL
    }
}
