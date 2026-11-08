package com.fcc.reco.ui.comprador

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.viewModels
import com.fcc.reco.R
import com.fcc.reco.data.model.Calificacion
import com.fcc.reco.databinding.SheetCalificarBinding
import com.fcc.reco.ui.comun.ChipFiltro
import com.fcc.reco.ui.comun.configurarVendedor
import com.fcc.reco.ui.comun.dp
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/** Hoja para calificar al vendedor después de confirmar la entrega (Figma 33:914). */
class CalificarBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: DetallePedidoViewModel by viewModels(ownerProducer = { requireParentFragment() })

    private var estrellas = 5
    private val etiquetasElegidas = linkedSetOf<String>()

    override fun onCreateView(inflater: LayoutInflater, contenedor: ViewGroup?, estado: Bundle?): View {
        val b = SheetCalificarBinding.inflate(inflater, contenedor, false)
        val pedido = viewModel.estado.value?.pedido
        if (pedido == null) {
            dismiss()
            return b.root
        }

        b.avatar.configurarVendedor(pedido.vendedor)
        b.titulo.text = getString(R.string.calificar_titulo, pedido.vendedor.nombre.substringBefore(' '))

        // Estrellas
        val vistas = (1..5).map { valor ->
            ImageView(requireContext()).apply {
                setImageResource(R.drawable.ic_estrella_llena)
                setPadding(b.root.dp(4), b.root.dp(4), b.root.dp(4), b.root.dp(4))
                contentDescription = valor.toString()
                setOnClickListener {
                    estrellas = valor
                    pintarEstrellas(b)
                }
                b.estrellas.addView(this, ViewGroup.LayoutParams(b.root.dp(44), b.root.dp(44)))
            }
        }
        pintarEstrellas(b, vistas)

        // Etiquetas (se pueden elegir varias)
        Calificacion.ETIQUETAS.forEach { etiqueta ->
            val chip = ChipFiltro(requireContext()).apply {
                text = etiqueta
                cerrarAlActivar = false
                setOnClickListener {
                    activo = !activo
                    if (activo) etiquetasElegidas.add(etiqueta) else etiquetasElegidas.remove(etiqueta)
                }
            }
            b.etiquetas.addView(chip)
        }

        b.botonEnviar.setOnClickListener {
            val enviada = viewModel.calificar(estrellas, etiquetasElegidas.toList(), b.comentario.text.toString())
            if (enviada != null) {
                Toast.makeText(requireContext(), R.string.calificar_gracias, Toast.LENGTH_SHORT).show()
            }
            dismiss()
        }
        return b.root
    }

    private fun pintarEstrellas(b: SheetCalificarBinding, vistas: List<View> = (0 until b.estrellas.childCount).map { b.estrellas.getChildAt(it) }) {
        val llena = ContextCompat.getColor(requireContext(), R.color.estrella)
        val vacia = ContextCompat.getColor(requireContext(), R.color.borde_suave)
        vistas.forEachIndexed { i, v ->
            ImageViewCompat.setImageTintList(v as ImageView, ColorStateList.valueOf(if (i < estrellas) llena else vacia))
        }
    }

    companion object {
        const val TAG = "calificar_sheet"
    }
}
