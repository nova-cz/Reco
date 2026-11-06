package com.fcc.reco.ui.comprador

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.fcc.reco.data.Repositorios
import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.data.model.Categoria
import com.fcc.reco.data.model.FiltrosProducto
import com.fcc.reco.data.model.Orden
import com.fcc.reco.data.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class EstadoResultados(
    val filtros: FiltrosProducto = FiltrosProducto(),
    val productos: List<Producto> = emptyList()
)

/** Los argumentos de navegación (categoria, marca, consulta) llegan por el SavedStateHandle. */
class ResultadosViewModel(aplicacion: Application, handle: SavedStateHandle) : AndroidViewModel(aplicacion) {

    private val repo = Repositorios.productos

    private val _filtros = MutableStateFlow(
        FiltrosProducto(
            categoria = handle.get<String>("categoria")?.let { runCatching { Categoria.valueOf(it) }.getOrNull() },
            marca = handle.get<String>("marca"),
            consulta = handle.get<String>("consulta").orEmpty()
        )
    )

    val estado: StateFlow<EstadoResultados> = _filtros
        .map { EstadoResultados(it, repo.obtenerPublicados(it)) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, EstadoResultados(_filtros.value, repo.obtenerPublicados(_filtros.value)))

    val favoritos: StateFlow<Set<Int>> = Repositorios.favoritos.ids

    /** Nombre corto de la carrera del estudiante ("Ing. en Ciencias de la Computación" -> "Computación"). */
    val carreraCorta: String = SesionLocal(aplicacion).leer()?.carrera?.let { carrera ->
        listOf("Computación", "Tecnologías", "Mecatrónica", "Civil", "Industrial", "Administración", "Diseño")
            .firstOrNull { carrera.contains(it) } ?: carrera
    }.orEmpty()

    val consultaInicial: String get() = _filtros.value.consulta

    fun quitarCategoria() = _filtros.update { it.copy(categoria = null) }
    fun quitarMarca() = _filtros.update { it.copy(marca = null) }
    fun alternarGrados() = _filtros.update { it.copy(soloGradosAB = !it.soloGradosAB) }
    fun alternarPrecio() = _filtros.update { it.copy(precioMax = if (it.precioMax == null) 10_000 else null) }
    fun alternarCertificado() = _filtros.update { it.copy(soloCertificados = !it.soloCertificados) }
    fun ponerConsulta(texto: String) = _filtros.update { it.copy(consulta = texto) }
    fun ponerOrden(orden: Orden) = _filtros.update { it.copy(orden = orden) }
    fun limpiar() = _filtros.update { FiltrosProducto(orden = it.orden) }
    fun alternarFavorito(producto: Producto) = Repositorios.favoritos.alternar(producto.id)
}
