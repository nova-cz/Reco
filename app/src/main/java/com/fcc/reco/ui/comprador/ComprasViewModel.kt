package com.fcc.reco.ui.comprador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fcc.reco.data.Repositorios
import com.fcc.reco.data.model.EstadoPedido
import com.fcc.reco.data.model.Pedido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

enum class FiltroCompras { EN_CURSO, COMPLETADAS }

data class EstadoCompras(
    val filtro: FiltroCompras = FiltroCompras.EN_CURSO,
    val totalEnCurso: Int = 0,
    val totalPedidos: Int = 0,
    val pedidos: List<Pedido> = emptyList()
)

private val ESTADOS_EN_CURSO = setOf(EstadoPedido.PAGADO_RETENIDO, EstadoPedido.ENTREGADO)

class ComprasViewModel : ViewModel() {

    private val filtro = MutableStateFlow(FiltroCompras.EN_CURSO)

    val estado: StateFlow<EstadoCompras> = combine(
        // Mis compras no incluye las ventas de mis anuncios (esas tienen comprador).
        Repositorios.pedidos.pedidos.map { lista -> lista.filter { it.comprador.isEmpty() } },
        Repositorios.calificaciones.calificaciones,
        filtro
    ) { pedidos, _, filtroActual ->
        val enCurso = pedidos.filter { it.estado in ESTADOS_EN_CURSO }
        EstadoCompras(
            filtro = filtroActual,
            totalEnCurso = enCurso.size,
            totalPedidos = pedidos.size,
            pedidos = when (filtroActual) {
                FiltroCompras.EN_CURSO -> enCurso
                FiltroCompras.COMPLETADAS -> pedidos.filter { it.estado !in ESTADOS_EN_CURSO }
            }.sortedByDescending { it.creado }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoCompras())

    fun ponerFiltro(nuevo: FiltroCompras) {
        filtro.value = nuevo
    }

    fun confirmarEntrega(pedidoId: Int): Boolean = Repositorios.pedidos.confirmarEntrega(pedidoId)
}
