package com.fcc.reco.data.model

/** Sesión del estudiante (simulada en el dispositivo hasta conectar Supabase). */
data class Sesion(
    val correo: String,
    val nombre: String,
    val verificado: Boolean = false,
    val universidad: String? = null,
    val carrera: String? = null,
    val ultimoModo: ModoUsuario? = null,
    /** Datos de vendedor; null mientras el estudiante no haya activado el modo vendedor. */
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

    /** Ya verificado, con carrera y con un modo elegido: puede entrar directo a la app. */
    val completa: Boolean get() = verificado && carrera != null && ultimoModo != null

    val iniciales: String
        get() = nombre.trim().split(" ").filter { it.isNotEmpty() }
            .take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" }
}

/** Lo que se pide para activar el modo vendedor. La CLABE es simulada. */
data class PerfilVendedor(
    val telefono: String,
    val puntoEntrega: String,
    val dias: List<String>,
    val franjas: List<String>,
    val clabe: String
)
