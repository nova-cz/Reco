package com.fcc.reco.ui.comprador

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.data.model.Categoria
import com.fcc.reco.data.model.Producto
import com.fcc.reco.databinding.FragmentInicioBinding
import com.fcc.reco.ui.comun.ChipInsignia
import com.fcc.reco.ui.comun.CuadriculaProductos
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.comun.comoPrecio
import com.fcc.reco.ui.comun.dp
import com.fcc.reco.ui.comun.dpF
import com.fcc.reco.ui.comun.irA
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class InicioFragment : Fragment(R.layout.fragment_inicio) {

    private val viewModel: InicioViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentInicioBinding.bind(view)
        b.raiz.ajustarPorBarrasDelSistema(arriba = true, abajo = true)
        val nav = findNavController()

        fun irAResultados(categoria: Categoria? = null, marca: String? = null, consulta: String? = null) {
            nav.irA(R.id.resultados, Bundle().apply {
                categoria?.let { putString("categoria", it.name) }
                marca?.let { putString("marca", it) }
                consulta?.let { putString("consulta", it) }
            })
        }

        // Categorías
        listOf(b.m1, b.m2, b.m3, b.m4, b.m5, b.m6).forEachIndexed { i, mosaico ->
            val categoria = Categoria.entries[i]
            mosaico.configurar(categoria)
            mosaico.setOnClickListener { irAResultados(categoria = categoria) }
        }

        // Búsqueda y accesos
        b.buscador.campoBusqueda.setOnEditorActionListener { campo, accion, _ ->
            if (accion == EditorInfo.IME_ACTION_SEARCH) {
                irAResultados(consulta = campo.text.toString())
                true
            } else false
        }
        b.buscador.botonFiltros.setOnClickListener { irAResultados() }
        b.botonNotificaciones.setOnClickListener {
            Toast.makeText(requireContext(), R.string.proximamente, Toast.LENGTH_SHORT).show()
        }
        b.verTodoMarcas.setOnClickListener { irAResultados() }
        b.verTodoCampus.setOnClickListener { irAResultados() }

        // En tu campus: se rearma cuando cambian los datos o los favoritos.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estado.combine(viewModel.favoritos) { estado, favoritos -> estado to favoritos }
                    .collect { (estado, favoritos) ->
                        pintar(b, estado)
                        CuadriculaProductos.mostrar(
                            b.listaCampus, estado.enTuCampus, favoritos,
                            alAbrir = { nav.irA(R.id.detalleProducto, Bundle().apply { putInt("productoId", it.id) }) },
                            alAlternarFavorito = { viewModel.alternarFavorito(it) }
                        )
                    }
            }
        }
    }

    private fun pintar(b: FragmentInicioBinding, estado: EstadoInicio) {
        val ctx = requireContext()
        b.avatar.configurarConNombre(
            estado.nombre,
            ContextCompat.getColor(ctx, R.color.azul),
            ContextCompat.getColor(ctx, R.color.verde_reco)
        )
        b.saludo.text = getString(R.string.hola_usuario, estado.nombre.substringBefore(' '))
        b.verificado.text = getString(R.string.verificado_universidad, estado.universidad)

        val destacado = estado.destacado
        b.tarjetaDestacada.visibility = if (destacado == null) View.GONE else View.VISIBLE
        if (destacado != null) {
            b.destacadoTitulo.text = destacado.modelo
            b.destacadoSpecs.text = listOfNotNull(destacado.ram, destacado.almacenamiento).joinToString(" · ")
            b.destacadoIcono.setImageResource(destacado.categoria.icono)
            b.destacadoPrecio.text = destacado.precio.comoPrecio()
            b.destacadoCertificado.visibility = if (destacado.certificado) View.VISIBLE else View.GONE
            b.destacadoCertificado.configurar(ChipInsignia.Tipo.CERTIFICADO, getString(R.string.certificado))
            b.tarjetaDestacada.setOnClickListener { abrirDetalle(destacado) }
        }

        // Marcas: la primera va resaltada en negro, como en el Figma.
        b.filaMarcas.removeAllViews()
        estado.marcas.forEachIndexed { indice, marca ->
            b.filaMarcas.addView(crearChipMarca(marca, resaltada = indice == 0), android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, b.root.dp(52)
            ).apply { marginEnd = b.root.dp(10) })
        }
    }

    private fun crearChipMarca(marca: String, resaltada: Boolean): TextView = TextView(requireContext()).apply {
        text = marca
        setTextAppearance(R.style.Reco_Texto_Boton)
        gravity = android.view.Gravity.CENTER
        setPadding(dp(26), 0, dp(26), 0)
        isClickable = true
        isFocusable = true
        setBackgroundResource(if (resaltada) R.drawable.bg_chip_negro else R.drawable.bg_chip_blanco)
        setTextColor(ContextCompat.getColor(context, if (resaltada) R.color.white else R.color.tinta))
        elevation = dpF(2f)
        setOnClickListener {
            findNavController().irA(R.id.resultados, Bundle().apply { putString("marca", marca) })
        }
    }

    private fun abrirDetalle(producto: Producto) {
        findNavController().irA(R.id.detalleProducto, Bundle().apply { putInt("productoId", producto.id) })
    }
}
