package com.fcc.reco.ui.acceso

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.databinding.FragmentVerificarEstudianteBinding
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema

class VerificarEstudianteFragment : Fragment(R.layout.fragment_verificar_estudiante) {

    private val viewModel: AccesoViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentVerificarEstudianteBinding.bind(view)
        b.raiz.ajustarPorBarrasDelSistema()

        b.botonAtras.setOnClickListener {
            if (!findNavController().popBackStack()) requireActivity().finish()
        }
        // Plan B: portal institucional simulado que regresa por deep link (reco://verificado).
        b.botonInstitucional.setOnClickListener {
            startActivity(Intent(requireContext(), PortalSimuladoActivity::class.java))
        }
        b.enlaceNoAparece.setOnClickListener {
            Toast.makeText(requireContext(), R.string.universidad_no_aparece_msg, Toast.LENGTH_SHORT).show()
        }
        recolectarEventosAcceso(viewModel)
    }
}
