package com.fcc.reco.data.model

/** Sesión del estudiante (simulada en el dispositivo hasta conectar Supabase). */
data class Sesion(
    val correo: String,
    val nombre: String,
    val verificado: Boolean = false,
    val universidad: String? = null,
    val carrera: String? = null,
    /** Datos de vendedor; null mientras el estudiante no se haya unido como vendedor. */
    val vendedor: PerfilVendedor? = null,
    /** Cuándo se creó la cuenta (para "Miembro desde"). */
    val creado: Long = 0L
) {
    val esVendedor: Boolean get() = vendedor != null

    /** "Miguel C.": el nombre con el que aparece como vendedor y en las reseñas. */
    val nombreCorto: String
        get() {
            val partes = nombre.trim().split(" ").filter { it.isNotEmpty() }
            return if (partes.size >= 2) "${partes.first()} ${partes.last().first().uppercase()}." else nombre
        }

    /** Ya verificado y con carrera: puede entrar directo a la app. */
    val completa: Boolean get() = verificado && carrera != null

    val iniciales: String
        get() = nombre.trim().split(" ").filter { it.isNotEmpty() }
            .take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" }
}

/** Lo que se pide para unirse como vendedor. La CLABE es simulada. */
data class PerfilVendedor(
    val telefono: String,
    val puntoEntrega: String,
    val dias: List<String>,
    val franjas: List<String>,
    val clabe: String,
    /** Apodo con el que te conocen en el campus (opcional). */
    val apodo: String = "",
    /** "Acerca de ti" (opcional). */
    val acerca: String = ""
)
