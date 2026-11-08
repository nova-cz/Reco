package com.fcc.reco.data.repository

import com.fcc.reco.data.local.AlmacenJson
import com.fcc.reco.data.model.EntregaElegida
import com.fcc.reco.data.model.EstadoPedido
import com.fcc.reco.data.model.Pedido
import com.fcc.reco.data.model.VendedorResumen
import com.fcc.reco.domain.Comision
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

interface PedidoRepository {
    val pedidos: StateFlow<List<Pedido>>
    fun crearPedidos(entregas: List<EntregaElegida>, stripePaymentId: String): List<Pedido>
    fun obtener(id: Int): Pedido?
    /** El vendedor marca que entregó el equipo (Pago retenido -> Listo para entregar). */
    fun marcarEntregado(id: Int): Boolean
    /** El comprador revisó el equipo y confirma: se libera el pago al vendedor. */
    fun confirmarEntrega(id: Int): Boolean
    fun registrarEstrellas(id: Int, estrellas: Int)
}

/** Pedidos en memoria, guardados en un JSON interno. El dinero queda retenido hasta confirmar la entrega. */
class PedidoRepositorySimulado(private val almacen: AlmacenJson) : PedidoRepository {

    private val _pedidos = MutableStateFlow(leer())
    override val pedidos: StateFlow<List<Pedido>> = _pedidos.asStateFlow()

    override fun crearPedidos(entregas: List<EntregaElegida>, stripePaymentId: String): List<Pedido> {
        var siguienteId = (_pedidos.value.maxOfOrNull { it.id } ?: 0) + 1
        val nuevos = entregas.map { e ->
            val monto = e.grupo.subtotal
            Pedido(
                id = siguienteId++,
                productoIds = e.grupo.productos.map { it.id },
                vendedor = e.grupo.vendedor,
                monto = monto,
                comision = Comision.calcular(monto),
                puntoEntrega = e.punto.nombre,
                horario = e.horario,
                estado = EstadoPedido.PAGADO_RETENIDO,
                creado = System.currentTimeMillis(),
                stripePaymentId = stripePaymentId
            )
        }
        _pedidos.value = _pedidos.value + nuevos
        guardar()
        return nuevos
    }

    override fun obtener(id: Int): Pedido? = _pedidos.value.firstOrNull { it.id == id }

    override fun marcarEntregado(id: Int): Boolean =
        cambiar(id, desde = EstadoPedido.PAGADO_RETENIDO) { it.copy(estado = EstadoPedido.ENTREGADO) }

    override fun confirmarEntrega(id: Int): Boolean =
        cambiar(id, desde = EstadoPedido.ENTREGADO) { it.copy(estado = EstadoPedido.LIBERADO) }

    override fun registrarEstrellas(id: Int, estrellas: Int) {
        cambiar(id, desde = null) { it.copy(estrellas = estrellas) }
    }

    /** Aplica el cambio solo si el pedido existe y está en el estado esperado. */
    private fun cambiar(id: Int, desde: EstadoPedido?, transformar: (Pedido) -> Pedido): Boolean {
        val actual = obtener(id) ?: return false
        if (desde != null && actual.estado != desde) return false
        _pedidos.value = _pedidos.value.map { if (it.id == id) transformar(it) else it }
        guardar()
        return true
    }

    private fun guardar() {
        val json = JSONArray()
        _pedidos.value.forEach { p ->
            json.put(JSONObject().apply {
                put("id", p.id)
                put("productoIds", JSONArray(p.productoIds))
                put("vendedor", JSONObject().apply {
                    put("nombre", p.vendedor.nombre); put("carrera", p.vendedor.carrera)
                    put("facultad", p.vendedor.facultad); put("reputacion", p.vendedor.reputacion)
                    put("ventas", p.vendedor.ventas)
                })
                put("monto", p.monto); put("comision", p.comision)
                put("punto", p.puntoEntrega); put("horario", p.horario)
                put("estado", p.estado.name); put("creado", p.creado)
                put("stripe", p.stripePaymentId); put("estrellas", p.estrellas ?: -1)
            })
        }
        almacen.guardar(json)
    }

    private fun leer(): List<Pedido> {
        val json = almacen.leer()
        return (0 until json.length()).mapNotNull { i ->
            runCatching {
                val o = json.getJSONObject(i)
                val v = o.getJSONObject("vendedor")
                val ids = o.getJSONArray("productoIds")
                Pedido(
                    id = o.getInt("id"),
                    productoIds = (0 until ids.length()).map { ids.getInt(it) },
                    vendedor = VendedorResumen(
                        v.getString("nombre"), v.getString("carrera"), v.getString("facultad"),
                        v.getDouble("reputacion"), v.getInt("ventas")
                    ),
                    monto = o.getInt("monto"), comision = o.getInt("comision"),
                    puntoEntrega = o.getString("punto"), horario = o.getString("horario"),
                    estado = EstadoPedido.valueOf(o.getString("estado")),
                    creado = o.getLong("creado"), stripePaymentId = o.getString("stripe"),
                    estrellas = o.getInt("estrellas").takeIf { it >= 0 }
                )
            }.getOrNull()
        }
    }
}
