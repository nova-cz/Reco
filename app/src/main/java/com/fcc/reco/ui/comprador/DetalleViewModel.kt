package com.fcc.reco.ui.comprador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import com.fcc.reco.data.Repositorios
import com.fcc.reco.data.model.Producto
import kotlinx.coroutines.flow.StateFlow

class DetalleViewModel(handle: SavedStateHandle) : ViewModel() {

    val producto: Producto? = Repositorios.productos.obtenerPorId(handle.get<Int>("productoId") ?: -1)

    val favoritos: StateFlow<Set<Int>> = Repositorios.favoritos.ids
    val carrito: StateFlow<Set<Int>> = Repositorios.carrito.ids

    fun alternarFavorito() = producto?.let { Repositorios.favoritos.alternar(it.id) }
    fun agregarAlCarrito() = producto?.let { Repositorios.carrito.agregar(it.id) }
}
