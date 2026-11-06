package com.fcc.reco.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Cada equipo es pieza única: no hay cantidades, solo está o no está en el carrito. */
interface CarritoRepository {
    val ids: StateFlow<Set<Int>>
    fun agregar(productoId: Int)
    fun quitar(productoId: Int)
}

class CarritoRepositorioSimulado : CarritoRepository {
    private val _ids = MutableStateFlow<Set<Int>>(emptySet())
    override val ids: StateFlow<Set<Int>> = _ids.asStateFlow()

    override fun agregar(productoId: Int) = _ids.update { it + productoId }
    override fun quitar(productoId: Int) = _ids.update { it - productoId }
}
