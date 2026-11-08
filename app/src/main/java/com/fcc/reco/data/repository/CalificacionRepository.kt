package com.fcc.reco.data.repository

import com.fcc.reco.data.local.AlmacenJson
import com.fcc.reco.data.model.Calificacion
import com.fcc.reco.data.model.EstadoPedido
import com.fcc.reco.data.model.Reputacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

interface CalificacionRepository {
    val calificaciones: StateFlow<List<Calificacion>>
    /** Solo se puede calificar un pedido liberado y una sola vez. Devuelve null si no se pudo. */
    fun calificarVendedor(pedidoId: Int, estrellas: Int, etiquetas: List<String>, comentario: String): Calificacion?
    fun obtenerPorPedido(pedidoId: Int): Calificacion?
    fun obtenerReputacion(vendedor: String): Reputacion?
}

/** Calificaciones en memoria, guardadas en un JSON interno. */
class CalificacionRepositorySimulado(
    private val almacen: AlmacenJson,
    private val pedidos: PedidoRepository
) : CalificacionRepository {

    private val _calificaciones = MutableStateFlow(leer())
    override val calificaciones: StateFlow<List<Calificacion>> = _calificaciones.asStateFlow()

    override fun calificarVendedor(
        pedidoId: Int, estrellas: Int, etiquetas: List<String>, comentario: String
    ): Calificacion? {
        val pedido = pedidos.obtener(pedidoId) ?: return null
        if (pedido.estado != EstadoPedido.LIBERADO || estrellas !in 1..5) return null
        if (obtenerPorPedido(pedidoId) != null) return null
        val nueva = Calificacion(
            id = (_calificaciones.value.maxOfOrNull { it.id } ?: 0) + 1,
            pedidoId = pedidoId,
            vendedor = pedido.vendedor.nombre,
            estrellas = estrellas,
            etiquetas = etiquetas,
            comentario = comentario.trim(),
            creado = System.currentTimeMillis()
        )
        _calificaciones.value = _calificaciones.value + nueva
        guardar()
        pedidos.registrarEstrellas(pedidoId, estrellas)
        return nueva
    }

    override fun obtenerPorPedido(pedidoId: Int): Calificacion? =
        _calificaciones.value.firstOrNull { it.pedidoId == pedidoId }

    override fun obtenerReputacion(vendedor: String): Reputacion? {
        val propias = _calificaciones.value.filter { it.vendedor == vendedor }
        if (propias.isEmpty()) return null
        return Reputacion(propias.map { it.estrellas }.average(), propias.size)
    }

    private fun guardar() {
        val json = JSONArray()
        _calificaciones.value.forEach { c ->
            json.put(JSONObject().apply {
                put("id", c.id); put("pedidoId", c.pedidoId); put("vendedor", c.vendedor)
                put("estrellas", c.estrellas); put("etiquetas", JSONArray(c.etiquetas))
                put("comentario", c.comentario); put("creado", c.creado)
            })
        }
        almacen.guardar(json)
    }

    private fun leer(): List<Calificacion> {
        val json = almacen.leer()
        return (0 until json.length()).mapNotNull { i ->
            runCatching {
                val o = json.getJSONObject(i)
                val et = o.getJSONArray("etiquetas")
                Calificacion(
                    id = o.getInt("id"), pedidoId = o.getInt("pedidoId"), vendedor = o.getString("vendedor"),
                    estrellas = o.getInt("estrellas"), etiquetas = (0 until et.length()).map { et.getString(it) },
                    comentario = o.getString("comentario"), creado = o.getLong("creado")
                )
            }.getOrNull()
        }
    }
}
