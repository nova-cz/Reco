package com.fcc.reco.ui.acceso

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.fcc.reco.R
import com.fcc.reco.data.model.ModoUsuario
import com.fcc.reco.databinding.FragmentElegirModoBinding
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema

class ElegirModoFragment : Fragment(R.layout.fragment_elegir_modo) {

    private val viewModel: AccesoViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentElegirModoBinding.bind(view)
        b.raiz.ajustarPorBarrasDelSistema()
        b.tarjetaComprar.setOnClickListener { viewModel.elegirModo(ModoUsuario.COMPRADOR) }
        b.tarjetaVender.setOnClickListener { viewModel.elegirModo(ModoUsuario.VENDEDOR) }
        recolectarEventosAcceso(viewModel)
    }
}
