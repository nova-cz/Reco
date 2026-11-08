package com.fcc.reco.ui.vendedor

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.data.model.Categoria
import com.fcc.reco.data.model.Grado
import com.fcc.reco.databinding.FragmentPublicar2Binding
import com.fcc.reco.domain.BorradorAnuncio
import com.fcc.reco.domain.ValidadorPublicacion
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.comun.dp
import com.fcc.reco.ui.comun.irA
import com.fcc.reco.ui.comun.sumarMargenInferiorDeBarras
import kotlinx.coroutines.launch

/** Publicar 2 de 5: almacenamiento, RAM, color, grado, batería, IMEI o serie y accesorios (Figma 8:257). */
class Publicar2Fragment : Fragment(R.layout.fragment_publicar_2) {

    private val vm: PublicarViewModel by activityViewModels()
    private val accesorios = listOf("Cargador", "Caja", "Funda", "Cable")
    private val categoriasConBateria = setOf(Categoria.LAPTOPS, Categoria.CELULARES, Categoria.TABLETS, Categoria.AUDIO)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentPublicar2Binding.bind(view)
        b.scroll.ajustarPorBarrasDelSistema(arriba = true, abajo = false)
        b.botonSiguiente.sumarMargenInferiorDeBarras(16)
        b.cabecera.configurar(this, vm, 2, R.string.pub_p2)

        val inicial = vm.borrador
        if (inicial.modelo == null) {
            findNavController().popBackStack()
            return
        }
        val esImei = ValidadorPublicacion.requiereImei(inicial.categoria)
        b.etiquetaId.setText(if (esImei) R.string.pub_imei else R.string.pub_serie)
        b.campoId.editText.hint = if (esImei) "490154203237518" else "C02FQ6LR9ABC"
        b.campoId.editText.inputType = if (esImei) android.text.InputType.TYPE_CLASS_NUMBER
            else android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
        b.bloqueBateria.visibility = if (inicial.categoria in categoriasConBateria) View.VISIBLE else View.GONE
        if (inicial.saludBateria != null) b.campoBateria.texto = inicial.saludBateria.toString()
        b.campoId.texto = inicial.identificador

        b.campoBateria.editText.addTextChangedListener {
            vm.saludBateria(it?.toString()?.toIntOrNull())
            b.campoBateria.ponerError(null)
        }
        b.campoId.editText.addTextChangedListener {
            vm.identificador(it?.toString().orEmpty())
            pintarEstadoId(b, esImei, mostrarVacio = false)
        }
        pintarEstadoId(b, esImei, mostrarVacio = false)

        // Grados A / B / C
        val descripciones = mapOf(Grado.A to R.string.grado_a_desc, Grado.B to R.string.grado_b_desc, Grado.C to R.string.grado_c_desc)
        val tarjetas = Grado.entries.map { grado ->
            val tarjeta = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), dp(12), dp(12), dp(12))
                isClickable = true
                isFocusable = true
                setOnClickListener { vm.grado(grado) }
            }
            tarjeta.addView(TextView(requireContext()).apply {
                text = grado.letra
                gravity = android.view.Gravity.CENTER
                setTextAppearance(R.style.Reco_Texto_Boton)
                setBackgroundResource(R.drawable.bg_icono_gris)
            }, LinearLayout.LayoutParams(dp(32), dp(32)))
            tarjeta.addView(TextView(requireContext()).apply {
                text = grado.etiqueta
                setTextAppearance(R.style.Reco_Texto_Boton)
                textSize = 15f
            }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(10) })
            tarjeta.addView(TextView(requireContext()).apply {
                setText(descripciones.getValue(grado))
                setTextAppearance(R.style.Reco_Texto_Secundario)
                textSize = 11f
            })
            b.filaGrados.addView(tarjeta, LinearLayout.LayoutParams(0, -2, 1f).apply { if (grado != Grado.A) marginStart = dp(8) })
            grado to tarjeta
        }

        b.modelo.modeloCambiar.setOnClickListener { findNavController().popBackStack(R.id.publicar1, false) }
        b.botonSiguiente.setOnClickListener {
            var ok = true
            val bateria = vm.borrador.saludBateria
            if (b.bloqueBateria.visibility == View.VISIBLE && (bateria == null || bateria !in 1..100)) {
                b.campoBateria.ponerError(getString(R.string.err_bateria)); ok = false
            }
            if (!pintarEstadoId(b, esImei, mostrarVacio = true)) ok = false
            if (ok) findNavController().irA(R.id.publicar3)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.estado.collect { borrador ->
                    val m = borrador.modelo ?: return@collect
                    b.modelo.pintar(this@Publicar2Fragment, borrador)
                    b.bloqueAlm.visibility = if (m.almacenamientos.isEmpty()) View.GONE else View.VISIBLE
                    b.bloqueRam.visibility = if (m.rams.isEmpty()) View.GONE else View.VISIBLE
                    b.grupoAlm.llenarUnico(m.almacenamientos, borrador.almacenamiento) { vm.almacenamiento(it) }
                    b.grupoRam.llenarUnico(m.rams, borrador.ram) { vm.ram(it) }
                    b.grupoColor.llenarUnico(m.colores, borrador.color) { vm.color(it) }
                    b.grupoAccesorios.llenarMultiple(accesorios, borrador.accesorios) { vm.alternarAccesorio(it) }
                    tarjetas.forEach { (grado, tarjeta) -> pintarGrado(tarjeta, grado == borrador.grado) }
                }
            }
        }
    }

    private fun pintarGrado(tarjeta: LinearLayout, activo: Boolean) {
        tarjeta.setBackgroundResource(if (activo) R.drawable.bg_tarjeta_negra else R.drawable.bg_tarjeta)
        val blanco = ContextCompat.getColor(requireContext(), R.color.white)
        val tinta = ContextCompat.getColor(requireContext(), R.color.tinta)
        val gris = ContextCompat.getColor(requireContext(), R.color.texto_secundario)
        (tarjeta.getChildAt(1) as TextView).setTextColor(if (activo) blanco else tinta)
        (tarjeta.getChildAt(2) as TextView).setTextColor(if (activo) 0xCCFFFFFF.toInt() else gris)
    }

    /** Muestra si el IMEI/serie es válido. Devuelve verdadero si no hay error. */
    private fun pintarEstadoId(b: FragmentPublicar2Binding, esImei: Boolean, mostrarVacio: Boolean): Boolean {
        val error = vm.errorIdentificador()
        val vacio = vm.borrador.identificador.isEmpty()
        val texto = b.estadoId
        if (vacio && !mostrarVacio) { texto.visibility = View.GONE; return error == null }
        texto.visibility = View.VISIBLE
        if (error == null) {
            texto.pintarAviso(getString(if (esImei) R.string.pub_id_ok_imei else R.string.pub_id_ok_serie), R.color.estado_verde, R.color.estado_verde_fondo, R.drawable.ic_check)
        } else {
            texto.pintarAviso(getString(error.mensaje(esImei)), R.color.estado_rojo, R.color.estado_rojo_fondo, R.drawable.ic_info)
        }
        return error == null
    }
}
