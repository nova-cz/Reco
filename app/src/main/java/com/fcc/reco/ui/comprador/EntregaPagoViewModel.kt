package com.fcc.reco.ui.comprador

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fcc.reco.R
import com.fcc.reco.data.DatosSimulados
import com.fcc.reco.data.Repositorios
import com.fcc.reco.data.model.EntregaElegida
import com.fcc.reco.data.model.GrupoCarrito
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Índices del punto y del horario elegidos para un vendedor. */
data class Seleccion(val punto: Int = 0, val horario: Int = 0)

data class EstadoPago(
    val grupos: List<GrupoCarrito>,
    val selecciones: Map<String, Seleccion>,
    val tarjetaUltimos4: String = "4242",
    val cargando: Boolean = false,
    @StringRes val error: Int? = null
) {
    val total: Int get() = grupos.sumOf { it.subtotal }
}

sealed interface EventoPago {
    data class PagoExitoso(val pedidoIds: IntArray) : EventoPago
}

class EntregaPagoViewModel : ViewModel() {

    private val grupos = Repositorios.carrito.listarAgrupadoPorVendedor()

    private val _estado = MutableStateFlow(
        EstadoPago(grupos, grupos.associate { it.vendedor.nombre to Seleccion() })
    )
    val estado: StateFlow<EstadoPago> = _estado.asStateFlow()

    private val _eventos = Channel<EventoPago>(Channel.BUFFERED)
    val eventos = _eventos.receiveAsFlow()

    fun elegirPunto(vendedor: String, indice: Int) = cambiar(vendedor) { it.copy(punto = indice) }

    fun elegirHorario(vendedor: String, indice: Int) = cambiar(vendedor) { it.copy(horario = indice) }

    fun cambiarTarjeta(ultimos4: String) = _estado.update { it.copy(tarjetaUltimos4 = ultimos4) }

    private fun cambiar(vendedor: String, cambio: (Seleccion) -> Seleccion) {
        _estado.update { e ->
            e.copy(selecciones = e.selecciones + (vendedor to cambio(e.selecciones[vendedor] ?: Seleccion())))
        }
    }

    /** Cobra (simulado), crea un pedido por vendedor, vacía el carrito y avisa a la pantalla. */
    fun pagar() {
        val actual = _estado.value
        if (actual.cargando || actual.grupos.isEmpty()) return
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            Repositorios.pagos.procesarPago(actual.total, actual.tarjetaUltimos4).fold(
                onSuccess = { idPago ->
                    val entregas = actual.grupos.map { g ->
                        val s = actual.selecciones[g.vendedor.nombre] ?: Seleccion()
                        EntregaElegida(g, DatosSimulados.puntosEntrega[s.punto], DatosSimulados.horariosEntrega[s.horario])
                    }
                    val pedidos = Repositorios.pedidos.crearPedidos(entregas, idPago)
                    actual.grupos.flatMap { it.productos }.forEach { Repositorios.carrito.quitar(it.id) }
                    _eventos.send(EventoPago.PagoExitoso(pedidos.map { it.id }.toIntArray()))
                },
                onFailure = { _estado.update { e -> e.copy(cargando = false, error = R.string.error_pago) } }
            )
        }
    }
}
