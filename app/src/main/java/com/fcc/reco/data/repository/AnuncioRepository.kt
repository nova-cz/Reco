package com.fcc.reco.data.repository

import com.fcc.reco.data.DatosSimulados
import com.fcc.reco.data.local.AlmacenJson
import com.fcc.reco.data.model.Anuncio
import com.fcc.reco.data.model.Categoria
import com.fcc.reco.data.model.EstadoAnuncio
import com.fcc.reco.data.model.Grado
import com.fcc.reco.data.model.VendedorResumen
import com.fcc.reco.domain.ValidadorPublicacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/** Motivos por los que un anuncio no se puede enviar a revisión. */
enum class ErrorEnvio {
    FALTAN_DATOS, LIMITE_ACTIVOS, FOTOS, PRECIO,
    ID_VACIO, ID_FORMATO, ID_LUHN, ID_BLOQUEADO, ID_DUPLICADO
}

interface AnuncioRepository {
    val anuncios: StateFlow<List<Anuncio>>
    fun obtener(id: Int): Anuncio?
    fun misAnuncios(correo: String): List<Anuncio>
    /** Estado que ve el vendedor: Vendido en cuanto el equipo se paga. */
    fun estadoEfectivo(a: Anuncio): EstadoAnuncio
    fun activos(correo: String): Int
    /** IMEI/series de anuncios que siguen vivos (para detectar duplicados). */
    fun identificadoresEnUso(excluirId: Int = 0): Set<String>
    fun guardarBorrador(a: Anuncio): Anuncio
    /** Valida todo y lo deja En revisión. Devuelve null si salió bien. */
    fun enviarARevision(a: Anuncio): ErrorEnvio?
    fun pausar(id: Int)
    fun reanudar(id: Int)
    fun eliminar(id: Int)
    // Moderación (se simula con botones de debug; en Supabase, desde el panel).
    fun aprobar(id: Int)
    fun rechazar(id: Int, motivo: String)
}

/** Anuncios en memoria, guardados en un JSON interno. */
class AnuncioRepositorySimulado(
    private val almacen: AlmacenJson,
    private val idsVendidos: () -> Set<Int>
) : AnuncioRepository {

    private val _anuncios = MutableStateFlow(leer())
    override val anuncios: StateFlow<List<Anuncio>> = _anuncios.asStateFlow()

    override fun obtener(id: Int) = _anuncios.value.firstOrNull { it.id == id }

    override fun misAnuncios(correo: String) =
        _anuncios.value.filter { it.vendedorCorreo == correo }.sortedByDescending { it.creado }

    override fun estadoEfectivo(a: Anuncio) = if (a.id in idsVendidos()) EstadoAnuncio.VENDIDO else a.estado

    override fun activos(correo: String) = misAnuncios(correo).count { estadoEfectivo(it).esActivo }

    override fun identificadoresEnUso(excluirId: Int): Set<String> =
        _anuncios.value.filter { it.id != excluirId && it.identificador.isNotEmpty() && (estadoEfectivo(it).esActivo || it.estado == EstadoAnuncio.PAUSADO || estadoEfectivo(it) == EstadoAnuncio.VENDIDO) }
            .map { it.identificador }.toSet()

    override fun guardarBorrador(a: Anuncio): Anuncio {
        val guardado = a.copy(estado = EstadoAnuncio.BORRADOR, id = if (a.id == 0) siguienteId() else a.id)
        reemplazar(guardado)
        return guardado
    }

    override fun enviarARevision(a: Anuncio): ErrorEnvio? {
        val yaExiste = a.id != 0 && obtener(a.id)?.estado?.esActivo == true
        if (!yaExiste && !ValidadorPublicacion.puedePublicarMas(activos(a.vendedorCorreo))) return ErrorEnvio.LIMITE_ACTIVOS
        if (!ValidadorPublicacion.validarFotos(a.fotos.size)) return ErrorEnvio.FOTOS
        if (a.precio <= 0) return ErrorEnvio.PRECIO
        ValidadorPublicacion.validarIdentificador(
            a.identificador, a.categoria, DatosSimulados.imeiBloqueados, identificadoresEnUso(a.id)
        )?.let {
            return when (it) {
                ValidadorPublicacion.ErrorIdentificador.VACIO -> ErrorEnvio.ID_VACIO
                ValidadorPublicacion.ErrorIdentificador.FORMATO -> ErrorEnvio.ID_FORMATO
                ValidadorPublicacion.ErrorIdentificador.LUHN -> ErrorEnvio.ID_LUHN
                ValidadorPublicacion.ErrorIdentificador.BLOQUEADO -> ErrorEnvio.ID_BLOQUEADO
                ValidadorPublicacion.ErrorIdentificador.DUPLICADO -> ErrorEnvio.ID_DUPLICADO
            }
        }
        reemplazar(a.copy(id = if (a.id == 0) siguienteId() else a.id, estado = EstadoAnuncio.EN_REVISION, motivoRechazo = null))
        return null
    }

    override fun pausar(id: Int) = cambiar(id, EstadoAnuncio.PUBLICADO) { it.copy(estado = EstadoAnuncio.PAUSADO) }
    override fun reanudar(id: Int) = cambiar(id, EstadoAnuncio.PAUSADO) { it.copy(estado = EstadoAnuncio.PUBLICADO) }

    override fun eliminar(id: Int) {
        _anuncios.value = _anuncios.value.filter { it.id != id }
        guardar()
    }

    override fun aprobar(id: Int) = cambiar(id, EstadoAnuncio.EN_REVISION) {
        it.copy(estado = EstadoAnuncio.PUBLICADO, certificado = it.certificar, motivoRechazo = null)
    }

    override fun rechazar(id: Int, motivo: String) = cambiar(id, EstadoAnuncio.EN_REVISION) {
        it.copy(estado = EstadoAnuncio.RECHAZADO, motivoRechazo = motivo)
    }

    private fun cambiar(id: Int, desde: EstadoAnuncio, f: (Anuncio) -> Anuncio) {
        val a = obtener(id) ?: return
        if (a.estado != desde) return
        reemplazar(f(a))
    }

    private fun siguienteId() = maxOf(1000, (_anuncios.value.maxOfOrNull { it.id } ?: 0) + 1)

    private fun reemplazar(a: Anuncio) {
        val lista = _anuncios.value
        _anuncios.value = if (lista.any { it.id == a.id }) lista.map { if (it.id == a.id) a else it } else lista + a
        guardar()
    }

    private fun guardar() {
        val json = JSONArray()
        _anuncios.value.forEach { a ->
            json.put(JSONObject().apply {
                put("id", a.id); put("correo", a.vendedorCorreo)
                put("vNombre", a.vendedor.nombre); put("vCarrera", a.vendedor.carrera); put("vFacultad", a.vendedor.facultad)
                put("vRep", a.vendedor.reputacion); put("vVentas", a.vendedor.ventas)
                put("categoria", a.categoria.name); put("marca", a.marca); put("modelo", a.modelo); put("anio", a.anio)
                put("ref", a.precioReferencia); put("alm", a.almacenamiento ?: ""); put("ram", a.ram ?: "")
                put("color", a.color); put("grado", a.grado.name); put("bateria", a.saludBateria ?: -1)
                put("ident", a.identificador); put("accesorios", JSONArray(a.accesorios)); put("fotos", JSONArray(a.fotos))
                put("precio", a.precio); put("certificar", a.certificar); put("certificado", a.certificado)
                put("estado", a.estado.name); put("motivo", a.motivoRechazo ?: ""); put("manual", a.revisionManual)
                put("punto", a.puntoEntrega); put("creado", a.creado)
            })
        }
        almacen.guardar(json)
    }

    private fun leer(): List<Anuncio> {
        val json = almacen.leer()
        fun JSONArray.lista() = (0 until length()).map { getString(it) }
        return (0 until json.length()).mapNotNull { i ->
            runCatching {
                val o = json.getJSONObject(i)
                Anuncio(
                    id = o.getInt("id"), vendedorCorreo = o.getString("correo"),
                    vendedor = VendedorResumen(o.getString("vNombre"), o.getString("vCarrera"), o.getString("vFacultad"), o.getDouble("vRep"), o.getInt("vVentas")),
                    categoria = Categoria.valueOf(o.getString("categoria")), marca = o.getString("marca"), modelo = o.getString("modelo"),
                    anio = o.getInt("anio"), precioReferencia = o.getInt("ref"),
                    almacenamiento = o.getString("alm").ifEmpty { null }, ram = o.getString("ram").ifEmpty { null },
                    color = o.getString("color"), grado = Grado.valueOf(o.getString("grado")),
                    saludBateria = o.getInt("bateria").takeIf { it >= 0 }, identificador = o.getString("ident"),
                    accesorios = o.getJSONArray("accesorios").lista(), fotos = o.getJSONArray("fotos").lista(),
                    precio = o.getInt("precio"), certificar = o.getBoolean("certificar"), certificado = o.getBoolean("certificado"),
                    estado = EstadoAnuncio.valueOf(o.getString("estado")), motivoRechazo = o.getString("motivo").ifEmpty { null },
                    revisionManual = o.getBoolean("manual"), puntoEntrega = o.getString("punto"), creado = o.getLong("creado")
                )
            }.getOrNull()
        }
    }
}
