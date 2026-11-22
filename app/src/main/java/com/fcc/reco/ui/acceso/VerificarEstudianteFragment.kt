package com.fcc.reco.ui.acceso

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.data.Repositorios
import com.fcc.reco.data.remote.Nube
import com.fcc.reco.databinding.FragmentVerificarEstudianteBinding
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import kotlinx.coroutines.launch

class VerificarEstudianteFragment : Fragment(R.layout.fragment_verificar_estudiante) {

    private val viewModel: AccesoViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentVerificarEstudianteBinding.bind(view)
        b.raiz.ajustarPorBarrasDelSistema()

        b.botonAtras.setOnClickListener {
            if (!findNavController().popBackStack()) requireActivity().finish()
        }
        if (Repositorios.usaNube && !Nube.verificacionRealActiva) {
            // Supabase sin Azure configurado: no hay con qué verificar todavía, así que no se muestran botones que fallen.
            b.botonInstitucional.visibility = View.GONE
            b.botonCodigo.visibility = View.GONE
            b.enlaceNoAparece.setText(R.string.verificacion_pronto)
            b.enlaceNoAparece.setOnClickListener(null)
        } else if (Repositorios.usaNube) {
            // Verificación real: Microsoft (la BUAP usa Microsoft 365) y, como plan B, un código al correo.
            b.botonInstitucional.text = getString(R.string.verificar_con_buap)
            b.botonInstitucional.setOnClickListener { viewModel.verificarConMicrosoft() }
            b.botonCodigo.visibility = View.VISIBLE
            b.botonCodigo.setOnClickListener { pedirCorreo() }
        } else {
            // Datos simulados (switch de debug): portal institucional simulado que regresa por reco://verificado.
            b.botonInstitucional.setOnClickListener {
                startActivity(Intent(requireContext(), PortalSimuladoActivity::class.java))
            }
        }
        if (!Repositorios.usaNube || Nube.verificacionRealActiva) {
            b.enlaceNoAparece.setOnClickListener {
                Toast.makeText(requireContext(), R.string.universidad_no_aparece_msg, Toast.LENGTH_SHORT).show()
            }
        }
        recolectarEventosAcceso(viewModel)
    }

    /** Plan B, paso 1: correo institucional. */
    private fun pedirCorreo() {
        val campo = campoTexto(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, "matricula@alumno.buap.mx")
        val dialogo = AlertDialog.Builder(requireContext())
            .setTitle(R.string.codigo_titulo)
            .setMessage(R.string.codigo_pide_correo)
            .setView(envolver(campo))
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.codigo_enviar, null)
            .create()
        dialogo.setOnShowListener {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { boton ->
                val correo = campo.text.toString().trim()
                boton.isEnabled = false
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.enviarCodigo(correo).fold(
                        onSuccess = { dialogo.dismiss(); pedirCodigo(correo) },
                        onFailure = {
                            boton.isEnabled = true
                            val mensaje = if (it.message == "dominio") R.string.error_dominio else R.string.error_codigo_envio
                            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }
        }
        dialogo.show()
    }

    /** Plan B, paso 2: el código que llegó al correo. */
    private fun pedirCodigo(correo: String) {
        val campo = campoTexto(InputType.TYPE_CLASS_NUMBER, "123456")
        val dialogo = AlertDialog.Builder(requireContext())
            .setTitle(R.string.codigo_titulo)
            .setMessage(getString(R.string.codigo_pide_codigo, correo))
            .setView(envolver(campo))
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.codigo_verificar, null)
            .create()
        dialogo.setOnShowListener {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { boton ->
                boton.isEnabled = false
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.confirmarCodigo(correo, campo.text.toString()).fold(
                        onSuccess = { dialogo.dismiss() },
                        onFailure = {
                            boton.isEnabled = true
                            Toast.makeText(requireContext(), R.string.error_codigo, Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }
        }
        dialogo.show()
    }

    private fun campoTexto(tipo: Int, pista: String) = EditText(requireContext()).apply {
        inputType = tipo
        hint = pista
        setSingleLine()
    }

    private fun envolver(campo: EditText) = FrameLayout(requireContext()).apply {
        val m = (24 * resources.displayMetrics.density).toInt()
        setPadding(m, 8, m, 0)
        addView(campo)
    }
}
