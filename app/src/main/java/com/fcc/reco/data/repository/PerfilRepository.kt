package com.fcc.reco.data.repository

import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.data.model.ModoUsuario
import com.fcc.reco.data.model.PerfilVendedor
import com.fcc.reco.data.model.Sesion

interface PerfilRepository {
    fun obtenerPerfil(): Sesion?
    fun guardarCarrera(carrera: String)
    fun cambiarModo(modo: ModoUsuario)
    fun obtenerUltimoModo(): ModoUsuario?
    /** Activa el modo vendedor con los datos del registro y lo deja como último modo usado. */
    fun activarVendedor(datos: PerfilVendedor)
}

class PerfilRepositorySimulado(private val local: SesionLocal) : PerfilRepository {

    override fun obtenerPerfil(): Sesion? = local.leer()

    override fun guardarCarrera(carrera: String) {
        local.leer()?.let { local.guardar(it.copy(carrera = carrera)) }
    }

    override fun cambiarModo(modo: ModoUsuario) {
        local.leer()?.let { local.guardar(it.copy(ultimoModo = modo)) }
    }

    override fun obtenerUltimoModo(): ModoUsuario? = local.leer()?.ultimoModo

    override fun activarVendedor(datos: PerfilVendedor) {
        local.leer()?.let { local.guardar(it.copy(vendedor = datos, ultimoModo = ModoUsuario.VENDEDOR)) }
    }
}
