package com.fcc.reco.ui.vendedor

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.data.Repositorios
import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.databinding.FragmentPublicar5Binding
import com.fcc.reco.domain.Comision
import com.fcc.reco.domain.ValidadorPublicacion
import com.fcc.reco.ui.comun.ChipInsignia
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.comun.comoPrecio
import com.fcc.reco.ui.comun.iconoInicio
import com.fcc.reco.ui.comun.irA
import com.fcc.reco.ui.comun.sumarMargenInferiorDeBarras
import kotlinx.coroutines.launch
import java.io.File

/** Publicar 5 de 5: resumen y envío a revisión (Figma 34:558). */
class Publicar5Fragment : Fragment(R.layout.fragment_publicar_5) {

    private val vm: PublicarViewModel by activityViewModels()
    private var confirmado = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentPublicar5Binding.bind(view)
        b.scroll.ajustarPorBarrasDelSistema(arriba = true, abajo = false)
        b.botonSiguiente.sumarMargenInferiorDeBarras(16)
        b.cabecera.configurar(this, vm, 5, R.string.pub_p5)
        val nav = findNavController()
        if (vm.borrador.modelo == null) { nav.popBackStack(R.id.publicar1, false); return }

        b.casillaConfirmo.pintarCasilla(false)
        b.filaConfirmo.setOnClickListener {
            confirmado = !confirmado
            b.casillaConfirmo.pintarCasilla(confirmado)
            b.error.visibility = View.GONE
        }
        b.editar.setOnClickListener { nav.popBackStack(R.id.publicar2, false) }
        b.botonSiguiente.setOnClickListener {
            if (!confirmado) {
                mostrarError(b, R.string.err_confirmar)
                return@setOnClickListener
            }
            val error = vm.enviar()
            if (error != null) {
                mostrarError(b, error.mensaje())
            } else {
                Toast.makeText(requireContext(), R.string.pub_enviado, Toast.LENGTH_LONG).show()
                nav.irA(R.id.anuncios, popHasta = R.id.panel)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.estado.collect { borrador ->
                    val m = borrador.modelo ?: return@collect
                    val esImei = ValidadorPublicacion.requiereImei(borrador.categoria)
                    val titulo = if (borrador.almacenamiento != null) "${m.modelo} · ${borrador.almacenamiento}" else m.modelo
                    b.resumenTitulo.text = titulo
                    b.resumenPrecio.text = borrador.precio.comoPrecio()
                    b.chipGrado.configurar(ChipInsignia.Tipo.GRADO, getString(R.string.grado_corto, borrador.grado.letra))
                    val bateria = borrador.saludBateria
                    if (bateria != null && bateria in 1..100) {
                        b.chipBateria.configurar(ChipInsignia.Tipo.NEUTRO_GRIS, "$bateria%", R.drawable.ic_bateria)
                        b.chipBateria.visibility = View.VISIBLE
                    } else b.chipBateria.visibility = View.GONE
                    borrador.fotos.firstOrNull()?.let { b.resumenFoto.setImageURI(Uri.fromFile(File(it))) }
                        ?: b.resumenFoto.setImageResource(m.categoria.icono)

                    b.detAlmram.text = listOfNotNull(borrador.almacenamiento, borrador.ram).joinToString(" · ").ifEmpty { "—" }
                    b.detColor.text = borrador.color ?: "—"
                    b.detAccesorios.text = borrador.accesorios.joinToString(", ").ifEmpty { "—" }
                    b.detFotos.text = getString(R.string.pub_fotos_n, borrador.fotos.size)
                    val id = borrador.identificador
                    b.detId.text = if (id.length > 8) id.take(4) + "…" + id.takeLast(4) else id
                    b.detId.setTextColor(ContextCompat.getColor(requireContext(), R.color.estado_verde))

                    val punto = SesionLocal(requireContext()).leer()?.vendedor?.puntoEntrega.orEmpty()
                    b.punto.text = punto
                    b.punto.iconoInicio(R.drawable.ic_ubicacion, 20, ContextCompat.getColor(requireContext(), R.color.azul))
                    val comision = Comision.calcular(borrador.precio)
                    val cert = if (borrador.certificar) Comision.CERTIFICACION else 0
                    b.recibes.text = maxOf(0, borrador.precio - comision - cert).comoPrecio()
                }
            }
        }
    }

    private fun mostrarError(b: FragmentPublicar5Binding, mensaje: Int) {
        b.error.setText(mensaje)
        b.error.visibility = View.VISIBLE
    }
}
