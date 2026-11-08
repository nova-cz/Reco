package com.fcc.reco.ui.comprador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fcc.reco.data.Repositorios
import com.fcc.reco.data.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class FiltroFavoritos { TODOS, DISPONIBLES, VENDIDOS }

data class EstadoFavoritos(
    val filtro: FiltroFavoritos = FiltroFavoritos.TODOS,
    /** Total de equipos guardados, sin importar el filtro. */
    val total: Int = 0,
    /** Equipos a mostrar según el filtro. */
    val productos: List<Producto> = emptyList(),
    val vendidos: Set<Int> = emptySet()
)

class FavoritosViewModel : ViewModel() {

    private val filtro = MutableStateFlow(FiltroFavoritos.TODOS)

    val estado: StateFlow<EstadoFavoritos> = combine(
        Repositorios.favoritos.ids,
        // Los pedidos cambian cuando un equipo guardado se vende.
        Repositorios.pedidos.pedidos,
        filtro
    ) { ids, _, filtroActual ->
        val productos = ids.reversed().mapNotNull { Repositorios.productos.obtenerPorId(it) }
        val vendidos = productos.filter { Repositorios.productos.estaVendido(it.id) }.map { it.id }.toSet()
        EstadoFavoritos(
            filtro = filtroActual,
            total = productos.size,
            productos = when (filtroActual) {
                FiltroFavoritos.TODOS -> productos
                FiltroFavoritos.DISPONIBLES -> productos.filter { it.id !in vendidos }
                FiltroFavoritos.VENDIDOS -> productos.filter { it.id in vendidos }
            },
            vendidos = vendidos
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoFavoritos())

    fun ponerFiltro(nuevo: FiltroFavoritos) {
        filtro.value = nuevo
    }

    fun alternarFavorito(producto: Producto) = Repositorios.favoritos.alternar(producto.id)
}
