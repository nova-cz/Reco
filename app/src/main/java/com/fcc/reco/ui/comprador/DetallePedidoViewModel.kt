package com.fcc.reco.ui.comprador

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fcc.reco.data.Repositorios
import com.fcc.reco.data.model.Calificacion
import com.fcc.reco.data.model.Pedido
import com.fcc.reco.data.model.Producto
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class EstadoDetallePedido(val pedido: Pedido, val productos: List<Producto>)

class DetallePedidoViewModel(handle: SavedStateHandle) : ViewModel() {

    private val pedidoId = handle.get<Int>("pedidoId") ?: -1

    private fun cargar(pedidos: List<Pedido>): EstadoDetallePedido? =
        pedidos.firstOrNull { it.id == pedidoId }?.let { p ->
            EstadoDetallePedido(p, p.productoIds.mapNotNull { Repositorios.productos.obtenerPorId(it) })
        }

    // Valor inicial ya cargado: la hoja de calificación puede abrirse antes de que alguien recolecte el flujo.
    val estado: StateFlow<EstadoDetallePedido?> = combine(
        Repositorios.pedidos.pedidos,
        Repositorios.calificaciones.calificaciones
    ) { pedidos, _ -> cargar(pedidos) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), cargar(Repositorios.pedidos.pedidos.value))

    /** Libera el pago al vendedor. Devuelve verdadero si el pedido estaba listo para confirmarse. */
    fun confirmarEntrega(): Boolean = Repositorios.pedidos.confirmarEntrega(pedidoId)

    /** Solo para pruebas: hasta el flujo de Ventas, el vendedor no tiene cómo marcar la entrega. */
    fun simularEntregaDelVendedor() = Repositorios.pedidos.marcarEntregado(pedidoId)

    fun calificar(estrellas: Int, etiquetas: List<String>, comentario: String): Calificacion? =
        Repositorios.calificaciones.calificarVendedor(pedidoId, estrellas, etiquetas, comentario)
}
