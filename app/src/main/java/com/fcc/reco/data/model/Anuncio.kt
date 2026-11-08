package com.fcc.reco.data.model

import com.fcc.reco.data.CatalogoModelos
import com.fcc.reco.domain.ValidadorPublicacion

/** Estados de un anuncio. Solo el moderador pasa de En revisión a Publicado o Rechazado. */
enum class EstadoAnuncio(val etiqueta: String) {
    BORRADOR("Borrador"),
    EN_REVISION("En revisión"),
    PUBLICADO("Publicado"),
    RECHAZADO("Rechazado"),
    RESERVADO("Reservado"),
    VENDIDO("Vendido"),
    PAUSADO("Pausado");

    /** Cuenta para el máximo de 3 anuncios activos. */
    val esActivo: Boolean get() = this == EN_REVISION || this == PUBLICADO || this == RESERVADO
}

/** Anuncio creado por el estudiante con el asistente de publicar. */
data class Anuncio(
    val id: Int,
    val vendedorCorreo: String,
    val vendedor: VendedorResumen,
    val categoria: Categoria,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val precioReferencia: Int,
    val almacenamiento: String?,
    val ram: String?,
    val color: String,
    val grado: Grado,
    val saludBateria: Int?,
    /** IMEI (celulares y tablets) o número de serie (el resto). */
    val identificador: String,
    val accesorios: List<String>,
    /** Rutas de archivos internos con las fotos (la primera es la portada). */
    val fotos: List<String>,
    val precio: Int,
    val certificar: Boolean,
    val certificado: Boolean,
    val estado: EstadoAnuncio,
    val motivoRechazo: String?,
    /** Precio fuera del 30 %–100 % de la referencia: el moderador lo revisa a mano. */
    val revisionManual: Boolean,
    val puntoEntrega: String,
    val creado: Long
) {
    val titulo: String get() = if (almacenamiento != null) "$modelo · $almacenamiento" else modelo

    /** Lo que ve el comprador en el catálogo. */
    fun comoProducto(esPropio: Boolean): Producto {
        val ficha = CatalogoModelos.buscar(marca, modelo)
        val tipoEtiqueta = if (categoria == Categoria.AUDIO || categoria == Categoria.CALCULADORAS) "Tipo" else "Procesador"
        val specs = listOfNotNull(
            ficha?.procesador?.let { Especificacion(tipoEtiqueta, it) },
            ram?.let { Especificacion("Memoria", it) },
            almacenamiento?.let { Especificacion("Almacenamiento", it) },
            ficha?.pantalla?.let { Especificacion("Pantalla", it) }
        )
        return Producto(
            id = id, categoria = categoria, marca = marca, modelo = modelo, anio = anio,
            almacenamiento = almacenamiento, ram = ram, color = color, grado = grado, saludBateria = saludBateria,
            precio = precio, certificado = certificado, vendedor = vendedor, especificaciones = specs,
            accesorios = accesorios.joinToString(", ") { it.lowercase() }.ifEmpty { "el equipo" },
            imeiUltimos4 = identificador.takeLast(4).takeIf { ValidadorPublicacion.requiereImei(categoria) && identificador.length >= 4 },
            puntoEntrega = puntoEntrega,
            diasPublicado = ((System.currentTimeMillis() - creado) / 86_400_000L).toInt().coerceAtLeast(0),
            fotos = fotos, esPropio = esPropio
        )
    }
}
