package com.fcc.reco.ui.acceso

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.data.model.Categoria
import com.fcc.reco.databinding.FragmentBienvenidaBinding
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema

class BienvenidaFragment : Fragment(R.layout.fragment_bienvenida) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentBienvenidaBinding.bind(view)
        b.raiz.ajustarPorBarrasDelSistema()

        listOf(b.m1, b.m2, b.m3, b.m4, b.m5, b.m6).forEachIndexed { i, mosaico ->
            mosaico.configurar(Categoria.entries[i])
            mosaico.ponerDecorativo()
        }
        b.botonCrear.setOnClickListener { findNavController().irAIniciarSesion(esRegistro = true) }
        b.botonYaTengo.setOnClickListener { findNavController().irAIniciarSesion(esRegistro = false) }
    }
}
