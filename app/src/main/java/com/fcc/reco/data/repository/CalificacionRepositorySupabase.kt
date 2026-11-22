package com.fcc.reco.data.repository

import com.fcc.reco.data.model.Calificacion
import com.fcc.reco.data.model.EstadoPedido
import com.fcc.reco.data.model.Reputacion
import com.fcc.reco.data.model.Resena
import com.fcc.reco.data.model.VendedorResumen
import com.fcc.reco.data.remote.IdMapa
import com.fcc.reco.data.remote.Nube
import com.fcc.reco.data.remote.NubeDatos
import com.fcc.reco.data.remote.ResenaDto
import com.fcc.reco.data.remote.epochDe
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Reseñas reales: se leen de la vista `resenas_publicas` y se escriben en `calificaciones` (solo el comprador, una vez). */
class CalificacionRepositorySupabase(
    private val datos: NubeDatos,
    private val pedidos: PedidoRepository
) : CalificacionRepository {

    private val _calificaciones = MutableStateFlow<List<Calificacion>>(emptyList())
    override val calificaciones: StateFlow<List<Calificacion>> = _calificaciones.asStateFlow()

    suspend fun cargar() {
        _calificaciones.value = Nube.cliente.from("resenas_publicas").select().decodeList<ResenaDto>().map(::aCalificacion)
    }

    private fun aCalificacion(d: ResenaDto) = Calificacion(
        id = IdMapa.id(d.id), pedidoId = IdMapa.id(d.pedidoId), vendedor = datos.nombreCorto(d.vendedorId),
        estrellas = d.estrellas, etiquetas = d.etiquetas, comentario = d.comentario.orEmpty(), creado = epochDe(d.creado),
        autor = if (d.compradorId == datos.miUid) "Tú" else datos.nombreCorto(d.compradorId), producto = d.producto
    )

    override fun calificarVendedor(pedidoId: Int, estrellas: Int, etiquetas: List<String>, comentario: String): Calificacion? {
        val pedido = pedidos.obtener(pedidoId) ?: return null
        if (pedido.estado != EstadoPedido.LIBERADO || estrellas !in 1..5) return null
        if (obtenerPorPedido(pedidoId) != null) return null
        if (!Calificacion.comentarioValido(estrellas, comentario)) return null
        val uuid = IdMapa.uuid(pedidoId) ?: return null
        val nueva = Calificacion(
            id = IdMapa.id("local-$uuid"), pedidoId = pedidoId, vendedor = pedido.vendedor.nombre, estrellas = estrellas,
            etiquetas = etiquetas, comentario = comentario.trim(), creado = System.currentTimeMillis(), autor = "Tú",
            producto = ""
        )
        _calificaciones.update { it + nueva }
        pedidos.registrarEstrellas(pedidoId, estrellas)
        Nube.alcance.launch {
            runCatching {
                Nube.cliente.from("calificaciones").insert(buildJsonObject {
                    put("pedido_id", uuid)
                    put("estrellas", estrellas)
                    put("etiquetas", JsonArray(etiquetas.map { JsonPrimitive(it) }))
                    put("comentario", comentario.trim())
                })
                cargar()
            }.onFailure { _calificaciones.update { l -> l.filter { it.id != nueva.id } } }
        }
        return nueva
    }

    override fun obtenerPorPedido(pedidoId: Int): Calificacion? = _calificaciones.value.firstOrNull { it.pedidoId == pedidoId }

    override fun obtenerReputacion(vendedor: String): Reputacion? {
        val propias = _calificaciones.value.filter { it.vendedor == vendedor }
        if (propias.isEmpty()) return null
        return Reputacion(propias.map { it.estrellas }.average(), propias.size)
    }

    override fun resenasDe(vendedor: VendedorResumen): List<Resena> =
        _calificaciones.value.filter { it.vendedor == vendedor.nombre }
            .map { Resena(it.autor.ifEmpty { "Tú" }, it.producto, it.estrellas, it.etiquetas, it.comentario, it.creado) }
            .sortedByDescending { it.fecha }
}
