package com.fcc.reco.ui.vendedor

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.databinding.FragmentPublicar4Binding
import com.fcc.reco.domain.Comision
import com.fcc.reco.domain.ValidadorPublicacion
import com.fcc.reco.domain.ValidadorPublicacion.ResultadoPrecio
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.comun.comoPrecio
import com.fcc.reco.ui.comun.irA
import com.fcc.reco.ui.comun.sumarMargenInferiorDeBarras
import kotlinx.coroutines.launch

/** Publicar 4 de 5: precio, rango permitido, lo que recibes y certificación opcional (Figma 34:387). */
class Publicar4Fragment : Fragment(R.layout.fragment_publicar_4) {

    private val vm: PublicarViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentPublicar4Binding.bind(view)
        b.scroll.ajustarPorBarrasDelSistema(arriba = true, abajo = false)
        b.botonSiguiente.sumarMargenInferiorDeBarras(16)
        b.cabecera.configurar(this, vm, 4, R.string.pub_p4)
        val modelo = vm.borrador.modelo
        if (modelo == null) { findNavController().popBackStack(R.id.publicar1, false); return }

        if (vm.borrador.precio > 0) b.campoPrecio.setText(vm.borrador.precio.toString())
        b.interruptorCertificar.isChecked = vm.borrador.certificar
        b.campoPrecio.addTextChangedListener { vm.precio(it?.toString()?.toIntOrNull() ?: 0) }
        b.interruptorCertificar.setOnCheckedChangeListener { _, marcado -> vm.certificar(marcado) }
        b.botonSiguiente.setOnClickListener { findNavController().irA(R.id.publicar5) }

        val rango = ValidadorPublicacion.rangoPermitido(modelo.precioReferencia)
        b.refTitulo.text = getString(R.string.pub_ref, modelo.precioReferencia.comoPrecio())
        b.rangoMin.text = rango.first.comoPrecio()
        b.rangoMax.text = rango.last.comoPrecio()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.estado.collect { borrador ->
                    val precio = borrador.precio
                    b.refGrado.text = getString(R.string.detalle_grado, borrador.grado.letra)
                    b.barraRango.configurar(rango.first, rango.last, precio, modelo.precioReferencia)
                    when (ValidadorPublicacion.evaluarPrecio(precio, modelo.precioReferencia)) {
                        ResultadoPrecio.INVALIDO -> b.avisoPrecio.pintarAviso(getString(R.string.pub_precio_vacio), R.color.estado_gris, R.color.estado_gris_fondo, R.drawable.ic_info)
                        ResultadoPrecio.DENTRO_DE_RANGO -> b.avisoPrecio.pintarAviso(getString(R.string.pub_precio_ok), R.color.estado_verde, R.color.estado_verde_fondo, R.drawable.ic_check)
                        ResultadoPrecio.REVISION_MANUAL -> b.avisoPrecio.pintarAviso(
                            getString(R.string.pub_precio_fuera, rango.first.comoPrecio(), rango.last.comoPrecio()),
                            R.color.estado_naranja, R.color.estado_naranja_fondo, R.drawable.ic_info
                        )
                    }
                    val comision = if (precio > 0) Comision.calcular(precio) else 0
                    val certificacion = if (borrador.certificar) Comision.CERTIFICACION else 0
                    b.linPrecio.text = precio.comoPrecio()
                    b.linComision.text = "−" + comision.comoPrecio()
                    b.linComision.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.estado_rojo))
                    b.filaCertificacion.visibility = if (borrador.certificar) View.VISIBLE else View.GONE
                    b.linCertificacion.text = "−" + certificacion.comoPrecio()
                    b.linRecibes.text = maxOf(0, precio - comision - certificacion).comoPrecio()
                    b.botonSiguiente.isEnabled = precio > 0
                }
            }
        }
    }
}
