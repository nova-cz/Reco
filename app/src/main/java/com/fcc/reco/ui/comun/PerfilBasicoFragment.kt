package com.fcc.reco.ui.comun

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.fcc.reco.R
import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.data.model.ModoUsuario
import com.fcc.reco.data.repository.PerfilRepositorySimulado
import com.fcc.reco.databinding.FragmentPerfilBasicoBinding
import com.fcc.reco.ui.acceso.abrirModoYTerminar
import com.fcc.reco.ui.acceso.cerrarSesionYSalir
import com.fcc.reco.ui.vendedor.VendedorActivity

/** Perfil mínimo (datos de la sesión, cambiar de modo y cerrar sesión) mientras se diseña el real. */
class PerfilBasicoFragment : Fragment(R.layout.fragment_perfil_basico) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentPerfilBasicoBinding.bind(view)
        b.raiz.ajustarPorBarrasDelSistema()

        val local = SesionLocal(requireContext())
        val sesion = local.leer()
        b.avatar.configurarConNombre(
            sesion?.nombre.orEmpty(),
            ContextCompat.getColor(requireContext(), R.color.azul),
            ContextCompat.getColor(requireContext(), R.color.rosa)
        )
        b.nombre.text = sesion?.nombre.orEmpty()
        b.detalle.text = listOfNotNull(sesion?.correo, sesion?.carrera, sesion?.universidad).joinToString("\n")

        val enModoVendedor = requireActivity() is VendedorActivity
        val otroModo = if (enModoVendedor) ModoUsuario.COMPRADOR else ModoUsuario.VENDEDOR
        b.botonCambiar.setText(if (enModoVendedor) R.string.cambiar_a_comprador else R.string.cambiar_a_vendedor)
        b.botonCambiar.setOnClickListener {
            PerfilRepositorySimulado(local).cambiarModo(otroModo)
            requireActivity().abrirModoYTerminar(otroModo)
        }
        b.botonSalir.setOnClickListener { requireActivity().cerrarSesionYSalir() }
    }
}
