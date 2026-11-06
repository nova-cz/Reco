package com.fcc.reco.ui.comun

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.fcc.reco.R
import com.fcc.reco.databinding.FragmentPlaceholderBinding

/** Pantalla "Próximamente" para las pestañas que aún no se construyen. Recibe el argumento `titulo`. */
class PlaceholderFragment : Fragment(R.layout.fragment_placeholder) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentPlaceholderBinding.bind(view)
        b.raiz.ajustarPorBarrasDelSistema()
        b.titulo.text = arguments?.getString("titulo").orEmpty()
    }
}
