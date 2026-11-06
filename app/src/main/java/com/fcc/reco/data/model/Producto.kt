package com.fcc.reco.data.model

enum class Grado(val letra: String, val etiqueta: String) {
    A("A", "Excelente"),
    B("B", "Muy bueno"),
    C("C", "Bueno")
}

data class VendedorResumen(
    val nombre: String,
    val carrera: String,
    val facultad: String,
    val reputacion: Double,
    val ventas: Int
) {
    val iniciales: String
        get() = nombre.split(" ").filter { it.isNotEmpty() }.take(2).joinToString("") { it.first().uppercase() }
}

data class Especificacion(val etiqueta: String, val valor: String)

/** Anuncio de un equipo. Cada equipo es pieza única (sin cantidades). */
data class Producto(
    val id: Int,
    val categoria: Categoria,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val almacenamiento: String?,
    val ram: String?,
    val color: String,
    val grado: Grado,
    val saludBateria: Int?,
    val precio: Int,
    val certificado: Boolean,
    val vendedor: VendedorResumen,
    val especificaciones: List<Especificacion>,
    val accesorios: String,
    val imeiUltimos4: String?,
    val puntoEntrega: String,
    val diasPublicado: Int
) {
    /** Título corto para tarjetas: "MacBook Air M1 · 256 GB". */
    val titulo: String get() = if (almacenamiento != null) "$modelo · $almacenamiento" else modelo

    val tituloDetalle: String get() = "$modelo · $anio"

    val descripcion: String
        get() = "${grado.etiqueta}: ${descripcionGrado(grado)} Incluye $accesorios. Entrega en $puntoEntrega."

    private fun descripcionGrado(grado: Grado) = when (grado) {
        Grado.A -> "sin marcas visibles y funciona perfecto."
        Grado.B -> "con marcas leves de uso y funciona perfecto."
        Grado.C -> "con rayones visibles, pero funciona perfecto."
    }
}
