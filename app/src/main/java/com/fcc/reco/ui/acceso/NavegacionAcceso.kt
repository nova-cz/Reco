package com.fcc.reco.ui.acceso

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.ui.MainActivity
import com.fcc.reco.ui.comun.irA
import kotlinx.coroutines.launch

fun NavController.irAIniciarSesion(esRegistro: Boolean) =
    irA(R.id.iniciarSesion, android.os.Bundle().apply { putBoolean("modoRegistro", esRegistro) })

/** Abre la app (MainActivity) y cierra el flujo de acceso. */
fun Activity.abrirAppYTerminar() {
    startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
    finish()
}

/** Escucha los eventos del ViewModel mientras la pantalla esté visible y los traduce a navegación. */
fun Fragment.recolectarEventosAcceso(viewModel: AccesoViewModel) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.eventos.collect { evento ->
                when (evento) {
                    EventoAcceso.IrAVerificar ->
                        findNavController().irA(R.id.verificarEstudiante, popHasta = R.id.bienvenida)
                    EventoAcceso.MostrarCarrera ->
                        if (parentFragmentManager.findFragmentByTag(CarreraBottomSheet.TAG) == null) {
                            CarreraBottomSheet().show(parentFragmentManager, CarreraBottomSheet.TAG)
                        }
                    EventoAcceso.AbrirApp -> requireActivity().abrirAppYTerminar()
                    is EventoAcceso.Mensaje ->
                        Toast.makeText(requireContext(), evento.texto, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

/** Borra la sesión simulada y vuelve a la Bienvenida. */
fun Activity.cerrarSesionYSalir() {
    SesionLocal(this).borrar()
    startActivity(Intent(this, AccesoActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
    finish()
}
