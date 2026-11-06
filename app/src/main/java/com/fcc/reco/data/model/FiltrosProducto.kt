package com.fcc.reco.data.model

enum class Orden(val etiqueta: String) {
    MAS_RECIENTES("Más recientes"),
    PRECIO_MENOR("Precio: menor a mayor"),
    PRECIO_MAYOR("Precio: mayor a menor")
}

data class FiltrosProducto(
    val categoria: Categoria? = null,
    val marca: String? = null,
    val consulta: String = "",
    val soloGradosAB: Boolean = false,
    val precioMax: Int? = null,
    val soloCertificados: Boolean = false,
    val orden: Orden = Orden.MAS_RECIENTES
) {
    val hayFiltros: Boolean
        get() = categoria != null || marca != null || consulta.isNotBlank() ||
            soloGradosAB || precioMax != null || soloCertificados
}
