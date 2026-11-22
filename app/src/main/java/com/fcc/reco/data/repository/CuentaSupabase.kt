package com.fcc.reco.data.repository

import android.net.Uri
import com.fcc.reco.data.DatosSimulados
import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.data.model.PerfilVendedor
import com.fcc.reco.data.model.Sesion
import com.fcc.reco.data.remote.Nube
import com.fcc.reco.data.remote.NubeDatos
import com.fcc.reco.data.remote.PerfilDto
import com.fcc.reco.data.remote.epochDe
import com.fcc.reco.domain.ValidadorAcceso
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Mantiene la [Sesion] local (que leen las pantallas) al día con el perfil de Supabase.
 * Si la cuenta es nueva, crea su fila en `perfiles`.
 */
class SesionNube(private val local: SesionLocal) {

    suspend fun refrescar(): Sesion? {
        val usuario = Nube.cliente.auth.currentUserOrNull() ?: return null
        val correo = usuario.email.orEmpty().lowercase()
        var perfil = leerPerfil(usuario.id)
        if (perfil == null) {
            Nube.cliente.from("perfiles").insert(buildJsonObject {
                put("id", usuario.id)
                put("nombre", ValidadorAcceso.nombreDesdeCorreo(correo))
            })
            perfil = leerPerfil(usuario.id)
        }
        val sesion = perfil?.let { aSesion(it, correo) } ?: return null
        local.guardar(sesion)
        return sesion
    }

    private suspend fun leerPerfil(uid: String): PerfilDto? =
        Nube.cliente.from("perfiles").select(
            Columns.raw("id,nombre,carrera,verificado,es_vendedor,telefono,disponibilidad,clabe_simulada,apodo,acerca_de,creado,universidades(nombre),puntos_entrega(nombre)")
        ) { filter { eq("id", uid) } }.decodeSingleOrNull<PerfilDto>()

    private fun aSesion(p: PerfilDto, correo: String): Sesion {
        fun lista(clave: String) = (p.disponibilidad?.get(clave) as? JsonArray)?.map { it.jsonPrimitive.content }.orEmpty()
        return Sesion(
            correo = correo, nombre = p.nombre, verificado = p.verificado,
            universidad = p.universidades?.nombre, carrera = p.carrera,
            vendedor = if (p.esVendedor) PerfilVendedor(
                telefono = p.telefono.orEmpty(), puntoEntrega = p.puntoEntrega?.nombre.orEmpty(),
                dias = lista("dias"), franjas = lista("franjas"), clabe = p.clabe.orEmpty(),
                apodo = p.apodo.orEmpty(), acerca = p.acercaDe.orEmpty()
            ) else null,
            creado = epochDe(p.creado)
        )
    }
}

class AuthRepositorySupabase(private val local: SesionLocal, private val sesionNube: SesionNube) : AuthRepository {

    override fun sesionActual(): Sesion? = local.leer()

    override suspend fun registrarConCorreo(correo: String, clave: String): Result<Sesion> = runCatching {
        val limpio = correo.trim().lowercase()
        Nube.cliente.auth.signUpWith(Email) { email = limpio; password = clave }
        // Con la confirmación de correo desactivada el registro ya inicia sesión; si no, se intenta entrar.
        if (Nube.cliente.auth.currentSessionOrNull() == null) {
            Nube.cliente.auth.signInWith(Email) { email = limpio; password = clave }
        }
        sesionNube.refrescar() ?: error("Sin perfil")
    }

    override suspend fun iniciarConCorreo(correo: String, clave: String): Result<Sesion> = runCatching {
        Nube.cliente.auth.signInWith(Email) { email = correo.trim().lowercase(); password = clave }
        sesionNube.refrescar() ?: error("Sin perfil")
    }

    override suspend fun iniciarConGoogle(): Result<Sesion> =
        Result.failure(UnsupportedOperationException("Google Sign-In real queda para después"))

    override fun cerrarSesion() {
        local.borrar()
        Nube.alcance.launch { runCatching { Nube.cliente.auth.signOut() } }
    }
}

/** El portal institucional sigue simulado; al volver por deep link, el servidor valida el dominio del correo. */
class VerificacionRepositorySupabase(private val sesionNube: SesionNube) : VerificacionRepository {

    override fun dominioValido(correo: String): Boolean =
        ValidadorAcceso.esInstitucional(correo, DatosSimulados.universidades.map { it.dominio })

    override suspend fun procesarDeepLink(uri: Uri): Result<Sesion> {
        if (uri.scheme != "reco" || uri.host != "verificado") return Result.failure(IllegalArgumentException("enlace"))
        val correo = uri.getQueryParameter("correo").orEmpty()
        if (!dominioValido(correo)) return Result.failure(IllegalArgumentException("dominio"))
        return runCatching {
            Nube.cliente.postgrest.rpc("verificar_estudiante", buildJsonObject { put("p_correo", correo) })
            sesionNube.refrescar() ?: error("Sin perfil")
        }
    }
}

class PerfilRepositorySupabase(
    private val local: SesionLocal,
    private val sesionNube: SesionNube,
    private val datos: NubeDatos
) : PerfilRepository {

    override fun obtenerPerfil(): Sesion? = local.leer()

    override suspend fun guardarCarrera(carrera: String) {
        val uid = Nube.cliente.auth.currentUserOrNull()?.id ?: return
        Nube.cliente.from("perfiles").update({ set("carrera", carrera) }) { filter { eq("id", uid) } }
        sesionNube.refrescar()
    }

    override suspend fun activarVendedor(datos: PerfilVendedor) {
        val uid = Nube.cliente.auth.currentUserOrNull()?.id ?: return
        val punto = this.datos.puntoPorNombre(datos.puntoEntrega)
        Nube.cliente.postgrest.rpc("activar_vendedor", buildJsonObject {
            put("p_telefono", datos.telefono)
            put("p_punto", punto?.id?.let { JsonPrimitive(it) } ?: JsonNull)
            put("p_disponibilidad", JsonObject(mapOf(
                "dias" to JsonArray(datos.dias.map { JsonPrimitive(it) }),
                "franjas" to JsonArray(datos.franjas.map { JsonPrimitive(it) })
            )))
            put("p_clabe", datos.clabe)
        })
        // Datos públicos del vendedor y respuestas de las preguntas rápidas del Detalle.
        Nube.cliente.from("perfiles").update({
            set("apodo", datos.apodo.ifBlank { null })
            set("acerca_de", datos.acerca.ifBlank { null })
            set("donde_entregas", "En ${datos.puntoEntrega}")
            set("horario", "${datos.dias.joinToString(", ")} · ${datos.franjas.joinToString(", ").lowercase()}")
        }) { filter { eq("id", uid) } }
        sesionNube.refrescar()
    }
}
