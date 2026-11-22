package com.fcc.reco.ui.acceso

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.data.Repositorios
import com.fcc.reco.data.remote.Nube
import com.fcc.reco.databinding.FragmentBienvenidaBinding
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.vendedor.esDebug

class BienvenidaFragment : Fragment(R.layout.fragment_bienvenida) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentBienvenidaBinding.bind(view)
        b.raiz.ajustarPorBarrasDelSistema()
        b.botonCrear.setOnClickListener { findNavController().irAIniciarSesion(esRegistro = true) }
        b.botonYaTengo.setOnClickListener { findNavController().irAIniciarSesion(esRegistro = false) }
        if (esDebug() && Nube.configurada) agregarSwitchDeDatos(b)
    }

    /** Solo en debug: alterna entre datos simulados (JSON local) y Supabase. Cierra la sesión al cambiar. */
    private fun agregarSwitchDeDatos(b: FragmentBienvenidaBinding) {
        val contexto = requireContext().applicationContext
        val texto = TextView(requireContext()).apply {
            gravity = Gravity.CENTER
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(0xFF6E6E76.toInt())
            setPadding(0, 24, 0, 24)
        }
        fun pintar() {
            texto.text = if (Repositorios.usaNube) "DEBUG · Datos: Supabase  (toca para usar simulados)"
            else "DEBUG · Datos: simulados  (toca para usar Supabase)"
        }
        pintar()
        texto.setOnClickListener {
            val usarNube = !Repositorios.usaNube
            Repositorios.cerrarSesion()
            Nube.fijarUsarNube(contexto, usarNube)
            Repositorios.iniciar(contexto)
            pintar()
        }
        (b.raiz.getChildAt(0) as? LinearLayout)?.addView(texto)
    }
}
