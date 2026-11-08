package com.fcc.reco.ui.comun

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fcc.reco.data.Repositorios
import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.data.model.EstadoPedido
import com.fcc.reco.data.model.Resena
import com.fcc.reco.data.model.Sesion
import com.fcc.reco.data.model.VendedorResumen
import com.fcc.reco.ui.vendedor.AnuncioConEstado
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class EstadoPerfil(
    val sesion: Sesion? = null,
    val compras: Int = 0,
    val porEntregar: Int = 0,
    val favoritos: Int = 0,
    val resenasDadas: Int = 0,
    val ventas: Int = 0,
    val calificacion: Double? = null,
    val articulos: List<AnuncioConEstado> = emptyList(),
    val resenas: List<Resena> = emptyList()
)

/** Datos del Perfil propio: sirve tanto para el estudiante que solo compra como para el que ya vende. */
class PerfilViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val local = SesionLocal(aplicacion)

    val estado: StateFlow<EstadoPerfil> = combine(
        Repositorios.pedidos.pedidos, Repositorios.anuncios.anuncios,
        Repositorios.favoritos.ids, Repositorios.calificaciones.calificaciones
    ) { pedidos, _, favoritos, calificaciones ->
        val s = local.leer()
        val propios = pedidos.filter { it.comprador.isEmpty() }
        val ventas = pedidos.filter { it.comprador.isNotEmpty() }
        val articulos = s?.let { se ->
            Repositorios.anuncios.misAnuncios(se.correo).map { AnuncioConEstado(it, Repositorios.anuncios.estadoEfectivo(it)) }
        }.orEmpty()
        val yo = s?.let { VendedorResumen(it.nombreCorto, it.carrera.orEmpty(), it.universidad ?: "BUAP", 5.0, 0) }
        EstadoPerfil(
            sesion = s,
            compras = propios.size,
            porEntregar = propios.count { it.estado == EstadoPedido.PAGADO_RETENIDO || it.estado == EstadoPedido.ENTREGADO },
            favoritos = favoritos.size,
            resenasDadas = calificaciones.count { it.autor == "Tú" },
            ventas = ventas.count { it.estado == EstadoPedido.LIBERADO },
            calificacion = s?.let { Repositorios.calificaciones.obtenerReputacion(it.nombreCorto)?.promedio },
            articulos = articulos.filter { it.estado != com.fcc.reco.data.model.EstadoAnuncio.BORRADOR }.take(2),
            resenas = yo?.let { Repositorios.calificaciones.resenasDe(it) }.orEmpty()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoPerfil(sesion = local.leer()))
}
