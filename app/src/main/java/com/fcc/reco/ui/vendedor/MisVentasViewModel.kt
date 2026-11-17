package com.fcc.reco.ui.vendedor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fcc.reco.data.Repositorios
import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.data.model.Anuncio
import com.fcc.reco.data.model.Calificacion
import com.fcc.reco.data.model.EntregaElegida
import com.fcc.reco.data.model.EstadoAnuncio
import com.fcc.reco.data.model.EstadoPedido
import com.fcc.reco.data.model.GrupoPedido
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

/** Cuántos anuncios se ven en Mis ventas antes de tocar "Ver todo". */
private const val ANUNCIOS_VISIBLES = 4

/** Anuncios del usuario actual con su estado efectivo, siempre al día. */
private fun misAnuncios(correo: String): List<AnuncioConEstado> =
    Repositorios.anuncios.misAnuncios(correo).map { AnuncioConEstado(it, Repositorios.anuncios.estadoEfectivo(it)) }

/** Ventas = pedidos de otros estudiantes sobre equipos de este vendedor. */
private fun ventasDe(pedidos: List<Pedido>) = pedidos.filter { it.comprador.isNotEmpty() }

data class EstadoMisVentas(
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
    val anuncios: List<AnuncioConEstado> = emptyList(),
    val hayMas: Boolean = false,
    val verTodo: Boolean = false
)

/** Mis ventas: ganancias, contadores, próxima entrega y Mis anuncios con sus acciones. */
class MisVentasViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val sesion = SesionLocal(aplicacion)
    private val verTodo = MutableStateFlow(false)

    val estado: StateFlow<EstadoMisVentas> = combine(
        Repositorios.anuncios.anuncios, Repositorios.pedidos.pedidos, Repositorios.calificaciones.calificaciones, verTodo
    ) { _, pedidos, _, todo ->
        val s = sesion.leer()
        val correo = s?.correo.orEmpty()
        val todos = misAnuncios(correo)
        val ventas = ventasDe(pedidos)
        val ahora = Calendar.getInstance()
        val delMes = ventas.filter {
            Calendar.getInstance().apply { timeInMillis = it.creado }.let { c -> c.get(Calendar.MONTH) == ahora.get(Calendar.MONTH) && c.get(Calendar.YEAR) == ahora.get(Calendar.YEAR) }
        }
        val resumen = ResumenVentas.de(delMes)
        val proxima = ventas.filter { it.estado == EstadoPedido.PAGADO_RETENIDO || it.estado == EstadoPedido.ENTREGADO }.minByOrNull { it.creado }
        // Al ver todo se incluyen los borradores, para poder continuarlos.
        val lista = if (todo) todos else todos.filter { it.estado != EstadoAnuncio.BORRADOR }.take(ANUNCIOS_VISIBLES)
        EstadoMisVentas(
            nombre = s?.nombre.orEmpty(),
            reputacion = s?.nombreCorto?.let { Repositorios.calificaciones.obtenerReputacion(it)?.promedio },
            mes = SimpleDateFormat("MMMM", Locale.forLanguageTag("es-MX")).format(Date()),
            ganancias = resumen.total, disponible = resumen.disponible, retenido = resumen.porCobrar,
            ventasMes = delMes.size,
            activos = todos.count { it.estado == EstadoAnuncio.PUBLICADO || it.estado == EstadoAnuncio.RESERVADO },
            enRevision = todos.count { it.estado == EstadoAnuncio.EN_REVISION },
            vendidos = todos.count { it.estado == EstadoAnuncio.VENDIDO },
            proxima = proxima,
            proximaTitulo = proxima?.productoIds?.firstOrNull()?.let { Repositorios.productos.obtenerPorId(it)?.titulo }.orEmpty(),
            anuncios = lista,
            hayMas = todos.size > ANUNCIOS_VISIBLES,
            verTodo = todo
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoMisVentas())

    fun alternarVerTodo() { verTodo.value = !verTodo.value }

    // Acciones sobre un anuncio
    fun pausar(id: Int) = Repositorios.anuncios.pausar(id)
    fun reanudar(id: Int) = Repositorios.anuncios.reanudar(id)
    fun eliminar(id: Int) = Repositorios.anuncios.eliminar(id)

    // Entregas
    fun marcarEntregado(id: Int) = Repositorios.pedidos.marcarEntregado(id)

    // Solo para probar (botones de debug): en Supabase la moderación se hace desde el panel y el comprador es real.
    fun aprobar(id: Int) = Repositorios.anuncios.aprobar(id)
    fun rechazar(id: Int, motivo: String) = Repositorios.anuncios.rechazar(id, motivo)

    fun simularCompra(a: Anuncio) {
        val producto = a.comoProducto(esPropio = false)
        val comprador = COMPRADORES[Repositorios.pedidos.pedidos.value.size % COMPRADORES.size]
        Repositorios.pedidos.crearPedidos(
            listOf(EntregaElegida(GrupoPedido(a.vendedor, listOf(producto)), PuntoEntrega(a.puntoEntrega.substringBefore(" ·"), ""), "Mar 13:00")),
            "pi_simulado", comprador
        )
    }

    /** El comprador es simulado, así que aquí se confirma por él (y deja una reseña de 5 estrellas). */
    fun simularConfirmacionDelComprador(id: Int) {
        if (Repositorios.pedidos.confirmarEntrega(id)) {
            Repositorios.calificaciones.calificarVendedor(
                id, 5, listOf(Calificacion.PUNTUAL, "Como se describió"),
                "Todo perfecto, llegó a la hora y el equipo estaba impecable."
            )
        }
    }
}
