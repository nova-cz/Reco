package com.fcc.reco.ui.vendedor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fcc.reco.data.Repositorios
import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.data.model.Anuncio
import com.fcc.reco.data.model.EntregaElegida
import com.fcc.reco.data.model.EstadoAnuncio
import com.fcc.reco.data.model.EstadoPedido
import com.fcc.reco.data.model.GrupoCarrito
import com.fcc.reco.data.model.Pedido
import com.fcc.reco.data.model.PuntoEntrega
import com.fcc.reco.domain.ResumenVentas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Un anuncio con el estado que ve el vendedor (Vendido en cuanto el equipo se paga). */
data class AnuncioConEstado(val anuncio: Anuncio, val estado: EstadoAnuncio)

private val COMPRADORES = listOf("Carlos M.", "Mariana V.", "José L.", "Daniela R.")

/** Anuncios del usuario actual con su estado efectivo, siempre al día. */
private fun misAnuncios(correo: String): List<AnuncioConEstado> =
    Repositorios.anuncios.misAnuncios(correo).map { AnuncioConEstado(it, Repositorios.anuncios.estadoEfectivo(it)) }

/** Ventas = pedidos de otros estudiantes sobre equipos de este vendedor. */
private fun ventasDe(pedidos: List<Pedido>) = pedidos.filter { it.comprador.isNotEmpty() }

// ---------------------------------------------------------------- Panel

data class EstadoPanel(
    val nombre: String = "",
    val reputacion: Double? = null,
    val mes: String = "",
    val ganancias: Int = 0,
    val disponible: Int = 0,
    val retenido: Int = 0,
    val ventasMes: Int = 0,
    val activos: Int = 0,
    val enRevision: Int = 0,
    val vendidos: Int = 0,
    val proxima: Pedido? = null,
    val proximaTitulo: String = "",
    val recientes: List<AnuncioConEstado> = emptyList()
)

class PanelViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val sesion = SesionLocal(aplicacion)

    val estado: StateFlow<EstadoPanel> = combine(
        Repositorios.anuncios.anuncios, Repositorios.pedidos.pedidos, Repositorios.calificaciones.calificaciones
    ) { _, pedidos, _ ->
        val s = sesion.leer()
        val correo = s?.correo.orEmpty()
        val anuncios = misAnuncios(correo)
        val ventas = ventasDe(pedidos)
        val ahora = Calendar.getInstance()
        val delMes = ventas.filter {
            Calendar.getInstance().apply { timeInMillis = it.creado }.let { c -> c.get(Calendar.MONTH) == ahora.get(Calendar.MONTH) && c.get(Calendar.YEAR) == ahora.get(Calendar.YEAR) }
        }
        val resumen = ResumenVentas.de(delMes)
        val proxima = ventas.filter { it.estado == EstadoPedido.PAGADO_RETENIDO || it.estado == EstadoPedido.ENTREGADO }.minByOrNull { it.creado }
        EstadoPanel(
            nombre = s?.nombre.orEmpty(),
            reputacion = s?.nombre?.let { Repositorios.calificaciones.obtenerReputacion(it)?.promedio },
            mes = SimpleDateFormat("MMMM", Locale.forLanguageTag("es-MX")).format(Date()),
            ganancias = resumen.total, disponible = resumen.disponible, retenido = resumen.porCobrar,
            ventasMes = delMes.size,
            activos = anuncios.count { it.estado == EstadoAnuncio.PUBLICADO || it.estado == EstadoAnuncio.RESERVADO },
            enRevision = anuncios.count { it.estado == EstadoAnuncio.EN_REVISION },
            vendidos = anuncios.count { it.estado == EstadoAnuncio.VENDIDO },
            proxima = proxima,
            proximaTitulo = proxima?.productoIds?.firstOrNull()?.let { Repositorios.productos.obtenerPorId(it)?.titulo }.orEmpty(),
            recientes = anuncios.filter { it.estado != EstadoAnuncio.BORRADOR }.take(4)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoPanel())
}

// ---------------------------------------------------------------- Mis anuncios

enum class FiltroAnuncios(val estados: Set<EstadoAnuncio>?) {
    TODOS(null),
    PUBLICADOS(setOf(EstadoAnuncio.PUBLICADO)),
    EN_REVISION(setOf(EstadoAnuncio.EN_REVISION)),
    RECHAZADOS(setOf(EstadoAnuncio.RECHAZADO)),
    PAUSADOS(setOf(EstadoAnuncio.PAUSADO)),
    VENDIDOS(setOf(EstadoAnuncio.VENDIDO)),
    BORRADORES(setOf(EstadoAnuncio.BORRADOR))
}

data class EstadoAnuncios(
    val filtro: FiltroAnuncios = FiltroAnuncios.TODOS,
    val total: Int = 0,
    val activos: Int = 0,
    val items: List<AnuncioConEstado> = emptyList()
)

class AnunciosViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val sesion = SesionLocal(aplicacion)
    private val filtro = MutableStateFlow(FiltroAnuncios.TODOS)

    val estado: StateFlow<EstadoAnuncios> = combine(
        Repositorios.anuncios.anuncios, Repositorios.pedidos.pedidos, filtro
    ) { _, _, f ->
        val todos = misAnuncios(sesion.leer()?.correo.orEmpty())
        EstadoAnuncios(
            filtro = f, total = todos.size, activos = todos.count { it.estado.esActivo },
            items = todos.filter { f.estados == null || it.estado in f.estados }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoAnuncios())

    fun ponerFiltro(f: FiltroAnuncios) { filtro.value = f }
    fun pausar(id: Int) = Repositorios.anuncios.pausar(id)
    fun reanudar(id: Int) = Repositorios.anuncios.reanudar(id)
    fun eliminar(id: Int) = Repositorios.anuncios.eliminar(id)

    // Moderación y compra de otro estudiante: solo para probar (en Supabase se hace desde el panel).
    fun aprobar(id: Int) = Repositorios.anuncios.aprobar(id)
    fun rechazar(id: Int, motivo: String) = Repositorios.anuncios.rechazar(id, motivo)

    fun simularCompra(a: Anuncio) {
        val producto = a.comoProducto(esPropio = false)
        val comprador = COMPRADORES[Repositorios.pedidos.pedidos.value.size % COMPRADORES.size]
        Repositorios.pedidos.crearPedidos(
            listOf(EntregaElegida(GrupoCarrito(a.vendedor, listOf(producto)), PuntoEntrega(a.puntoEntrega.substringBefore(" ·"), ""), "Mar 13:00")),
            "pi_simulado", comprador
        )
    }
}

// ---------------------------------------------------------------- Ventas

enum class FiltroVentas { POR_ENTREGAR, ENTREGADAS, COMPLETADAS }

data class EstadoVentas(
    val filtro: FiltroVentas = FiltroVentas.POR_ENTREGAR,
    val resumen: ResumenVentas = ResumenVentas(0, 0, 0),
    val porEntregar: Int = 0,
    val totalVentas: Int = 0,
    val ventas: List<Pedido> = emptyList()
)

class VentasViewModel : androidx.lifecycle.ViewModel() {
    private val filtro = MutableStateFlow(FiltroVentas.POR_ENTREGAR)

    val estado: StateFlow<EstadoVentas> = combine(Repositorios.pedidos.pedidos, filtro) { pedidos, f ->
        val ventas = ventasDe(pedidos)
        EstadoVentas(
            filtro = f, resumen = ResumenVentas.de(ventas),
            porEntregar = ventas.count { it.estado == EstadoPedido.PAGADO_RETENIDO },
            totalVentas = ventas.size,
            ventas = ventas.filter {
                when (f) {
                    FiltroVentas.POR_ENTREGAR -> it.estado == EstadoPedido.PAGADO_RETENIDO
                    FiltroVentas.ENTREGADAS -> it.estado == EstadoPedido.ENTREGADO
                    FiltroVentas.COMPLETADAS -> it.estado == EstadoPedido.LIBERADO || it.estado == EstadoPedido.CANCELADO
                }
            }.sortedByDescending { it.creado }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoVentas())

    fun ponerFiltro(f: FiltroVentas) { filtro.value = f }
    fun marcarEntregado(id: Int) = Repositorios.pedidos.marcarEntregado(id)
    /** Solo para probar: el comprador es simulado, así que aquí se confirma por él. */
    fun simularConfirmacionDelComprador(id: Int) = Repositorios.pedidos.confirmarEntrega(id)
}
