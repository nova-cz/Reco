package com.fcc.reco.ui.comun

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.fcc.reco.data.Repositorios
import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.data.model.EstadoPedido
import com.fcc.reco.data.model.Producto
import com.fcc.reco.data.model.Resena
import com.fcc.reco.data.model.VendedorResumen
import com.fcc.reco.domain.InsigniaPuntual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class TabPerfilPublico { A_LA_VENTA, RESENAS }

data class EstadoPerfilPublico(
    val vendedor: VendedorResumen? = null,
    val miembroDesde: Long = 0L,
    val resenas: List<Resena> = emptyList(),
    val promedio: Double? = null,
    val puntual: Boolean = false,
    val ventas: Int = 0,
    val puntos: List<String> = emptyList(),
    val aLaVenta: List<Producto> = emptyList(),
    val favoritos: Set<Int> = emptySet(),
    val tab: TabPerfilPublico = TabPerfilPublico.A_LA_VENTA
)

/** Perfil público de un vendedor: reputación, dónde entrega, equipos a la venta y reseñas. Sin datos de contacto. */
class PerfilPublicoViewModel(aplicacion: Application, handle: SavedStateHandle) : AndroidViewModel(aplicacion) {
    private val nombre = handle.get<String>("vendedor").orEmpty()
    private val tab = MutableStateFlow(if (handle.get<Boolean>("resenas") == true) TabPerfilPublico.RESENAS else TabPerfilPublico.A_LA_VENTA)
    private val sesion = SesionLocal(aplicacion).leer()

    val estado: StateFlow<EstadoPerfilPublico> = combine(
        Repositorios.pedidos.pedidos, Repositorios.calificaciones.calificaciones,
        Repositorios.favoritos.ids, tab
    ) { pedidos, _, favoritos, tabActual ->
        val esPropio = sesion?.nombreCorto == nombre
        val vendedor = Repositorios.productos.buscarVendedor(nombre)
            ?: sesion?.takeIf { esPropio }?.let { VendedorResumen(it.nombreCorto, it.carrera.orEmpty(), it.universidad ?: "BUAP", 5.0, 0) }
        if (vendedor == null) return@combine EstadoPerfilPublico(tab = tabActual)
        val resenas = Repositorios.calificaciones.resenasDe(vendedor)
        val aLaVenta = Repositorios.productos.obtenerPorVendedor(nombre)
        val puntos = (aLaVenta.map { it.puntoEntrega } + listOfNotNull(sesion?.vendedor?.puntoEntrega.takeIf { esPropio })).distinct()
        EstadoPerfilPublico(
            vendedor = vendedor,
            miembroDesde = if (esPropio) sesion?.creado ?: 0L else miembroSimulado(nombre),
            resenas = resenas,
            promedio = resenas.takeIf { it.isNotEmpty() }?.map { it.estrellas }?.average() ?: vendedor.reputacion.takeIf { !esPropio },
            puntual = InsigniaPuntual.gana(resenas),
            ventas = if (esPropio) pedidos.count { it.comprador.isNotEmpty() && it.estado == EstadoPedido.LIBERADO } else vendedor.ventas,
            puntos = puntos, aLaVenta = aLaVenta, favoritos = favoritos, tab = tabActual
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoPerfilPublico(tab = tab.value))

    fun ponerTab(t: TabPerfilPublico) { tab.value = t }
    fun alternarFavorito(p: Producto) = Repositorios.favoritos.alternar(p.id)

    /** Fecha de alta de ejemplo para los vendedores simulados (entre 2 y 4 meses atrás, estable por nombre). */
    private fun miembroSimulado(nombre: String): Long =
        System.currentTimeMillis() - (60L + Math.floorMod(nombre.hashCode(), 60)) * 86_400_000L
}
