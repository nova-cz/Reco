package com.fcc.reco.ui.vendedor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.data.CatalogoModelos
import com.fcc.reco.data.model.Categoria
import com.fcc.reco.databinding.FragmentPublicar1Binding
import com.fcc.reco.databinding.ItemModeloCatalogoBinding
import com.fcc.reco.domain.BorradorAnuncio
import com.fcc.reco.ui.comun.MosaicoCategoria
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.comun.comoPrecio
import com.fcc.reco.ui.comun.dp
import com.fcc.reco.ui.comun.irA
import com.fcc.reco.ui.comun.sumarMargenInferiorDeBarras
import kotlinx.coroutines.launch

/** Publicar 1 de 5: categoría, marca y modelo del catálogo (Figma 8:134). */
class Publicar1Fragment : Fragment(R.layout.fragment_publicar_1) {

    private val vm: PublicarViewModel by activityViewModels()
    private var marcaElegida: String? = null
    private var consulta = ""
    private var categoriaPintada: Categoria? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentPublicar1Binding.bind(view)
        b.scroll.ajustarPorBarrasDelSistema(arriba = true, abajo = false)
        b.botonSiguiente.ajustarMargenInferior()

        if (savedInstanceState == null) {
            val anuncioId = arguments?.getInt("anuncioId") ?: 0
            if (anuncioId > 0) vm.retomar(anuncioId) else if (arguments?.getBoolean("nuevo") == true) vm.nuevo()
        }
        b.cabecera.configurar(this, vm, 1, R.string.pub_p1)

        // Categorías (2 filas de 3) con palomita en la elegida
        val marcas = mutableMapOf<Categoria, View>()
        Categoria.entries.chunked(3).forEachIndexed { fila, grupo ->
            val contenedor = LinearLayout(requireContext())
            grupo.forEachIndexed { i, categoria ->
                val celda = FrameLayout(requireContext())
                celda.addView(MosaicoCategoria(requireContext()).apply {
                    configurar(categoria)
                    setOnClickListener { vm.elegirCategoria(categoria) }
                }, FrameLayout.LayoutParams(-1, -1))
                val check = ImageView(requireContext()).apply {
                    setBackgroundResource(R.drawable.bg_circulo_tinta)
                    setImageResource(R.drawable.ic_check)
                    setPadding(dp(5), dp(5), dp(5), dp(5))
                    androidx.core.widget.ImageViewCompat.setImageTintList(this, android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE))
                    visibility = View.GONE
                }
                celda.addView(check, FrameLayout.LayoutParams(dp(26), dp(26), android.view.Gravity.TOP or android.view.Gravity.END).apply { setMargins(0, dp(8), dp(8), 0) })
                marcas[categoria] = check
                contenedor.addView(celda, LinearLayout.LayoutParams(0, dp(104), 1f).apply { if (i > 0) marginStart = dp(10) })
            }
            b.grillaCategorias.addView(contenedor, LinearLayout.LayoutParams(-1, -2).apply { if (fila > 0) topMargin = dp(10) })
        }

        b.campoBusqueda.addTextChangedListener { consulta = it?.toString().orEmpty(); pintarLista(b, vm.borrador) }
        b.botonSiguiente.setOnClickListener { findNavController().irA(R.id.publicar2) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.estado.collect { borrador ->
                    marcas.forEach { (c, check) -> check.visibility = if (c == borrador.categoria) View.VISIBLE else View.GONE }
                    if (categoriaPintada != borrador.categoria) {
                        categoriaPintada = borrador.categoria
                        marcaElegida = borrador.modelo?.marca
                    }
                    pintarMarcas(b, borrador)
                    pintarLista(b, borrador)
                    b.botonSiguiente.isEnabled = borrador.modelo != null
                }
            }
        }
    }

    private fun pintarMarcas(b: FragmentPublicar1Binding, borrador: BorradorAnuncio) {
        val lista = CatalogoModelos.deCategoria(borrador.categoria).map { it.marca }.distinct()
        if (marcaElegida == null || marcaElegida !in lista) marcaElegida = lista.firstOrNull()
        b.grupoMarcas.llenarUnico(lista, marcaElegida) { marcaElegida = it; pintarMarcas(b, vm.borrador); pintarLista(b, vm.borrador) }
    }

    private fun pintarLista(b: FragmentPublicar1Binding, borrador: BorradorAnuncio) {
        val texto = consulta.trim().lowercase()
        val modelos = CatalogoModelos.deCategoria(borrador.categoria).filter {
            if (texto.isEmpty()) it.marca == marcaElegida else "${it.marca} ${it.modelo}".lowercase().contains(texto)
        }
        b.listaModelos.removeAllViews()
        if (modelos.isEmpty()) {
            b.listaModelos.addView(android.widget.TextView(requireContext()).apply {
                setTextAppearance(R.style.Reco_Texto_Secundario)
                setText(R.string.pub_sin_modelos)
                setPadding(0, dp(16), 0, dp(16))
            })
            return
        }
        val inflater = LayoutInflater.from(requireContext())
        modelos.forEachIndexed { i, m ->
            val fila = ItemModeloCatalogoBinding.inflate(inflater, b.listaModelos, false)
            fila.titulo.text = m.titulo
            fila.referencia.text = getString(R.string.pub_ref, m.precioReferencia.comoPrecio())
            val elegido = borrador.modelo == m
            fila.marcaSeleccion.setBackgroundResource(if (elegido) R.drawable.bg_circulo_tinta else R.drawable.bg_radio_vacio)
            fila.marcaSeleccion.setImageResource(if (elegido) R.drawable.ic_check else 0)
            androidx.core.widget.ImageViewCompat.setImageTintList(fila.marcaSeleccion, android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE))
            fila.root.setOnClickListener { vm.elegirModelo(m) }
            b.listaModelos.addView(fila.root)
            if (i < modelos.lastIndex) b.listaModelos.addView(View(requireContext()).apply { setBackgroundColor(ContextCompat.getColor(context, R.color.borde_suave)) }, LinearLayout.LayoutParams(-1, 1))
        }
    }
}

/** El botón flotante de cada paso respeta la barra de navegación del sistema. */
fun View.ajustarMargenInferior(baseDp: Int = 16) = sumarMargenInferiorDeBarras(baseDp)
