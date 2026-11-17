package com.fcc.reco.data.repository

import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.data.model.PerfilVendedor
import com.fcc.reco.data.model.Sesion

interface PerfilRepository {
    fun obtenerPerfil(): Sesion?
    fun guardarCarrera(carrera: String)
    /** Une al estudiante como vendedor con los datos de "Vende en Reco". */
    fun activarVendedor(datos: PerfilVendedor)
}

class PerfilRepositorySimulado(private val local: SesionLocal) : PerfilRepository {

    override fun obtenerPerfil(): Sesion? = local.leer()

    override fun guardarCarrera(carrera: String) {
        local.leer()?.let { local.guardar(it.copy(carrera = carrera)) }
    }

    override fun activarVendedor(datos: PerfilVendedor) {
        local.leer()?.let { local.guardar(it.copy(vendedor = datos)) }
    }
}
