package com.fcc.reco.ui.vendedor

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fcc.reco.data.DatosSimulados
import com.fcc.reco.data.ModeloCatalogo
import com.fcc.reco.data.Repositorios
import com.fcc.reco.data.local.AlmacenFotos
import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.data.model.Anuncio
import com.fcc.reco.data.model.Categoria
import com.fcc.reco.data.model.EstadoAnuncio
import com.fcc.reco.data.model.Grado
import com.fcc.reco.data.model.VendedorResumen
import com.fcc.reco.data.repository.ErrorEnvio
import com.fcc.reco.domain.AnuncioBuilder
import com.fcc.reco.domain.BorradorAnuncio
import com.fcc.reco.domain.ValidadorPublicacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Estado compartido por los cinco pasos de Publicar. Usa el patrón Builder (`AnuncioBuilder`). */
class PublicarViewModel(private val aplicacion: Application) : AndroidViewModel(aplicacion) {

    private var builder = AnuncioBuilder()
    private val _estado = MutableStateFlow(builder.borrador)
    val estado: StateFlow<BorradorAnuncio> = _estado.asStateFlow()
    val borrador: BorradorAnuncio get() = builder.borrador

    private fun cambiar(f: AnuncioBuilder.() -> Unit) {
        builder.f()
        _estado.value = builder.borrador
    }

    fun nuevo() {
        builder = AnuncioBuilder()
        _estado.value = builder.borrador
    }

    fun retomar(anuncioId: Int) {
        Repositorios.anuncios.obtener(anuncioId)?.let {
            builder = AnuncioBuilder.desde(it)
            _estado.value = builder.borrador
        }
    }

    // Paso 1
    fun elegirCategoria(c: Categoria) = cambiar { categoria(c) }
    fun elegirModelo(m: ModeloCatalogo) = cambiar { modelo(m) }

    // Paso 2
    fun almacenamiento(v: String) = cambiar { almacenamiento(v) }
    fun ram(v: String) = cambiar { ram(v) }
    fun color(v: String) = cambiar { color(v) }
    fun grado(v: Grado) = cambiar { grado(v) }
    fun saludBateria(v: Int?) = cambiar { saludBateria(v) }
    fun identificador(v: String) = cambiar { identificador(v) }
    fun alternarAccesorio(v: String) = cambiar { alternarAccesorio(v) }

    fun errorIdentificador(): ValidadorPublicacion.ErrorIdentificador? = borrador.let {
        ValidadorPublicacion.validarIdentificador(
            it.identificador, it.categoria, DatosSimulados.imeiBloqueados, Repositorios.anuncios.identificadoresEnUso(it.id)
        )
    }

    // Paso 3
    fun agregarFotos(uris: List<Uri>) {
        val cupo = ValidadorPublicacion.MAX_FOTOS - borrador.fotos.size
        viewModelScope.launch {
            val rutas = withContext(Dispatchers.IO) { uris.take(cupo).mapNotNull { AlmacenFotos.guardar(aplicacion, it) } }
            cambiar { rutas.forEach { agregarFoto(it) } }
        }
    }

    fun agregarFotoDePrueba() {
        if (borrador.fotos.size >= ValidadorPublicacion.MAX_FOTOS) return
        viewModelScope.launch {
            val ruta = withContext(Dispatchers.IO) { AlmacenFotos.generarDePrueba(aplicacion, borrador.fotos.size + 1) }
            if (ruta != null) cambiar { agregarFoto(ruta) }
        }
    }

    fun quitarFoto(ruta: String) = cambiar { quitarFoto(ruta) }

    // Paso 4
    fun precio(v: Int) = cambiar { precio(v) }
    fun certificar(v: Boolean) = cambiar { certificar(v) }

    // Paso 5 y borrador
    private fun construir(estado: EstadoAnuncio): Anuncio? {
        if (borrador.modelo == null) return null
        val sesion = SesionLocal(aplicacion).leer() ?: return null
        val vendedor = VendedorResumen(sesion.nombreCorto, sesion.carrera.orEmpty(), sesion.universidad ?: "BUAP", 5.0, 0)
        val punto = sesion.vendedor?.puntoEntrega ?: DatosSimulados.puntosEntrega.first().nombre
        return builder.construir(sesion.correo, vendedor, punto, estado)
    }

    /** Guarda lo avanzado como Borrador. Devuelve falso si todavía no hay modelo. */
    fun guardarBorrador(): Boolean {
        val anuncio = construir(EstadoAnuncio.BORRADOR) ?: return false
        val guardado = Repositorios.anuncios.guardarBorrador(anuncio)
        cambiar { }
        builder = AnuncioBuilder(borrador.copy(id = guardado.id))
        _estado.value = builder.borrador
        return true
    }

    /** Valida todo y lo manda a revisión. Devuelve null si salió bien. */
    fun enviar(): ErrorEnvio? {
        val anuncio = construir(EstadoAnuncio.EN_REVISION) ?: return ErrorEnvio.FALTAN_DATOS
        val error = Repositorios.anuncios.enviarARevision(anuncio)
        if (error == null) nuevo()
        return error
    }
}
