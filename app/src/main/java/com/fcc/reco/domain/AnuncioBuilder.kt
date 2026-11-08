package com.fcc.reco.domain

import com.fcc.reco.data.ModeloCatalogo
import com.fcc.reco.data.model.Anuncio
import com.fcc.reco.data.model.Categoria
import com.fcc.reco.data.model.EstadoAnuncio
import com.fcc.reco.data.model.Grado
import com.fcc.reco.data.model.VendedorResumen

/** Lo que el asistente de publicar va juntando en sus cinco pasos. */
data class BorradorAnuncio(
    val id: Int = 0,
    val categoria: Categoria = Categoria.LAPTOPS,
    val modelo: ModeloCatalogo? = null,
    val almacenamiento: String? = null,
    val ram: String? = null,
    val color: String? = null,
    val grado: Grado = Grado.A,
    val saludBateria: Int? = null,
    val identificador: String = "",
    val accesorios: Set<String> = emptySet(),
    val fotos: List<String> = emptyList(),
    val precio: Int = 0,
    val certificar: Boolean = false
)

/**
 * Patrón Builder del asistente de publicar: cada paso agrega su parte y `construir` arma el Anuncio.
 * Los métodos devuelven el mismo builder para poder encadenarlos.
 */
class AnuncioBuilder(inicial: BorradorAnuncio = BorradorAnuncio()) {

    var borrador: BorradorAnuncio = inicial
        private set

    /** Paso 1: al cambiar de categoría o de modelo se limpian las opciones que dependían de él. */
    fun categoria(c: Categoria) = apply {
        if (c != borrador.categoria) borrador = BorradorAnuncio(id = borrador.id, categoria = c)
    }

    fun modelo(m: ModeloCatalogo) = apply {
        borrador = borrador.copy(
            categoria = m.categoria, modelo = m,
            almacenamiento = m.almacenamientos.firstOrNull(), ram = m.rams.firstOrNull(),
            color = m.colores.firstOrNull(), precio = 0
        )
    }

    /** Paso 2 */
    fun almacenamiento(valor: String?) = apply { borrador = borrador.copy(almacenamiento = valor) }
    fun ram(valor: String?) = apply { borrador = borrador.copy(ram = valor) }
    fun color(valor: String) = apply { borrador = borrador.copy(color = valor) }
    fun grado(valor: Grado) = apply { borrador = borrador.copy(grado = valor) }
    fun saludBateria(valor: Int?) = apply { borrador = borrador.copy(saludBateria = valor) }
    fun identificador(valor: String) = apply { borrador = borrador.copy(identificador = valor) }
    fun alternarAccesorio(valor: String) = apply {
        val actual = borrador.accesorios
        borrador = borrador.copy(accesorios = if (valor in actual) actual - valor else actual + valor)
    }

    /** Paso 3 */
    fun agregarFoto(ruta: String) = apply {
        if (borrador.fotos.size < ValidadorPublicacion.MAX_FOTOS) borrador = borrador.copy(fotos = borrador.fotos + ruta)
    }
    fun quitarFoto(ruta: String) = apply { borrador = borrador.copy(fotos = borrador.fotos - ruta) }

    /** Paso 4 */
    fun precio(valor: Int) = apply { borrador = borrador.copy(precio = valor) }
    fun certificar(valor: Boolean) = apply { borrador = borrador.copy(certificar = valor) }

    /** Paso 5: arma el anuncio. Falla si todavía no se eligió modelo o color. */
    fun construir(
        correo: String, vendedor: VendedorResumen, puntoEntrega: String,
        estado: EstadoAnuncio, creado: Long = System.currentTimeMillis()
    ): Anuncio {
        val b = borrador
        val m = requireNotNull(b.modelo) { "Falta elegir el modelo" }
        return Anuncio(
            id = b.id, vendedorCorreo = correo, vendedor = vendedor,
            categoria = m.categoria, marca = m.marca, modelo = m.modelo, anio = m.anio, precioReferencia = m.precioReferencia,
            almacenamiento = b.almacenamiento, ram = b.ram, color = b.color ?: m.colores.first(),
            grado = b.grado, saludBateria = b.saludBateria, identificador = b.identificador.trim().uppercase().replace(" ", ""),
            accesorios = b.accesorios.toList(), fotos = b.fotos, precio = b.precio,
            certificar = b.certificar, certificado = false, estado = estado, motivoRechazo = null,
            revisionManual = ValidadorPublicacion.evaluarPrecio(b.precio, m.precioReferencia) == ValidadorPublicacion.ResultadoPrecio.REVISION_MANUAL,
            puntoEntrega = puntoEntrega, creado = creado
        )
    }

    companion object {
        /** Retoma un borrador guardado. */
        fun desde(a: Anuncio): AnuncioBuilder {
            val modelo = com.fcc.reco.data.CatalogoModelos.buscar(a.marca, a.modelo)
            return AnuncioBuilder(
                BorradorAnuncio(
                    id = a.id, categoria = a.categoria, modelo = modelo, almacenamiento = a.almacenamiento, ram = a.ram,
                    color = a.color, grado = a.grado, saludBateria = a.saludBateria, identificador = a.identificador,
                    accesorios = a.accesorios.toSet(), fotos = a.fotos, precio = a.precio, certificar = a.certificar
                )
            )
        }
    }
}
