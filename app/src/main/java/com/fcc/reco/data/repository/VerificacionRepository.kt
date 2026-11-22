package com.fcc.reco.data.repository

import android.net.Uri
import com.fcc.reco.data.DatosSimulados
import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.data.model.Sesion
import com.fcc.reco.domain.ValidadorAcceso

interface VerificacionRepository {
    fun dominioValido(correo: String): Boolean
    /** Procesa `reco://verificado?correo=...` al volver del login institucional. */
    suspend fun procesarDeepLink(uri: Uri): Result<Sesion>

    /** Abre el login de Microsoft (liga la identidad a la cuenta si ya hay sesión). Regresa por `reco://auth`. */
    suspend fun iniciarMicrosoft(): Result<Unit>
    /** El servidor comprueba la identidad Microsoft o la sesión por código y marca al estudiante como verificado. */
    suspend fun completarVerificacion(): Result<Sesion>
    /** Plan B: envía un código al correo institucional. */
    suspend fun enviarCodigo(correo: String): Result<Unit>
    /** Plan B: confirma el código (abre la sesión de ese correo) y verifica en el servidor. */
    suspend fun confirmarCodigo(correo: String, codigo: String): Result<Sesion>
}

/** Plan B: el portal institucional es simulado, pero el regreso por deep link es real. */
class VerificacionRepositorySimulado(private val local: SesionLocal) : VerificacionRepository {

    override fun dominioValido(correo: String): Boolean = ValidadorAcceso.correoInstitucionalValido(correo)

    // La verificación real (Microsoft y código) solo existe con Supabase.
    override suspend fun iniciarMicrosoft(): Result<Unit> = Result.failure(UnsupportedOperationException())
    override suspend fun completarVerificacion(): Result<Sesion> = Result.failure(UnsupportedOperationException())
    override suspend fun enviarCodigo(correo: String): Result<Unit> = Result.failure(UnsupportedOperationException())
    override suspend fun confirmarCodigo(correo: String, codigo: String): Result<Sesion> =
        Result.failure(UnsupportedOperationException())

    override suspend fun procesarDeepLink(uri: Uri): Result<Sesion> {
        if (uri.scheme != "reco" || uri.host != "verificado") {
            return Result.failure(IllegalArgumentException("enlace"))
        }
        val correo = uri.getQueryParameter("correo").orEmpty()
        if (!dominioValido(correo)) return Result.failure(IllegalArgumentException("dominio"))

        val universidad = DatosSimulados.universidades
            .first { it.dominio == ValidadorAcceso.dominioDe(correo) }
        val actual = local.leer() ?: Sesion(correo = correo, nombre = ValidadorAcceso.nombreDesdeCorreo(correo))
        val verificada = actual.copy(verificado = true, universidad = universidad.nombre)
        local.guardar(verificada)
        return Result.success(verificada)
    }
}
