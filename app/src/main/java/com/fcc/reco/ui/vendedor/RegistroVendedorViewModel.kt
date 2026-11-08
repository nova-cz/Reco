package com.fcc.reco.ui.vendedor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.fcc.reco.R
import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.data.model.PerfilVendedor
import com.fcc.reco.data.repository.PerfilRepositorySimulado
import com.fcc.reco.domain.ValidadorVendedor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EstadoRegistro(
    val punto: String? = null,
    val dias: Set<String> = emptySet(),
    val franjas: Set<String> = emptySet(),
    val reglas: Boolean = false,
    val errorTelefono: Int? = null,
    val errorClabe: Int? = null,
    /** Error de punto, días/franjas o reglas (un solo mensaje, el primero que falle). */
    val errorGeneral: Int? = null,
    val registrado: Boolean = false
)

class RegistroVendedorViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {

    private val perfil = PerfilRepositorySimulado(SesionLocal(aplicacion))

    private val _estado = MutableStateFlow(EstadoRegistro())
    val estado: StateFlow<EstadoRegistro> = _estado.asStateFlow()

    fun elegirPunto(punto: String) = _estado.update { it.copy(punto = punto, errorGeneral = null) }

    fun alternarDia(dia: String) = _estado.update {
        it.copy(dias = if (dia in it.dias) it.dias - dia else it.dias + dia, errorGeneral = null)
    }

    fun alternarFranja(franja: String) = _estado.update {
        it.copy(franjas = if (franja in it.franjas) it.franjas - franja else it.franjas + franja, errorGeneral = null)
    }

    fun alternarReglas() = _estado.update { it.copy(reglas = !it.reglas, errorGeneral = null) }

    fun crear(telefono: String, clabe: String) {
        val e = _estado.value
        val errorTelefono = if (ValidadorVendedor.telefonoValido(telefono)) null else R.string.error_telefono
        val errorClabe = if (ValidadorVendedor.clabeValida(clabe)) null else R.string.error_clabe
        val errorGeneral = when {
            e.punto == null -> R.string.error_registro_punto
            e.dias.isEmpty() || e.franjas.isEmpty() -> R.string.error_registro_dias
            !e.reglas -> R.string.error_registro_reglas
            else -> null
        }
        _estado.update { it.copy(errorTelefono = errorTelefono, errorClabe = errorClabe, errorGeneral = errorGeneral) }
        if (errorTelefono != null || errorClabe != null || errorGeneral != null) return

        perfil.activarVendedor(
            PerfilVendedor(
                telefono = ValidadorVendedor.soloDigitos(telefono),
                puntoEntrega = e.punto!!,
                dias = e.dias.toList(),
                franjas = e.franjas.toList(),
                clabe = ValidadorVendedor.soloDigitos(clabe)
            )
        )
        _estado.update { it.copy(registrado = true) }
    }
}
