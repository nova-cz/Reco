package com.fcc.reco.data.repository

import com.fcc.reco.data.DatosSimulados
import com.fcc.reco.data.model.EstadoAnuncio
import com.fcc.reco.data.model.FiltrosProducto
import com.fcc.reco.data.model.Grado
import com.fcc.reco.data.model.Orden
import com.fcc.reco.data.model.Producto

interface ProductoRepository {
    fun obtenerPublicados(filtros: FiltrosProducto = FiltrosProducto()): List<Producto>
    fun obtenerPorId(id: Int): Producto?
    fun marcas(): List<String>
    fun destacado(): Producto?
    fun obtenerPorVendedor(nombre: String): List<Producto>
    /** Verdadero si el equipo ya se pagó y salió del catálogo. */
    fun estaVendido(id: Int): Boolean
}

/** Catálogo en memoria. La versión real consulta Supabase (solo anuncios Publicados). */
class ProductoRepositorySimulado(
    private val idsVendidos: () -> Set<Int> = { emptySet() },
    private val anuncios: AnuncioRepository? = null,
    private val correoActual: () -> String? = { null }
) : ProductoRepository {

    private fun deAnuncios(soloPublicados: Boolean): List<Producto> =
        anuncios?.anuncios?.value.orEmpty()
            .filter { !soloPublicados || it.estado == EstadoAnuncio.PUBLICADO }
            .map { it.comoProducto(esPropio = it.vendedorCorreo == correoActual()) }

    /** Todo lo que existe (para favoritos y pedidos), incluso lo ya vendido. */
    private val todos: List<Producto> get() = DatosSimulados.productos + deAnuncios(soloPublicados = false)

    /** Cada equipo es pieza única: sale del catálogo cuando se paga. Solo anuncios Publicados. */
    private val catalogo: List<Producto>
        get() = idsVendidos().let { vendidos -> (DatosSimulados.productos + deAnuncios(soloPublicados = true)).filter { it.id !in vendidos } }

    override fun obtenerPublicados(filtros: FiltrosProducto): List<Producto> {
        val consulta = filtros.consulta.trim().lowercase()
        val filtrados = catalogo.filter { p ->
            (filtros.categoria == null || p.categoria == filtros.categoria) &&
                (filtros.marca == null || p.marca == filtros.marca) &&
                (consulta.isEmpty() || "${p.marca} ${p.modelo} ${p.categoria.name}".lowercase().contains(consulta)) &&
                (!filtros.soloGradosAB || p.grado != Grado.C) &&
                (filtros.precioMax == null || p.precio <= filtros.precioMax) &&
                (!filtros.soloCertificados || p.certificado)
        }
        return when (filtros.orden) {
            // Los certificados tienen prioridad en las búsquedas.
            Orden.MAS_RECIENTES -> filtrados.sortedWith(compareBy({ !it.certificado }, { it.diasPublicado }))
            Orden.PRECIO_MENOR -> filtrados.sortedBy { it.precio }
            Orden.PRECIO_MAYOR -> filtrados.sortedByDescending { it.precio }
        }
    }

    override fun obtenerPorId(id: Int): Producto? = todos.firstOrNull { it.id == id }

    override fun marcas(): List<String> =
        catalogo.groupingBy { it.marca }.eachCount().entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { it.key }

    override fun estaVendido(id: Int): Boolean = id in idsVendidos()

    override fun obtenerPorVendedor(nombre: String): List<Producto> = catalogo.filter { it.vendedor.nombre == nombre }

    override fun destacado(): Producto? = catalogo.firstOrNull { it.certificado && it.categoria.name == "LAPTOPS" }
}
