package com.fcc.reco.ui.vendedor

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.databinding.FragmentPublicar3Binding
import com.fcc.reco.domain.ValidadorPublicacion
import com.fcc.reco.ui.comun.ChipInsignia
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.comun.dp
import com.fcc.reco.ui.comun.dpF
import com.fcc.reco.ui.comun.irA
import com.fcc.reco.ui.comun.sumarMargenInferiorDeBarras
import kotlinx.coroutines.launch
import java.io.File

/** Publicar 3 de 5: de 2 a 8 fotos; la primera es la portada (Figma 34:200). */
class Publicar3Fragment : Fragment(R.layout.fragment_publicar_3) {

    private val vm: PublicarViewModel by activityViewModels()

    private val selector = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(ValidadorPublicacion.MAX_FOTOS)
    ) { uris: List<Uri> -> if (uris.isNotEmpty()) vm.agregarFotos(uris) }

    private val items = listOf(R.string.pub_chk1, R.string.pub_chk2, R.string.pub_chk3, R.string.pub_chk4, R.string.pub_chk5)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentPublicar3Binding.bind(view)
        b.scroll.ajustarPorBarrasDelSistema(arriba = true, abajo = false)
        b.botonSiguiente.sumarMargenInferiorDeBarras(16)
        b.cabecera.configurar(this, vm, 3, R.string.pub_p3)
        if (vm.borrador.modelo == null) { findNavController().popBackStack(); return }

        b.fotoPrueba.visibility = if (esDebug()) View.VISIBLE else View.GONE
        b.fotoPrueba.setOnClickListener { vm.agregarFotoDePrueba() }
        b.botonSiguiente.setOnClickListener { findNavController().irA(R.id.publicar4) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.estado.collect { borrador ->
                    b.modelo.pintar(this@Publicar3Fragment, borrador)
                    val fotos = borrador.fotos
                    b.contador.text = getString(R.string.pub_fotos_contador, fotos.size, ValidadorPublicacion.MAX_FOTOS)
                    pintarGrilla(b, fotos)
                    pintarLista(b, fotos.size)
                    b.botonSiguiente.isEnabled = ValidadorPublicacion.validarFotos(fotos.size)
                }
            }
        }
    }

    private fun pintarGrilla(b: FragmentPublicar3Binding, fotos: List<String>) {
        b.grillaFotos.removeAllViews()
        val gap = requireContext().dp(10)
        val lado = (resources.displayMetrics.widthPixels - 2 * resources.getDimensionPixelSize(R.dimen.margen_pantalla) - 2 * gap) / 3
        val celdas = maxOf(6, ((fotos.size + 1 + 2) / 3) * 3)
        for (fila in 0 until celdas / 3) {
            val contenedor = LinearLayout(requireContext())
            for (col in 0 until 3) {
                val i = fila * 3 + col
                val celda = when {
                    i < fotos.size -> celdaFoto(fotos[i], i == 0)
                    i == fotos.size && fotos.size < ValidadorPublicacion.MAX_FOTOS -> celdaAgregar()
                    else -> FrameLayout(requireContext()).apply { setBackgroundResource(R.drawable.bg_foto_vacia) }
                }
                contenedor.addView(celda, LinearLayout.LayoutParams(lado, lado).apply { if (col > 0) marginStart = gap })
            }
            b.grillaFotos.addView(contenedor, LinearLayout.LayoutParams(-1, -2).apply { if (fila > 0) topMargin = gap })
        }
    }

    private fun celdaFoto(ruta: String, portada: Boolean): View {
        val ctx = requireContext()
        return FrameLayout(ctx).apply {
            setBackgroundResource(R.drawable.bg_foto_placeholder)
            clipToOutline = true
            addView(ImageView(ctx).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageURI(Uri.fromFile(File(ruta)))
            }, FrameLayout.LayoutParams(-1, -1))
            addView(ImageView(ctx).apply {
                setBackgroundResource(R.drawable.bg_circulo_tinta)
                setImageResource(R.drawable.ic_cerrar)
                setPadding(dp(5), dp(5), dp(5), dp(5))
                ImageViewCompat.setImageTintList(this, android.content.res.ColorStateList.valueOf(Color.WHITE))
                contentDescription = getString(R.string.cerrar)
                setOnClickListener { vm.quitarFoto(ruta) }
            }, FrameLayout.LayoutParams(dp(26), dp(26), Gravity.TOP or Gravity.END).apply { setMargins(0, dp(6), dp(6), 0) })
            if (portada) {
                addView(ChipInsignia(ctx).apply { configurar(ChipInsignia.Tipo.GRADO, getString(R.string.pub_portada)) },
                    FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM or Gravity.START).apply { setMargins(dp(8), 0, 0, dp(8)) })
            }
        }
    }

    private fun celdaAgregar(): View {
        val ctx = requireContext()
        return LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.bg_foto_agregar)
            isClickable = true
            isFocusable = true
            setOnClickListener { selector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            addView(ImageView(ctx).apply {
                setBackgroundResource(R.drawable.bg_circulo_naranja)
                setImageResource(R.drawable.ic_mas)
                setPadding(dp(8), dp(8), dp(8), dp(8))
                ImageViewCompat.setImageTintList(this, android.content.res.ColorStateList.valueOf(Color.WHITE))
            }, LinearLayout.LayoutParams(dp(36), dp(36)))
            addView(TextView(ctx).apply {
                setText(R.string.pub_agregar)
                setTextAppearance(R.style.Reco_Texto_Boton)
                textSize = 13f
                setTextColor(ContextCompat.getColor(ctx, R.color.naranja))
            }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(6) })
        }
    }

    private fun pintarLista(b: FragmentPublicar3Binding, cantidad: Int) {
        while (b.listaPiden.childCount > 1) b.listaPiden.removeViewAt(1)
        items.forEachIndexed { i, texto ->
            val hecho = cantidad > i
            b.listaPiden.addView(LinearLayout(requireContext()).apply {
                gravity = Gravity.CENTER_VERTICAL
                addView(ImageView(context).apply {
                    if (hecho) {
                        setBackgroundResource(R.drawable.bg_circulo_verde)
                        setImageResource(R.drawable.ic_check)
                        ImageViewCompat.setImageTintList(this, android.content.res.ColorStateList.valueOf(Color.WHITE))
                    } else setBackgroundResource(R.drawable.bg_radio_vacio)
                    setPadding(dp(4), dp(4), dp(4), dp(4))
                }, LinearLayout.LayoutParams(dp(24), dp(24)))
                addView(TextView(context).apply {
                    setText(texto)
                    setTextAppearance(R.style.Reco_Texto_Cuerpo)
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(context, if (hecho) R.color.tinta else R.color.texto_secundario))
                }, LinearLayout.LayoutParams(-2, -2).apply { marginStart = dp(12) })
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, -2).apply { topMargin = dp(8) })
        }
    }
}
