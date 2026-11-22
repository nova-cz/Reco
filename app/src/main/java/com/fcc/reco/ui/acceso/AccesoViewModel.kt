package com.fcc.reco.ui.acceso

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fcc.reco.R
import com.fcc.reco.data.Repositorios
import com.fcc.reco.data.model.Sesion
import com.fcc.reco.data.repository.AuthRepository
import com.fcc.reco.data.repository.PerfilRepository
import com.fcc.reco.data.repository.VerificacionRepository
import com.fcc.reco.domain.ValidadorAcceso
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** Error de un campo del formulario (o general, si [campo] es null). */
data class ErrorAcceso(val campo: Campo?, val mensaje: Int) {
    enum class Campo { CORREO, CLAVE }
}

data class EstadoAcceso(
    val cargando: Boolean = false,
    val error: ErrorAcceso? = null
)

/** Cosas que el ViewModel le pide hacer a la pantalla (se consumen una sola vez). */
sealed interface EventoAcceso {
    data object IrAVerificar : EventoAcceso
    data object MostrarCarrera : EventoAcceso
    data object AbrirApp : EventoAcceso
    data class Mensaje(val texto: Int) : EventoAcceso
}

/** ViewModel compartido por todo el flujo de acceso. */
class AccesoViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {

    // Siempre se leen de Repositorios: el switch de debug puede cambiar la implementación.
    private val auth: AuthRepository get() = Repositorios.auth
    private val verificacion: VerificacionRepository get() = Repositorios.verificacion
    private val perfil: PerfilRepository get() = Repositorios.perfil

    private val _estado = MutableStateFlow(EstadoAcceso())
    val estado: StateFlow<EstadoAcceso> = _estado.asStateFlow()

    private val _eventos = Channel<EventoAcceso>(Channel.BUFFERED)
    val eventos = _eventos.receiveAsFlow()

    fun sesion(): Sesion? = auth.sesionActual()

    fun limpiarError() {
        _estado.value = _estado.value.copy(error = null)
    }

    fun iniciar(correo: String, clave: String, esRegistro: Boolean) {
        if (!ValidadorAcceso.correoValido(correo)) {
            _estado.value = EstadoAcceso(error = ErrorAcceso(ErrorAcceso.Campo.CORREO, R.string.error_correo))
            return
        }
        if (!ValidadorAcceso.claveValida(clave)) {
            _estado.value = EstadoAcceso(error = ErrorAcceso(ErrorAcceso.Campo.CLAVE, R.string.error_contrasena))
            return
        }
        entrar { if (esRegistro) auth.registrarConCorreo(correo, clave) else auth.iniciarConCorreo(correo, clave) }
    }

    fun iniciarConGoogle() = entrar { auth.iniciarConGoogle() }

    private fun entrar(accion: suspend () -> Result<Sesion>) {
        if (_estado.value.cargando) return
        viewModelScope.launch {
            _estado.value = EstadoAcceso(cargando = true)
            val resultado = accion()
            _estado.value = EstadoAcceso()
            resultado.onSuccess { sesion ->
                when {
                    sesion.completa -> abrirApp()
                    sesion.verificado -> _eventos.send(EventoAcceso.MostrarCarrera)
                    else -> _eventos.send(EventoAcceso.IrAVerificar)
                }
            }.onFailure {
                _estado.value = EstadoAcceso(error = ErrorAcceso(null, if (it is UnsupportedOperationException) R.string.error_google else R.string.error_sesion))
            }
        }
    }

    /** Regreso del login institucional por `reco://verificado`. */
    fun procesarDeepLink(uri: Uri) {
        viewModelScope.launch {
            verificacion.procesarDeepLink(uri).fold(
                onSuccess = { _eventos.send(EventoAcceso.MostrarCarrera) },
                onFailure = {
                    val mensaje = if (it.message == "dominio") R.string.error_dominio else R.string.error_enlace
                    _eventos.send(EventoAcceso.Mensaje(mensaje))
                }
            )
        }
    }

    fun elegirCarrera(carrera: String) {
        viewModelScope.launch {
            runCatching { perfil.guardarCarrera(carrera) }
                .onSuccess { abrirApp() }
                .onFailure { _eventos.send(EventoAcceso.Mensaje(R.string.error_red)) }
        }
    }

    /** Carga los datos de Supabase (catálogo, pedidos, favoritos) antes de abrir la app. */
    private suspend fun abrirApp() {
        _estado.value = EstadoAcceso(cargando = true)
        val listo = preparar()
        _estado.value = EstadoAcceso()
        _eventos.send(if (listo) EventoAcceso.AbrirApp else EventoAcceso.Mensaje(R.string.error_red))
    }

    /** Falso si no hay sesión o no se pudo cargar (sin red). */
    suspend fun preparar(): Boolean = runCatching { Repositorios.precargar() }.getOrDefault(false)
}
