package com.fcc.reco.ui.comprador

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fcc.reco.data.Repositorios
import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.data.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EstadoInicio(
    val nombre: String = "",
    val universidad: String = "",
    val destacado: Producto? = null,
    val marcas: List<String> = emptyList(),
    val enTuCampus: List<Producto> = emptyList()
)

class InicioViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {

    private val productos = Repositorios.productos
    private val favoritosRepo = Repositorios.favoritos

    private val _estado = MutableStateFlow(EstadoInicio())
    val estado: StateFlow<EstadoInicio> = _estado.asStateFlow()

    val favoritos: StateFlow<Set<Int>> = favoritosRepo.ids

    init {
        val sesion = SesionLocal(aplicacion).leer()
        _estado.value = EstadoInicio(
            nombre = sesion?.nombre.orEmpty(),
            universidad = sesion?.universidad ?: "BUAP",
            destacado = productos.destacado(),
            marcas = productos.marcas().take(6),
            enTuCampus = productos.obtenerPublicados().take(6)
        )
    }

    fun alternarFavorito(producto: Producto) = favoritosRepo.alternar(producto.id)
}
