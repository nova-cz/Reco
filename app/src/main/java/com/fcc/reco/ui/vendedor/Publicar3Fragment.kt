package com.fcc.reco.ui.vendedor

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.databinding.FragmentPublicar3Binding
import com.fcc.reco.domain.Comision
import com.fcc.reco.domain.ValidadorPublicacion
import com.fcc.reco.domain.ValidadorPublicacion.ResultadoPrecio
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.comun.comoPrecio
import com.fcc.reco.ui.comun.sumarMargenInferiorDeBarras
import kotlinx.coroutines.launch

/** Publicar 3 de 3 · Precio y publicar: precio, rango sugerido, lo que recibes y "Enviar a revisión" (Figma 48:1873). */
class Publicar3Fragment : Fragment(R.layout.fragment_publicar_3) {

    private val vm: PublicarViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentPublicar3Binding.bind(view)
        b.scroll.ajustarPorBarrasDelSistema(arriba = true, abajo = false)
        b.botonSiguiente.sumarMargenInferiorDeBarras(16)
        b.cabecera.configurar(this, vm, 3, R.string.pub_p3)
        val nav = findNavController()
        val modelo = vm.borrador.modelo
        if (modelo == null) { nav.popBackStack(R.id.elegirModelo, false); return }

        if (vm.borrador.precio > 0) b.campoPrecio.setText(vm.borrador.precio.toString())
        b.campoPrecio.addTextChangedListener { vm.precio(it?.toString()?.toIntOrNull() ?: 0) }
        b.botonSiguiente.setOnClickListener {
            val error = vm.enviar()
            if (error != null) {
                Toast.makeText(requireContext(), error.mensaje(), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), R.string.pub_enviado, Toast.LENGTH_LONG).show()
                // El asistente siempre se abre desde Mis ventas: se vuelve ahí con el anuncio En revisión.
                nav.popBackStack(R.id.misVentas, false)
            }
        }

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
                    b.linPrecio.text = precio.comoPrecio()
                    b.linComision.text = "−" + comision.comoPrecio()
                    b.linComision.setTextColor(ContextCompat.getColor(requireContext(), R.color.estado_rojo))
                    b.linRecibes.text = maxOf(0, precio - comision).comoPrecio()
                    b.botonSiguiente.isEnabled = precio > 0
                }
            }
        }
    }
}
