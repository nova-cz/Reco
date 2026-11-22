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
import com.fcc.reco.databinding.FragmentPublicar2Binding
import com.fcc.reco.domain.ValidadorPublicacion
import com.fcc.reco.ui.comun.ChipInsignia
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.comun.dp
import com.fcc.reco.ui.comun.dpF
import com.fcc.reco.ui.comun.irA
import com.fcc.reco.ui.comun.sumarMargenInferiorDeBarras
import kotlinx.coroutines.launch
import coil3.load
import java.io.File

/** Publicar 2 de 3 · Fotos: de 2 a 8 fotos; la primera es la portada (Figma 48:1778). */
class Publicar2Fragment : Fragment(R.layout.fragment_publicar_2) {

    private val vm: PublicarViewModel by activityViewModels()

    private val selector = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(ValidadorPublicacion.MAX_FOTOS)
    ) { uris: List<Uri> -> if (uris.isNotEmpty()) vm.agregarFotos(uris) }

    private var fotoCamara: Uri? = null
    private val camara = registerForActivityResult(ActivityResultContracts.TakePicture()) { tomada ->
        val uri = fotoCamara
        if (tomada && uri != null) vm.agregarFotos(listOf(uri))
        fotoCamara = null
    }

    private val items = listOf(R.string.pub_chk1, R.string.pub_chk2, R.string.pub_chk3, R.string.pub_chk4, R.string.pub_chk5)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentPublicar2Binding.bind(view)
        b.scroll.ajustarPorBarrasDelSistema(arriba = true, abajo = false)
        b.botonSiguiente.sumarMargenInferiorDeBarras(16)
        b.cabecera.configurar(this, vm, 2, R.string.pub_p2)
        if (vm.borrador.modelo == null) { findNavController().popBackStack(R.id.elegirModelo, false); return }

        b.fotoPrueba.visibility = if (esDebug()) View.VISIBLE else View.GONE
        b.fotoPrueba.setOnClickListener { vm.agregarFotoDePrueba() }
        b.botonSiguiente.setOnClickListener { findNavController().irA(R.id.publicar3) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.estado.collect { borrador ->
                    b.modelo.pintar(this@Publicar2Fragment, borrador)
                    val fotos = borrador.fotos
                    b.contador.text = getString(R.string.pub_fotos_contador, fotos.size, ValidadorPublicacion.MAX_FOTOS)
                    pintarGrilla(b, fotos)
                    pintarLista(b, fotos.size)
                    b.botonSiguiente.isEnabled = ValidadorPublicacion.validarFotos(fotos.size)
                }
            }
        }
    }

    private fun pintarGrilla(b: FragmentPublicar2Binding, fotos: List<String>) {
        b.grillaFotos.removeAllViews()
        val gap = requireContext().dp(10)
        val lado = (resources.displayMetrics.widthPixels - 2 * resources.getDimensionPixelSize(R.dimen.margen_pantalla) - 2 * gap) / 3
        val celdas = maxOf(6, ((fotos.size + 1 + 2) / 3) * 3)
        for (fila in 0 until celdas / 3) {
            val contenedor = LinearLayout(requireContext())
            for (col in 0 until 3) {
                val i = fila * 3 + col
                val celda = when {
                    i < fotos.size -> celdaFoto(fotos[i], i, fotos.size)
                    i == fotos.size && fotos.size < ValidadorPublicacion.MAX_FOTOS -> celdaAgregar()
                    else -> FrameLayout(requireContext()).apply { setBackgroundResource(R.drawable.bg_foto_vacia) }
                }
                contenedor.addView(celda, LinearLayout.LayoutParams(lado, lado).apply { if (col > 0) marginStart = gap })
            }
            b.grillaFotos.addView(contenedor, LinearLayout.LayoutParams(-1, -2).apply { if (fila > 0) topMargin = gap })
        }
    }

    private fun celdaFoto(ruta: String, indice: Int, total: Int): View {
        val ctx = requireContext()
        val portada = indice == 0
        return FrameLayout(ctx).apply {
            setBackgroundResource(R.drawable.bg_foto_placeholder)
            clipToOutline = true
            addView(ImageView(ctx).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                // Las fotos nuevas son archivos del teléfono; las de un anuncio ya enviado son URLs de Storage.
                if (ruta.startsWith("http")) load(ruta) else setImageURI(Uri.fromFile(File(ruta)))
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
                    FrameLayout.LayoutParams(-2, -2, Gravity.TOP or Gravity.START).apply { setMargins(dp(8), dp(8), 0, 0) })
            }
            // Reordenar y elegir portada: ◀ (antes), "Portada" (solo si no lo es) y ▶ (después).
            addView(LinearLayout(ctx).apply {
                gravity = Gravity.CENTER
                setBackgroundColor(Color.argb(110, 0, 0, 0))
                if (indice > 0) addView(botonFoto("◀") { vm.moverFoto(ruta, -1) })
                if (!portada) addView(botonFoto(getString(R.string.pub_hacer_portada), getString(R.string.pub_portada)) { vm.hacerPortada(ruta) })
                if (indice < total - 1) addView(botonFoto("▶") { vm.moverFoto(ruta, 1) })
            }, FrameLayout.LayoutParams(-1, dp(30), Gravity.BOTTOM))
        }
    }

    private fun botonFoto(texto: String, descripcion: String? = null, accion: () -> Unit) = TextView(requireContext()).apply {
        text = texto
        contentDescription = descripcion
        textSize = 12f
        setTextColor(Color.WHITE)
        gravity = Gravity.CENTER
        setPadding(dp(8), 0, dp(8), 0)
        setOnClickListener { accion() }
    }.let { it.apply { layoutParams = LinearLayout.LayoutParams(0, -1, 1f) } }

    /** Galería (Photo Picker, sin permisos) o cámara. */
    private fun elegirOrigen() {
        val opciones = arrayOf(getString(R.string.pub_galeria), getString(R.string.pub_camara))
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.pub_agregar_desde)
            .setItems(opciones) { _, cual ->
                if (cual == 0) selector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                else tomarFoto()
            }
            .show()
    }

    private fun tomarFoto() {
        val ctx = requireContext()
        val carpeta = File(ctx.cacheDir, "camara").apply { mkdirs() }
        val archivo = File(carpeta, "foto_${System.currentTimeMillis()}.jpg")
        val uri = androidx.core.content.FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", archivo)
        fotoCamara = uri
        camara.launch(uri)
    }

    private fun celdaAgregar(): View {
        val ctx = requireContext()
        return LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.bg_foto_agregar)
            isClickable = true
            isFocusable = true
            setOnClickListener { elegirOrigen() }
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

    private fun pintarLista(b: FragmentPublicar2Binding, cantidad: Int) {
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
