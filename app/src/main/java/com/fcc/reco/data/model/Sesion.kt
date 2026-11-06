package com.fcc.reco.data.model

/** Sesión del estudiante (simulada en el dispositivo hasta conectar Supabase). */
data class Sesion(
    val correo: String,
    val nombre: String,
    val verificado: Boolean = false,
    val universidad: String? = null,
    val carrera: String? = null,
    val ultimoModo: ModoUsuario? = null
) {
    /** Ya verificado, con carrera y con un modo elegido: puede entrar directo a la app. */
    val completa: Boolean get() = verificado && carrera != null && ultimoModo != null

    val iniciales: String
        get() = nombre.trim().split(" ").filter { it.isNotEmpty() }
            .take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" }
}
