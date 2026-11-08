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
    val estrellas: Int? = null,
    /** Vacío si lo compró este usuario; con nombre si es una venta de uno de sus anuncios. */
    val comprador: String = ""
) {
    val gananciaVendedor: Int get() = monto - comision

    /** Código visible del pedido, p. ej. "RC-1042". */
    val codigo: String get() = "RC-${1000 + id}"

    /** Código de 4 dígitos que el comprador muestra al vendedor en la entrega (simulado, estable por pedido). */
    val codigoEntrega: String get() = (1000 + (id * 7919 + 1234) % 9000).toString()
}

/** Reseña que el comprador deja al vendedor al confirmar la entrega. */
data class Calificacion(
    val id: Int,
    val pedidoId: Int,
    val vendedor: String,
    val estrellas: Int,
    val etiquetas: List<String>,
    val comentario: String,
    val creado: Long
) {
    companion object {
        const val PUNTUAL = "Puntual"
        val ETIQUETAS = listOf(PUNTUAL, "Como se describió", "Amable", "Buen precio")
    }
}

data class Reputacion(val promedio: Double, val total: Int)

/** Equipos del carrito de un mismo vendedor. */
data class GrupoCarrito(val vendedor: VendedorResumen, val productos: List<Producto>) {
    val subtotal: Int get() = productos.sumOf { it.precio }
}

data class PuntoEntrega(val nombre: String, val descripcion: String)

/** Lo que el comprador eligió para entregar un grupo del carrito. */
data class EntregaElegida(val grupo: GrupoCarrito, val punto: PuntoEntrega, val horario: String)
