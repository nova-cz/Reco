package com.fcc.reco.data.repository

import android.net.Uri
import com.fcc.reco.data.DatosSimulados
import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.data.model.Sesion
import com.fcc.reco.domain.ValidadorAcceso

interface VerificacionRepository {
    fun dominioValido(correo: String): Boolean
    /** Procesa `reco://verificado?correo=...` al volver del login institucional. */
    fun procesarDeepLink(uri: Uri): Result<Sesion>
}

/** Plan B: el portal institucional es simulado, pero el regreso por deep link es real. */
class VerificacionRepositorySimulado(private val local: SesionLocal) : VerificacionRepository {

    override fun dominioValido(correo: String): Boolean =
        ValidadorAcceso.esInstitucional(correo, DatosSimulados.universidades.map { it.dominio })

    override fun procesarDeepLink(uri: Uri): Result<Sesion> {
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
