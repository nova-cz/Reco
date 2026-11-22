package com.fcc.reco.data.repository

import com.fcc.reco.data.model.EntregaElegida
import com.fcc.reco.data.model.EstadoPedido
import com.fcc.reco.data.model.Pedido
import com.fcc.reco.data.remote.EstrellasDto
import com.fcc.reco.data.remote.IdMapa
import com.fcc.reco.data.remote.Nube
import com.fcc.reco.data.remote.NubeDatos
import com.fcc.reco.data.remote.PedidoDto
import com.fcc.reco.data.remote.epochDe
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Pedidos reales. Se crean con la función `crear_pedido` (compra directa de un equipo) y cambian de estado con
 * `marcar_entregado` y `confirmar_entrega`; el monto y la comisión los calcula el servidor.
 */
class PedidoRepositorySupabase(
    private val datos: NubeDatos,
    private val productos: ProductoRepositorySupabase
) : PedidoRepository {

    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    override val pedidos: StateFlow<List<Pedido>> = _pedidos.asStateFlow()

    suspend fun refrescar() {
        val filas = Nube.cliente.from("pedidos").select { order("creado", Order.ASCENDING) }.decodeList<PedidoDto>()
        productos.asegurar(filas.map { it.productoId })
        val estrellas = runCatching {
            Nube.cliente.from("calificaciones").select { filter { isIn("pedido_id", filas.map { it.id }) } }
                .decodeList<EstrellasDto>().associate { it.pedidoId to it.estrellas }
        }.getOrDefault(emptyMap())
        _pedidos.value = filas.map { aPedido(it, estrellas[it.id]) }
        productos.marcarVendidos(
            filas.filter { it.compradorId == datos.miUid && it.estado != "cancelado" }.map { IdMapa.id(it.productoId) }.toSet()
        )
    }

    private fun aPedido(d: PedidoDto, estrellas: Int?): Pedido {
        val soyComprador = d.compradorId == datos.miUid
        return Pedido(
            id = IdMapa.id(d.id),
            productoIds = listOf(IdMapa.id(d.productoId)),
            vendedor = datos.resumen(d.vendedorId),
            monto = d.monto, comision = d.comision,
            puntoEntrega = datos.punto(d.puntoId)?.nombre ?: "Ciudad Universitaria",
            horario = d.horario.orEmpty(),
            estado = runCatching { EstadoPedido.valueOf(d.estado.uppercase()) }.getOrDefault(EstadoPedido.PAGADO_RETENIDO),
            creado = epochDe(d.creado),
            stripePaymentId = "",
            estrellas = estrellas,
            comprador = if (soyComprador) "" else datos.nombreCorto(d.compradorId)
        )
    }

    /** Compra directa: un pedido por equipo. Lanza una excepción si el servidor lo rechaza (ya vendido, sin verificar...). */
    override suspend fun crearPedidos(entregas: List<EntregaElegida>, stripePaymentId: String, comprador: String): List<Pedido> {
        val creados = mutableListOf<String>()
        entregas.forEach { e ->
            val producto = e.grupo.productos.first()
            val uuid = IdMapa.uuid(producto.id) ?: error("Equipo desconocido")
            val punto = datos.puntoPorNombre(e.punto.nombre)?.id
            val id = Nube.cliente.postgrest.rpc(
                "crear_pedido",
                buildJsonObject {
                    put("p_producto", uuid)
                    put("p_punto", punto?.let { JsonPrimitive(it) } ?: JsonNull)
                    put("p_horario", e.horario)
                }
            ).decodeAs<String>()
            creados += id
        }
        productos.refrescar()
        refrescar()
        return creados.mapNotNull { id -> obtener(IdMapa.id(id)) }
    }

    override fun obtener(id: Int): Pedido? = _pedidos.value.firstOrNull { it.id == id }

    override fun marcarEntregado(id: Int): Boolean = cambiar(id, EstadoPedido.PAGADO_RETENIDO, EstadoPedido.ENTREGADO, "marcar_entregado")

    override fun confirmarEntrega(id: Int): Boolean = cambiar(id, EstadoPedido.ENTREGADO, EstadoPedido.LIBERADO, "confirmar_entrega")

    override fun registrarEstrellas(id: Int, estrellas: Int) {
        _pedidos.update { lista -> lista.map { if (it.id == id) it.copy(estrellas = estrellas) else it } }
    }

    /** Cambia el estado en pantalla al instante y avisa al servidor; si este lo rechaza, se vuelve a cargar. */
    private fun cambiar(id: Int, desde: EstadoPedido, hasta: EstadoPedido, funcion: String): Boolean {
        val actual = obtener(id) ?: return false
        if (actual.estado != desde) return false
        val uuid = IdMapa.uuid(id) ?: return false
        _pedidos.update { lista -> lista.map { if (it.id == id) it.copy(estado = hasta) else it } }
        Nube.alcance.launch {
            runCatching { Nube.cliente.postgrest.rpc(funcion, buildJsonObject { put("p_pedido", uuid) }) }
                .onFailure { runCatching { refrescar() } }
        }
        return true
    }
}
