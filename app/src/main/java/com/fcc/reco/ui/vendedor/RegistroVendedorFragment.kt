package com.fcc.reco.ui.vendedor

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.data.DatosSimulados
import com.fcc.reco.data.model.ModoUsuario
import com.fcc.reco.databinding.FragmentRegistroVendedorBinding
import com.fcc.reco.ui.acceso.abrirModoYTerminar
import com.fcc.reco.ui.comun.ChipFiltro
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.comun.dp
import com.fcc.reco.ui.comun.dpF
import kotlinx.coroutines.launch

/** Registro de vendedor: teléfono, punto de entrega, horarios, CLABE simulada y reglas (Figma 7:219). */
class RegistroVendedorFragment : Fragment(R.layout.fragment_registro_vendedor) {

    private val viewModel: RegistroVendedorViewModel by viewModels()

    private val dias = listOf("L" to "Lun", "M" to "Mar", "M" to "Mié", "J" to "Jue", "V" to "Vie")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentRegistroVendedorBinding.bind(view)
        b.raiz.ajustarPorBarrasDelSistema()

        b.botonAtras.setOnClickListener { findNavController().popBackStack() }
        b.filaFoto.setOnClickListener {
            Toast.makeText(requireContext(), R.string.proximamente, Toast.LENGTH_SHORT).show()
        }

        // Punto de entrega: menú desplegable con los puntos del campus.
        b.selectorPunto.setOnClickListener {
            PopupMenu(requireContext(), b.selectorPunto).apply {
                DatosSimulados.puntosEntrega.forEachIndexed { i, p -> menu.add(0, i, i, "${p.nombre} · CU") }
                setOnMenuItemClickListener { item ->
                    viewModel.elegirPunto("${DatosSimulados.puntosEntrega[item.itemId].nombre} · CU")
                    true
                }
                show()
            }
        }

        // Días (L M M J V): mismo chip negro/blanco que el resto de la app.
        val chipsDias = dias.map { (letra, clave) ->
            crearChipDia(letra).also { chip ->
                chip.setOnClickListener { viewModel.alternarDia(clave) }
                b.filaDias.addView(chip, android.widget.LinearLayout.LayoutParams(0, b.root.dp(52), 1f).apply {
                    if (b.filaDias.childCount > 0) marginStart = b.root.dp(8)
                })
            }
        }

        // Franjas: Mañana / Tarde
        val franjas = listOf(R.string.franja_manana to "Mañana", R.string.franja_tarde to "Tarde")
        val chipsFranjas = franjas.map { (texto, clave) ->
            ChipFiltro(requireContext()).apply {
                setText(texto)
                cerrarAlActivar = false
                ponerIcono(R.drawable.ic_reloj)
                setOnClickListener { viewModel.alternarFranja(clave) }
                b.filaFranjas.addView(this, android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { if (b.filaFranjas.childCount > 0) marginStart = b.root.dp(8) })
            }
        }

        b.filaReglas.setOnClickListener { viewModel.alternarReglas() }
        b.botonCrear.setOnClickListener { viewModel.crear(b.campoTelefono.texto, b.campoClabe.texto) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estado.collect { e ->
                    if (e.registrado) {
                        requireActivity().abrirModoYTerminar(ModoUsuario.VENDEDOR)
                        return@collect
                    }
                    b.puntoTexto.text = e.punto ?: getString(R.string.registro_punto_hint)
                    b.puntoTexto.setTextColor(
                        ContextCompat.getColor(requireContext(), if (e.punto == null) R.color.texto_secundario else R.color.tinta)
                    )
                    dias.forEachIndexed { i, (_, clave) -> pintarChipDia(chipsDias[i], clave in e.dias) }
                    franjas.forEachIndexed { i, (_, clave) -> chipsFranjas[i].activo = clave in e.franjas }
                    pintarCasilla(b, e.reglas)
                    b.campoTelefono.ponerError(e.errorTelefono?.let { getString(it) })
                    b.campoClabe.ponerError(e.errorClabe?.let { getString(it) })
                    b.error.visibility = if (e.errorGeneral == null) View.GONE else View.VISIBLE
                    e.errorGeneral?.let { b.error.setText(it) }
                }
            }
        }
    }

    private fun crearChipDia(letra: String) = TextView(requireContext()).apply {
        text = letra
        setTextAppearance(R.style.Reco_Texto_Boton)
        gravity = android.view.Gravity.CENTER
        isClickable = true
        isFocusable = true
        elevation = dpF(2f)
    }

    private fun pintarChipDia(chip: TextView, activo: Boolean) {
        chip.setBackgroundResource(if (activo) R.drawable.bg_chip_negro else R.drawable.bg_chip_blanco)
        chip.setTextColor(ContextCompat.getColor(requireContext(), if (activo) R.color.white else R.color.tinta))
    }

    private fun pintarCasilla(b: FragmentRegistroVendedorBinding, marcada: Boolean) {
        val tinta = ContextCompat.getColor(requireContext(), R.color.tinta)
        b.casillaReglas.background = GradientDrawable().apply {
            cornerRadius = b.root.dpF(7f)
            if (marcada) setColor(tinta) else {
                setColor(Color.WHITE)
                setStroke(b.root.dp(2), ContextCompat.getColor(requireContext(), R.color.borde_suave))
            }
        }
        b.casillaReglas.setImageResource(if (marcada) R.drawable.ic_check else 0)
        ImageViewCompat.setImageTintList(b.casillaReglas, android.content.res.ColorStateList.valueOf(Color.WHITE))
    }
}
