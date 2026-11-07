package com.fcc.reco.ui.comprador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fcc.reco.data.Repositorios
import com.fcc.reco.data.model.GrupoCarrito
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class EstadoCarrito(val grupos: List<GrupoCarrito> = emptyList()) {
    val total: Int get() = grupos.sumOf { it.subtotal }
    val cantidad: Int get() = grupos.sumOf { it.productos.size }
}

class CarritoViewModel : ViewModel() {
    private val carrito = Repositorios.carrito

    val estado: StateFlow<EstadoCarrito> = carrito.ids
        .map { EstadoCarrito(carrito.listarAgrupadoPorVendedor()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, EstadoCarrito(carrito.listarAgrupadoPorVendedor()))

    fun quitar(productoId: Int) = carrito.quitar(productoId)
}
