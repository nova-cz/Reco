package com.fcc.reco.domain

import com.fcc.reco.data.model.EstadoPedido
import com.fcc.reco.data.model.Pedido

/** Cifras del vendedor: lo retenido, lo ya liberado y las comisiones que pagó (13.6 % + $8 por pedido). */
data class ResumenVentas(val porCobrar: Int, val disponible: Int, val comisiones: Int) {
    val total: Int get() = porCobrar + disponible

    companion object {
        fun de(ventas: List<Pedido>) = ResumenVentas(
            porCobrar = ventas.filter { it.estado == EstadoPedido.PAGADO_RETENIDO || it.estado == EstadoPedido.ENTREGADO }.sumOf { it.gananciaVendedor },
            disponible = ventas.filter { it.estado == EstadoPedido.LIBERADO }.sumOf { it.gananciaVendedor },
            comisiones = ventas.filter { it.estado == EstadoPedido.LIBERADO }.sumOf { it.comision }
        )
    }
}
