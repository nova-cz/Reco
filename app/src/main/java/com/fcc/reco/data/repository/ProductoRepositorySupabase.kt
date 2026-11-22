package com.fcc.reco.data.repository

import com.fcc.reco.data.model.Categoria
import com.fcc.reco.data.model.Especificacion
import com.fcc.reco.data.model.FiltrosProducto
import com.fcc.reco.data.model.Grado
import com.fcc.reco.data.model.Producto
import com.fcc.reco.data.model.VendedorResumen
import com.fcc.reco.data.remote.IdMapa
import com.fcc.reco.data.remote.Nube
import com.fcc.reco.data.remote.NubeDatos
import com.fcc.reco.data.remote.ProductoDto
import com.fcc.reco.data.remote.epochDe
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

/**
 * Catálogo desde Supabase (solo anuncios publicados, con sus fotos). Las pantallas piden los datos de forma
 * síncrona, así que se cargan antes de abrir la app ([refrescar]) y aquí se consultan en memoria.
 * Los equipos de pedidos y favoritos que ya no están publicados se agregan con [asegurar].
 */
class ProductoRepositorySupabase(private val datos: NubeDatos) : ProductoRepository {

    private var catalogo: List<Producto> = emptyList()
    private val conocidos = HashMap<Int, Producto>()
    private var vendidos: Set<Int> = emptySet()

    /** Marca los equipos comprados por este usuario (para "Vendido" en Favoritos). */
    fun marcarVendidos(ids: Set<Int>) { vendidos = ids }

    suspend fun refrescar() {
        val filas = Nube.cliente.from("productos").select(COLUMNAS) {
            filter { eq("estado", "publicado") }
            order("creado", Order.DESCENDING)
        }.decodeList<ProductoDto>()
        val nuevos = filas.mapNotNull(::aProducto)
        catalogo = nuevos
        nuevos.forEach { conocidos[it.id] = it }
    }

    /** Carga los equipos que faltan (vendidos o pausados) para poder mostrarlos en pedidos y favoritos. */
    suspend fun asegurar(uuids: Collection<String>) {
        val faltan = uuids.filter { IdMapa.id(it) !in conocidos }.distinct()
        if (faltan.isEmpty()) return
        runCatching {
            Nube.cliente.from("productos").select(COLUMNAS) { filter { isIn("id", faltan) } }
                .decodeList<ProductoDto>().mapNotNull(::aProducto).forEach { conocidos[it.id] = it }
        }
    }

    private fun aProducto(d: ProductoDto): Producto? {
        val m = d.modelos ?: return null
        val categoria = aCategoria(m.categoria)
        val tipoEtiqueta = if (categoria == Categoria.AUDIO || categoria == Categoria.CALCULADORAS) "Tipo" else "Procesador"
        val specs = listOfNotNull(
            m.procesador?.let { Especificacion(tipoEtiqueta, it) },
            d.ram?.let { Especificacion("Memoria", it) },
            d.almacenamiento?.let { Especificacion("Almacenamiento", it) },
            m.pantalla?.let { Especificacion("Pantalla", it) }
        )
        val vendedor: VendedorResumen = datos.resumen(d.vendedorId)
        return Producto(
            id = IdMapa.id(d.id), categoria = categoria, marca = m.marca, modelo = m.modelo, anio = m.anio,
            almacenamiento = d.almacenamiento, ram = d.ram, color = d.color,
            grado = runCatching { Grado.valueOf(d.grado) }.getOrDefault(Grado.B),
            saludBateria = d.saludBateria, precio = d.precio, certificado = d.certificado,
            vendedor = vendedor, especificaciones = specs,
            accesorios = d.accesorios.joinToString(", ") { it.lowercase() }.ifEmpty { "el equipo" },
            // Solo celulares y tablets llevan IMEI; el resto, número de serie (que no se muestra).
            imeiUltimos4 = d.imeiUltimos4?.takeIf { categoria == Categoria.CELULARES || categoria == Categoria.TABLETS },
            puntoEntrega = datos.textoPunto(datos.perfiles[d.vendedorId]?.puntoPreferidoId),
            diasPublicado = ((System.currentTimeMillis() - epochDe(d.creado)) / 86_400_000L).toInt().coerceAtLeast(0),
            fotos = d.fotos.sortedBy { it.orden }.map { it.url },
            esPropio = d.vendedorId == datos.miUid
        )
    }

    override fun obtenerPublicados(filtros: FiltrosProducto): List<Producto> = filtrarYOrdenar(catalogo, filtros)

    override fun obtenerPorId(id: Int): Producto? = conocidos[id]

    override fun marcas(): List<String> =
        catalogo.groupingBy { it.marca }.eachCount().entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { it.key }

    override fun destacado(): Producto? = catalogo.firstOrNull { it.certificado && it.categoria == Categoria.LAPTOPS }
        ?: catalogo.firstOrNull { it.categoria == Categoria.LAPTOPS }

    override fun obtenerPorVendedor(nombre: String): List<Producto> = catalogo.filter { it.vendedor.nombre == nombre }

    override fun buscarVendedor(nombre: String): VendedorResumen? =
        datos.uuidDeVendedor(nombre)?.let { datos.resumen(it) }

    override fun estaVendido(id: Int): Boolean = id in vendidos

    private companion object {
        val COLUMNAS = Columns.raw(
            "id,vendedor_id,almacenamiento,ram,color,grado,salud_bateria,imei_ultimos4,accesorios,descripcion,precio,estado," +
                "certificado,creado,modelos(marca,modelo,categoria,anio,precio_referencia,procesador,pantalla),fotos(url,orden)"
        )

        /** "accesorios" no existe en la app: se muestra junto a Audio. */
        fun aCategoria(texto: String): Categoria = when (texto) {
            "laptops" -> Categoria.LAPTOPS
            "celulares" -> Categoria.CELULARES
            "tablets" -> Categoria.TABLETS
            "gaming" -> Categoria.GAMING
            "calculadoras" -> Categoria.CALCULADORAS
            else -> Categoria.AUDIO
        }
    }
}
