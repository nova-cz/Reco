package com.fcc.reco.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** Filas de Supabase tal como llegan por Postgrest (ver supabase/migrations). */
@Serializable
data class ModeloDto(
    val marca: String,
    val modelo: String,
    val categoria: String,
    val anio: Int = 2020,
    @SerialName("precio_referencia") val precioReferencia: Int = 0,
    val procesador: String? = null,
    val pantalla: String? = null
)

@Serializable
data class FotoDto(
    val url: String,
    val orden: Int = 0,
    @SerialName("es_portada") val esPortada: Boolean = false
)

@Serializable
data class ProductoDto(
    val id: String,
    @SerialName("vendedor_id") val vendedorId: String,
    val almacenamiento: String? = null,
    val ram: String? = null,
    val color: String = "",
    val grado: String = "B",
    @SerialName("salud_bateria") val saludBateria: Int? = null,
    @SerialName("imei_ultimos4") val imeiUltimos4: String? = null,
    val accesorios: List<String> = emptyList(),
    val descripcion: String? = null,
    val precio: Int = 0,
    val estado: String = "publicado",
    val certificado: Boolean = false,
    val certificar: Boolean = false,
    @SerialName("motivo_rechazo") val motivoRechazo: String? = null,
    @SerialName("revision_manual") val revisionManual: Boolean = false,
    val creado: String = "",
    val modelos: ModeloDto? = null,
    val fotos: List<FotoDto> = emptyList()
)

@Serializable
data class NombreDto(val nombre: String)

@Serializable
data class PuntoDto(val id: String, val nombre: String, val descripcion: String? = null)

/** Fila de la vista perfiles_publicos (sin teléfono ni CLABE). */
@Serializable
data class PerfilPublicoDto(
    val id: String,
    val nombre: String,
    val apodo: String? = null,
    @SerialName("acerca_de") val acercaDe: String? = null,
    val carrera: String? = null,
    @SerialName("es_vendedor") val esVendedor: Boolean = false,
    val reputacion: Double = 0.0,
    @SerialName("ventas_completadas") val ventasCompletadas: Int = 0,
    @SerialName("donde_entregas") val dondeEntregas: String? = null,
    val horario: String? = null,
    @SerialName("acepta_tarjeta") val aceptaTarjeta: Boolean = false,
    @SerialName("punto_preferido_id") val puntoPreferidoId: String? = null
)

/** El perfil propio (tabla perfiles) con la universidad y el punto de entrega ya resueltos. */
@Serializable
data class PerfilDto(
    val id: String,
    val nombre: String,
    val carrera: String? = null,
    val verificado: Boolean = false,
    @SerialName("es_vendedor") val esVendedor: Boolean = false,
    val telefono: String? = null,
    val disponibilidad: JsonObject? = null,
    @SerialName("clabe_simulada") val clabe: String? = null,
    val apodo: String? = null,
    @SerialName("acerca_de") val acercaDe: String? = null,
    val creado: String = "",
    val universidades: NombreDto? = null,
    @SerialName("puntos_entrega") val puntoEntrega: NombreDto? = null
)

@Serializable
data class PedidoDto(
    val id: String,
    @SerialName("producto_id") val productoId: String,
    @SerialName("comprador_id") val compradorId: String,
    @SerialName("vendedor_id") val vendedorId: String,
    val monto: Int,
    val comision: Int,
    @SerialName("punto_id") val puntoId: String? = null,
    val horario: String? = null,
    val estado: String,
    val creado: String = ""
)

@Serializable
data class EstrellasDto(@SerialName("pedido_id") val pedidoId: String, val estrellas: Int)

@Serializable
data class FavoritoDto(@SerialName("producto_id") val productoId: String)

/** Fila de la vista resenas_publicas. */
@Serializable
data class ResenaDto(
    val id: String,
    @SerialName("pedido_id") val pedidoId: String,
    val estrellas: Int,
    val etiquetas: List<String> = emptyList(),
    val comentario: String? = null,
    val creado: String = "",
    @SerialName("vendedor_id") val vendedorId: String,
    @SerialName("comprador_id") val compradorId: String,
    val producto: String = ""
)
