package com.fcc.reco.data.repository

import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.data.model.PerfilVendedor
import com.fcc.reco.data.model.Sesion

interface PerfilRepository {
    fun obtenerPerfil(): Sesion?
    suspend fun guardarCarrera(carrera: String)
    /** Une al estudiante como vendedor con los datos de "Vende en Reco". */
    suspend fun activarVendedor(datos: PerfilVendedor)
}

class PerfilRepositorySimulado(private val local: SesionLocal) : PerfilRepository {

    override fun obtenerPerfil(): Sesion? = local.leer()

    override suspend fun guardarCarrera(carrera: String) {
        local.leer()?.let { local.guardar(it.copy(carrera = carrera)) }
    }

    override suspend fun activarVendedor(datos: PerfilVendedor) {
        local.leer()?.let { local.guardar(it.copy(vendedor = datos)) }
    }
}
