package com.fcc.reco.data.repository

import com.fcc.reco.data.DatosSimulados
import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.data.model.Anuncio
import com.fcc.reco.data.model.EstadoAnuncio
import com.fcc.reco.data.model.Grado
import com.fcc.reco.data.remote.IdMapa
import com.fcc.reco.data.remote.Nube
import com.fcc.reco.data.remote.NubeDatos
import com.fcc.reco.data.remote.ProductoDto
import com.fcc.reco.data.remote.epochDe
import com.fcc.reco.domain.ValidadorPublicacion
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.util.UUID

@Serializable
private data class ModeloIdDto(val id: String, val marca: String, val modelo: String)

/**
 * Anuncios del vendedor en Supabase (`productos` + `fotos`; las imágenes en Storage, bucket "fotos").
 * Los **borradores** se quedan en el teléfono (con rutas de archivo) y solo al **enviar a revisión** se suben las
 * fotos a `fotos/<vendedor_id>/<anuncio_id>/<n>.jpg` y se crea el anuncio. La moderación (aprobar o rechazar) se
 * hace desde el panel de Supabase, así que aquí no hay acciones de moderador.
 */
class AnuncioRepositorySupabase(
    private val borradores: AnuncioRepositorySimulado,
    private val datos: NubeDatos,
    private val sesion: SesionLocal
) : AnuncioRepository {

    private val remotos = MutableStateFlow<List<Anuncio>>(emptyList())
    private var modeloIds: Map<Pair<String, String>, String> = emptyMap()

    override val anuncios: StateFlow<List<Anuncio>> =
        combine(borradores.anuncios, remotos) { b, r -> b.filter { it.estado == EstadoAnuncio.BORRADOR } + r }
            .stateIn(Nube.alcance, SharingStarted.Eagerly, emptyList())

    private fun todos(): List<Anuncio> = borradores.anuncios.value.filter { it.estado == EstadoAnuncio.BORRADOR } + remotos.value

    private fun esRemoto(id: Int) = remotos.value.any { it.id == id }

    override fun obtener(id: Int) = todos().firstOrNull { it.id == id }
    override fun misAnuncios(correo: String) = todos().filter { it.vendedorCorreo == correo }.sortedByDescending { it.creado }
    override fun estadoEfectivo(a: Anuncio) = a.estado
    override fun activos(correo: String) = misAnuncios(correo).count { it.estado.esActivo }

    override fun identificadoresEnUso(excluirId: Int): Set<String> =
        todos().filter {
            it.id != excluirId && it.identificador.isNotEmpty() &&
                (it.estado.esActivo || it.estado == EstadoAnuncio.PAUSADO || it.estado == EstadoAnuncio.VENDIDO)
        }.map { it.identificador }.toSet()

    override suspend fun guardarBorrador(a: Anuncio): Anuncio = borradores.guardarBorrador(a)

    override suspend fun refrescar() {
        val uid = datos.miUid ?: return
        if (modeloIds.isEmpty()) {
            modeloIds = Nube.cliente.from("modelos").select(Columns.raw("id,marca,modelo")).decodeList<ModeloIdDto>()
                .associate { (it.marca to it.modelo) to it.id }
        }
        val filas = Nube.cliente.from("productos").select(COLUMNAS) {
            filter { eq("vendedor_id", uid) }
            order("creado", Order.DESCENDING)
        }.decodeList<ProductoDto>()
        val correo = sesion.leer()?.correo.orEmpty()
        remotos.value = filas.mapNotNull { d ->
            // El IMEI completo solo lo devuelve la función al dueño; se usa para validar duplicados y retomar anuncios.
            val imei = runCatching {
                Nube.cliente.postgrest.rpc("obtener_mi_imei", buildJsonObject { put("p_producto", d.id) }).decodeAsOrNull<String>()
            }.getOrNull().orEmpty()
            aAnuncio(d, correo, imei)
        }
    }

    private fun aAnuncio(d: ProductoDto, correo: String, imei: String): Anuncio? {
        val m = d.modelos ?: return null
        return Anuncio(
            id = IdMapa.id(d.id), vendedorCorreo = correo, vendedor = datos.resumen(d.vendedorId),
            categoria = ProductoRepositorySupabase.aCategoria(m.categoria), marca = m.marca, modelo = m.modelo, anio = m.anio,
            precioReferencia = m.precioReferencia, almacenamiento = d.almacenamiento, ram = d.ram, color = d.color,
            grado = runCatching { Grado.valueOf(d.grado) }.getOrDefault(Grado.B), saludBateria = d.saludBateria,
            identificador = imei, accesorios = d.accesorios,
            fotos = d.fotos.sortedWith(compareByDescending<com.fcc.reco.data.remote.FotoDto> { it.esPortada }.thenBy { it.orden }).map { it.url },
            precio = d.precio, certificar = d.certificar, certificado = d.certificado,
            estado = runCatching { EstadoAnuncio.valueOf(d.estado.uppercase()) }.getOrDefault(EstadoAnuncio.EN_REVISION),
            motivoRechazo = d.motivoRechazo, revisionManual = d.revisionManual,
            puntoEntrega = datos.textoPunto(datos.perfiles[d.vendedorId]?.puntoPreferidoId), creado = epochDe(d.creado)
        )
    }

    override suspend fun enviarARevision(a: Anuncio): ErrorEnvio? {
        val uid = datos.miUid ?: return ErrorEnvio.RED
        val existente = esRemoto(a.id)
        if (!existente && !ValidadorPublicacion.puedePublicarMas(activos(a.vendedorCorreo))) return ErrorEnvio.LIMITE_ACTIVOS
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
        return runCatching {
            if (modeloIds.isEmpty()) refrescar()
            val modeloId = modeloIds[a.marca to a.modelo] ?: return ErrorEnvio.FALTAN_DATOS
            val uuid = if (existente) IdMapa.uuid(a.id)!! else UUID.randomUUID().toString()

            // 1) Fotos: las nuevas (rutas de archivo) se suben a fotos/<vendedor>/<anuncio>/<n>.jpg; las que ya son URL se conservan.
            val bucket = Nube.cliente.storage.from("fotos")
            var siguiente = a.fotos.filter { it.startsWith("http") }
                .mapNotNull { it.substringAfterLast('/').substringBefore('.').toIntOrNull() }.maxOrNull()?.plus(1) ?: 0
            val urls = a.fotos.map { foto ->
                if (foto.startsWith("http")) foto
                else {
                    val ruta = "$uid/$uuid/${siguiente++}.jpg"
                    bucket.upload(ruta, File(foto).readBytes()) { upsert = true; contentType = ContentType.Image.JPEG }
                    bucket.publicUrl(ruta)
                }
            }

            // 2) El anuncio en revisión.
            val campos = buildJsonObject {
                put("modelo_id", modeloId)
                put("almacenamiento", a.almacenamiento?.let { JsonPrimitive(it) } ?: JsonNull)
                put("ram", a.ram?.let { JsonPrimitive(it) } ?: JsonNull)
                put("color", a.color)
                put("grado", a.grado.name)
                put("salud_bateria", a.saludBateria?.let { JsonPrimitive(it) } ?: JsonNull)
                put("imei", a.identificador)
                put("accesorios", JsonArray(a.accesorios.map { JsonPrimitive(it) }))
                put("precio", a.precio)
                put("certificar", a.certificar)
            }
            if (existente) {
                val estado = if (obtener(a.id)?.estado == EstadoAnuncio.RECHAZADO) JsonPrimitive("en_revision") else null
                Nube.cliente.from("productos").update(JsonObject(campos + listOfNotNull(estado?.let { "estado" to it }))) {
                    filter { eq("id", uuid) }
                }
            } else {
                Nube.cliente.from("productos").insert(JsonObject(campos + mapOf(
                    "id" to JsonPrimitive(uuid), "vendedor_id" to JsonPrimitive(uid), "estado" to JsonPrimitive("en_revision")
                )))
            }

            // 3) Las fotos del anuncio (portada = la primera). Si falla, un anuncio nuevo no queda a medias.
            try {
                if (existente) Nube.cliente.from("fotos").delete { filter { eq("producto_id", uuid) } }
                Nube.cliente.from("fotos").insert(urls.mapIndexed { i, url ->
                    buildJsonObject { put("producto_id", uuid); put("url", url); put("orden", i); put("es_portada", i == 0) }
                })
            } catch (e: Exception) {
                if (!existente) runCatching { Nube.cliente.from("productos").delete { filter { eq("id", uuid) } } }
                throw e
            }

            if (a.id != 0 && !existente) borradores.eliminar(a.id)
            refrescar()
            null
        }.getOrElse { e -> errorDeServidor(e) }
    }

    /** Traduce el rechazo del servidor (triggers y restricciones de la base) al error que ya conoce la pantalla. */
    private fun errorDeServidor(e: Throwable): ErrorEnvio {
        // Solo en debug y solo el tipo de error: el mensaje de Supabase puede traer datos del anuncio.
        if (com.fcc.reco.BuildConfig.DEBUG) android.util.Log.w("RecoAnuncios", "Envío rechazado: ${e::class.simpleName}")
        val texto = e.message.orEmpty()
        return when {
            "Máximo 3 anuncios" in texto -> ErrorEnvio.LIMITE_ACTIVOS
            "IMEI bloqueado" in texto -> ErrorEnvio.ID_BLOQUEADO
            "productos_imei_activo" in texto || "duplicate key" in texto -> ErrorEnvio.ID_DUPLICADO
            else -> ErrorEnvio.RED
        }
    }

    override suspend fun pausar(id: Int) = cambiarEstado(id, "pausado")
    override suspend fun reanudar(id: Int) = cambiarEstado(id, "publicado")

    private suspend fun cambiarEstado(id: Int, estado: String) {
        val uuid = IdMapa.uuid(id) ?: return
        Nube.cliente.from("productos").update({ set("estado", estado) }) { filter { eq("id", uuid) } }
        refrescar()
    }

    override suspend fun eliminar(id: Int) {
        if (!esRemoto(id)) return borradores.eliminar(id)
        val uuid = IdMapa.uuid(id) ?: return
        Nube.cliente.from("productos").delete { filter { eq("id", uuid) } }   // solo borrador, rechazado o pausado (RLS)
        refrescar()
    }

    // La moderación se hace desde el panel de Supabase.
    override fun aprobar(id: Int) {}
    override fun rechazar(id: Int, motivo: String) {}

    private companion object {
        val COLUMNAS = Columns.raw(
            "id,vendedor_id,almacenamiento,ram,color,grado,salud_bateria,accesorios,descripcion,precio,estado,certificar," +
                "certificado,motivo_rechazo,revision_manual,creado,modelos(marca,modelo,categoria,anio,precio_referencia,procesador,pantalla)," +
                "fotos(url,orden,es_portada)"
        )
    }
}
