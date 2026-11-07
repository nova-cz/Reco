package com.fcc.reco.data.repository

import com.fcc.reco.data.local.AlmacenJson
import com.fcc.reco.data.model.GrupoCarrito
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray

/** Cada equipo es pieza única: no hay cantidades, solo está o no está en el carrito. */
interface CarritoRepository {
    val ids: StateFlow<Set<Int>>
    fun agregar(productoId: Int)
    fun quitar(productoId: Int)
    fun listarAgrupadoPorVendedor(): List<GrupoCarrito>
}

class CarritoRepositorioSimulado(
    private val almacen: AlmacenJson,
    private val productos: ProductoRepository
) : CarritoRepository {
    private val _ids = MutableStateFlow(leerIds(almacen))
    override val ids: StateFlow<Set<Int>> = _ids.asStateFlow()

    override fun agregar(productoId: Int) = actualizar { it + productoId }
    override fun quitar(productoId: Int) = actualizar { it - productoId }

    private fun actualizar(cambio: (Set<Int>) -> Set<Int>) {
        _ids.update(cambio)
        almacen.guardar(JSONArray(_ids.value.toList()))
    }

    override fun listarAgrupadoPorVendedor(): List<GrupoCarrito> =
        _ids.value.mapNotNull { productos.obtenerPorId(it) }
            .groupBy { it.vendedor }
            .map { (vendedor, lista) -> GrupoCarrito(vendedor, lista) }
}
