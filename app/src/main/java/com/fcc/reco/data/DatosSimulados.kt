package com.fcc.reco.data

import com.fcc.reco.data.model.Universidad

/** Datos de prueba en memoria. Se reemplazan por Supabase más adelante. */
object DatosSimulados {
    val universidades = listOf(Universidad("BUAP", "alumno.buap.mx"))

    val carreras = listOf(
        "Ing. en Ciencias de la Computación",
        "Ing. en Tecnologías de la Información",
        "Lic. en Ciencias de la Computación",
        "Ing. Mecatrónica",
        "Ing. Civil",
        "Ing. Industrial",
        "Administración de Empresas",
        "Diseño Gráfico",
        "Otra carrera"
    )
}
