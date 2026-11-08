package com.fcc.reco.domain

import com.fcc.reco.data.model.Categoria

/** Reglas de publicación de Reco (IMEI/serie, fotos, precio y máximo de anuncios activos). */
object ValidadorPublicacion {
    const val MIN_FOTOS = 2
    const val MAX_FOTOS = 8
    const val MAX_ACTIVOS = 3
    const val PRECIO_MIN = 0.30
    const val PRECIO_MAX = 1.00

    enum class ErrorIdentificador { VACIO, FORMATO, LUHN, BLOQUEADO, DUPLICADO }
    enum class ResultadoPrecio { INVALIDO, DENTRO_DE_RANGO, REVISION_MANUAL }

    /** Celulares y tablets llevan IMEI de 15 dígitos; el resto, número de serie. */
    fun requiereImei(categoria: Categoria) = categoria == Categoria.CELULARES || categoria == Categoria.TABLETS

    /** IMEI de 15 dígitos que pasa el algoritmo de Luhn. */
    fun validarImei(imei: String) = imei.length == 15 && imei.all { it.isDigit() } && Luhn.esValido(imei)

    fun imeiBloqueado(identificador: String, bloqueados: Set<String>) = normalizar(identificador) in bloqueados

    fun imeiDuplicado(identificador: String, enUso: Set<String>) = normalizar(identificador) in enUso

    /** Valida el formato, la lista de bloqueados y que no esté en otro anuncio activo. Null si todo está bien. */
    fun validarIdentificador(
        valor: String, categoria: Categoria, bloqueados: Set<String>, enUso: Set<String>
    ): ErrorIdentificador? {
        val id = normalizar(valor)
        return when {
            id.isEmpty() -> ErrorIdentificador.VACIO
            requiereImei(categoria) && !(id.length == 15 && id.all { it.isDigit() }) -> ErrorIdentificador.FORMATO
            requiereImei(categoria) && !Luhn.esValido(id) -> ErrorIdentificador.LUHN
            !requiereImei(categoria) && !(id.length in 6..20 && id.all { it.isLetterOrDigit() }) -> ErrorIdentificador.FORMATO
            imeiBloqueado(id, bloqueados) -> ErrorIdentificador.BLOQUEADO
            imeiDuplicado(id, enUso) -> ErrorIdentificador.DUPLICADO
            else -> null
        }
    }

    fun validarFotos(cantidad: Int) = cantidad in MIN_FOTOS..MAX_FOTOS

    /** Entre 30 % y 100 % de la referencia es normal; fuera de ese rango va a revisión manual. */
    fun evaluarPrecio(precio: Int, referencia: Int): ResultadoPrecio = when {
        precio <= 0 || referencia <= 0 -> ResultadoPrecio.INVALIDO
        precio >= (referencia * PRECIO_MIN).toInt() && precio <= (referencia * PRECIO_MAX).toInt() -> ResultadoPrecio.DENTRO_DE_RANGO
        else -> ResultadoPrecio.REVISION_MANUAL
    }

    fun rangoPermitido(referencia: Int): IntRange = (referencia * PRECIO_MIN).toInt()..(referencia * PRECIO_MAX).toInt()

    fun puedePublicarMas(activos: Int) = activos < MAX_ACTIVOS

    private fun normalizar(valor: String) = valor.trim().uppercase().replace(" ", "")
}
