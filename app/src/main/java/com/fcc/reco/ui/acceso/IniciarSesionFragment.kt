package com.fcc.reco.ui.acceso

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.databinding.FragmentIniciarSesionBinding
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import kotlinx.coroutines.launch

/** Sirve para iniciar sesión y, con `modoRegistro`, para crear cuenta (mismo diseño). */
class IniciarSesionFragment : Fragment(R.layout.fragment_iniciar_sesion) {

    private val viewModel: AccesoViewModel by activityViewModels()
    private var esRegistro = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentIniciarSesionBinding.bind(view)
        b.raiz.ajustarPorBarrasDelSistema()
        esRegistro = arguments?.getBoolean("modoRegistro") ?: false
        aplicarModo(b)

        b.botonAtras.setOnClickListener { findNavController().popBackStack() }
        b.olvidaste.setOnClickListener {
            Toast.makeText(requireContext(), R.string.proximamente, Toast.LENGTH_SHORT).show()
        }
        b.enlaceCambio.setOnClickListener {
            esRegistro = !esRegistro
            viewModel.limpiarError()
            aplicarModo(b)
        }
        b.botonPrincipal.setOnClickListener {
            viewModel.iniciar(b.campoCorreo.texto, b.campoContrasena.texto, esRegistro)
        }
        b.botonGoogle.setOnClickListener { viewModel.iniciarConGoogle() }
        b.campoCorreo.editText.doAfterTextChanged { viewModel.limpiarError() }
        b.campoContrasena.editText.doAfterTextChanged { viewModel.limpiarError() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estado.collect { estado ->
                    val error = estado.error
                    b.campoCorreo.ponerError(
                        error?.takeIf { it.campo == ErrorAcceso.Campo.CORREO }?.let { getString(it.mensaje) }
                    )
                    b.campoContrasena.ponerError(
                        error?.takeIf { it.campo == ErrorAcceso.Campo.CLAVE }?.let { getString(it.mensaje) }
                    )
                    b.botonPrincipal.isEnabled = !estado.cargando
                    b.botonGoogle.isEnabled = !estado.cargando
                    b.botonPrincipal.text = when {
                        estado.cargando -> getString(R.string.entrando)
                        esRegistro -> getString(R.string.crear_cuenta)
                        else -> getString(R.string.iniciar_sesion)
                    }
                }
            }
        }
        recolectarEventosAcceso(viewModel)
    }

    private fun aplicarModo(b: FragmentIniciarSesionBinding) {
        b.titulo.setText(if (esRegistro) R.string.registro_titulo else R.string.login_titulo)
        b.subtitulo.setText(if (esRegistro) R.string.registro_subtitulo else R.string.login_subtitulo)
        b.botonPrincipal.setText(if (esRegistro) R.string.crear_cuenta else R.string.iniciar_sesion)
        b.enlaceCambio.setText(if (esRegistro) R.string.con_cuenta else R.string.sin_cuenta)
        b.olvidaste.visibility = if (esRegistro) View.INVISIBLE else View.VISIBLE
    }
}
