package com.fcc.reco.data.model

enum class EstadoPedido(val etiqueta: String) {
    PAGADO_RETENIDO("Pago retenido"),
    ENTREGADO("Entregado"),
    LIBERADO("Completado"),
    EN_RECLAMO("En reclamo"),
    CANCELADO("Cancelado")
}

/** Un pedido por vendedor; puede incluir varios equipos. El dinero queda retenido hasta confirmar la entrega. */
data class Pedido(
    val id: Int,
    val productoIds: List<Int>,
    val vendedor: VendedorResumen,
    val monto: Int,
    val comision: Int,
    val puntoEntrega: String,
    val horario: String,
    val estado: EstadoPedido,
    val creado: Long,
    val stripePaymentId: String,
    val estrellas: Int? = null
) {
    val gananciaVendedor: Int get() = monto - comision
}

/** Equipos del carrito de un mismo vendedor. */
data class GrupoCarrito(val vendedor: VendedorResumen, val productos: List<Producto>) {
    val subtotal: Int get() = productos.sumOf { it.precio }
}

data class PuntoEntrega(val nombre: String, val descripcion: String)

/** Lo que el comprador eligió para entregar un grupo del carrito. */
data class EntregaElegida(val grupo: GrupoCarrito, val punto: PuntoEntrega, val horario: String)
