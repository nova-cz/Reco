package com.fcc.reco.data.repository

import com.fcc.reco.data.local.AlmacenJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray

interface FavoritoRepository {
    val ids: StateFlow<Set<Int>>
    fun alternar(productoId: Int)
}

/** Favoritos en memoria, guardados en un JSON interno. */
class FavoritoRepositorySimulado(private val almacen: AlmacenJson) : FavoritoRepository {
    private val _ids = MutableStateFlow(leerIds(almacen))
    override val ids: StateFlow<Set<Int>> = _ids.asStateFlow()

    override fun alternar(productoId: Int) {
        _ids.update { if (productoId in it) it - productoId else it + productoId }
        almacen.guardar(JSONArray(_ids.value.toList()))
    }
}

internal fun leerIds(almacen: AlmacenJson): Set<Int> {
    val json = almacen.leer()
    return (0 until json.length()).map { json.getInt(it) }.toSet()
}
