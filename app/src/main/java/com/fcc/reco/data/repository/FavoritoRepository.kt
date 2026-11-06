package com.fcc.reco.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface FavoritoRepository {
    val ids: StateFlow<Set<Int>>
    fun alternar(productoId: Int)
}

class FavoritoRepositorySimulado : FavoritoRepository {
    private val _ids = MutableStateFlow<Set<Int>>(emptySet())
    override val ids: StateFlow<Set<Int>> = _ids.asStateFlow()

    override fun alternar(productoId: Int) {
        _ids.update { if (productoId in it) it - productoId else it + productoId }
    }
}
