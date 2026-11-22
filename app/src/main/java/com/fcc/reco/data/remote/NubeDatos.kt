package com.fcc.reco.data.remote

import com.fcc.reco.data.model.VendedorResumen
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from

/** Datos de apoyo compartidos por los repositorios de Supabase: puntos de entrega y perfiles públicos. */
class NubeDatos {
    var miUid: String? = null
    var puntos: List<PuntoDto> = emptyList()
        private set
    var perfiles: Map<String, PerfilPublicoDto> = emptyMap()
        private set

    suspend fun cargarReferencias() {
        miUid = Nube.cliente.auth.currentUserOrNull()?.id
        puntos = Nube.cliente.from("puntos_entrega").select().decodeList<PuntoDto>()
        perfiles = Nube.cliente.from("perfiles_publicos").select().decodeList<PerfilPublicoDto>().associateBy { it.id }
    }

    /** "Valeria Mendoza Ortiz" -> "Valeria M." (el nombre con el que aparece como vendedor y en reseñas). */
    fun nombreCorto(uuid: String): String = perfiles[uuid]?.nombre?.let { corto(it) } ?: "Estudiante"

    fun resumen(uuid: String): VendedorResumen {
        val p = perfiles[uuid]
        return VendedorResumen(
            nombre = nombreCorto(uuid),
            carrera = p?.carrera ?: "Estudiante BUAP",
            facultad = "BUAP",
            reputacion = p?.reputacion ?: 0.0,
            ventas = p?.ventasCompletadas ?: 0
        )
    }

    fun uuidDeVendedor(nombreCorto: String): String? = perfiles.keys.firstOrNull { nombreCorto(it) == nombreCorto }

    fun punto(uuid: String?): PuntoDto? = puntos.firstOrNull { it.id == uuid }
    fun puntoPorNombre(nombre: String): PuntoDto? = puntos.firstOrNull { it.nombre == nombre }

    /** "Biblioteca Central · Ciudad Universitaria · planta baja" */
    fun textoPunto(uuid: String?): String =
        punto(uuid)?.let { p -> listOfNotNull(p.nombre, p.descripcion).joinToString(" · ") } ?: "Ciudad Universitaria"

    companion object {
        fun corto(nombre: String): String {
            val partes = nombre.trim().split(" ").filter { it.isNotEmpty() }
            return if (partes.size >= 2) "${partes.first()} ${partes.last().first().uppercase()}." else nombre
        }
    }
}
