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
import com.fcc.reco.data.model.ModoUsuario
import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.ui.comprador.CompradorActivity
import com.fcc.reco.ui.comun.irA
import com.fcc.reco.ui.vendedor.VendedorActivity
import kotlinx.coroutines.launch

fun NavController.irAIniciarSesion(esRegistro: Boolean) =
    irA(R.id.iniciarSesion, android.os.Bundle().apply { putBoolean("modoRegistro", esRegistro) })

/** Abre CompradorActivity o VendedorActivity y cierra el flujo de acceso. */
fun Activity.abrirModoYTerminar(modo: ModoUsuario) {
    val destino = if (modo == ModoUsuario.COMPRADOR) CompradorActivity::class.java else VendedorActivity::class.java
    startActivity(Intent(this, destino).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
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
                    EventoAcceso.IrAElegirModo ->
                        findNavController().irA(R.id.elegirModo, popHasta = R.id.bienvenida, inclusivo = true)
                    EventoAcceso.IrARegistroVendedor -> findNavController().irA(R.id.registroVendedor)
                    is EventoAcceso.AbrirModo -> requireActivity().abrirModoYTerminar(evento.modo)
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
