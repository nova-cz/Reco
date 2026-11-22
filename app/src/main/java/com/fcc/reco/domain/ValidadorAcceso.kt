package com.fcc.reco.domain

/** Reglas de validación del acceso. Lógica pura, sin Android, para poder probarla. */
object ValidadorAcceso {
    private val regexCorreo = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    const val LARGO_MINIMO_CLAVE = 6

    fun correoValido(correo: String): Boolean = regexCorreo.matches(correo.trim())

    /** Formato del correo institucional de la BUAP: nombre.apellido@alumno.buap.mx (o la matrícula), en minúsculas. */
    private val regexInstitucional = Regex("^[a-z0-9]+(\\.[a-z0-9]+)*@alumno\\.buap\\.mx$")

    /** Se compara en minúsculas y sin espacios alrededor (en el teléfono es fácil que la primera letra salga en mayúscula). */
    fun correoInstitucionalValido(correo: String): Boolean = regexInstitucional.matches(correo.trim().lowercase())

    fun claveValida(clave: String): Boolean = clave.length >= LARGO_MINIMO_CLAVE

    fun dominioDe(correo: String): String = correo.trim().substringAfterLast('@').lowercase()

    /** El correo es válido y su dominio está en la lista de universidades permitidas. */
    fun esInstitucional(correo: String, dominios: List<String>): Boolean =
        correoValido(correo) && dominioDe(correo) in dominios.map { it.lowercase() }

    /** "miguel.cruz@x.mx" -> "Miguel Cruz". */
    fun nombreDesdeCorreo(correo: String): String =
        correo.trim().substringBefore('@').split('.', '_', '-')
            .filter { it.isNotEmpty() }
            .joinToString(" ") { parte -> parte.replaceFirstChar { it.uppercase() } }
            .ifEmpty { "Estudiante" }
}
