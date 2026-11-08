package com.fcc.reco.domain

import com.fcc.reco.data.model.EstadoPedido
import com.fcc.reco.data.model.Pedido
import com.fcc.reco.data.model.VendedorResumen
import org.junit.Assert.assertEquals
import org.junit.Test

class ResumenVentasTest {
    private val vendedor = VendedorResumen("Miguel C.", "Computación", "BUAP", 5.0, 0)

    private fun venta(id: Int, monto: Int, estado: EstadoPedido) = Pedido(
        id, listOf(id), vendedor, monto, Comision.calcular(monto), "Biblioteca Central", "Mar 13:00", estado, 0L, "pi", comprador = "Carlos M."
    )

    @Test fun separaRetenidoYLiberado() {
        val r = ResumenVentas.de(listOf(
            venta(1, 5000, EstadoPedido.LIBERADO),
            venta(2, 1000, EstadoPedido.PAGADO_RETENIDO),
            venta(3, 2000, EstadoPedido.ENTREGADO)
        ))
        assertEquals(4312, r.disponible)                       // $5,000 - $688
        assertEquals(1000 - 144 + 2000 - 280, r.porCobrar)     // 136 + 8 = 144 y 272 + 8 = 280
        assertEquals(688, r.comisiones)
    }
}
