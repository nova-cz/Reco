package com.fcc.reco.data.repository

import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.data.model.Sesion
import com.fcc.reco.domain.ValidadorAcceso
import kotlinx.coroutines.delay

interface AuthRepository {
    fun sesionActual(): Sesion?
    suspend fun registrarConCorreo(correo: String, clave: String): Result<Sesion>
    suspend fun iniciarConCorreo(correo: String, clave: String): Result<Sesion>
    suspend fun iniciarConGoogle(): Result<Sesion>
    fun cerrarSesion()
}

/** Versión en memoria/local. La versión real usará supabase.auth. */
class AuthRepositorySimulado(private val local: SesionLocal) : AuthRepository {

    override fun sesionActual(): Sesion? = local.leer()

    override suspend fun registrarConCorreo(correo: String, clave: String): Result<Sesion> =
        entrar(correo)

    override suspend fun iniciarConCorreo(correo: String, clave: String): Result<Sesion> =
        entrar(correo)

    override suspend fun iniciarConGoogle(): Result<Sesion> = entrar("estudiante@gmail.com")

    override fun cerrarSesion() = local.borrar()

    /** Si el correo ya tenía perfil guardado lo conserva; si no, crea uno nuevo. */
    private suspend fun entrar(correo: String): Result<Sesion> {
        delay(600) // simula la red
        val limpio = correo.trim().lowercase()
        val previa = local.leer()?.takeIf { it.correo == limpio }
        val sesion = previa ?: Sesion(correo = limpio, nombre = ValidadorAcceso.nombreDesdeCorreo(limpio))
        local.guardar(sesion)
        return Result.success(sesion)
    }
}
