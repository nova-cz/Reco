package com.fcc.reco.ui.comprador

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.fcc.reco.R
import com.fcc.reco.data.model.Orden
import com.fcc.reco.databinding.FragmentResultadosBinding
import com.fcc.reco.ui.comun.ChipFiltro
import com.fcc.reco.ui.comun.EspaciadoGrid
import com.fcc.reco.ui.comun.ProductoAdapter
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.comun.dp
import com.fcc.reco.ui.comun.iconoInicio
import com.fcc.reco.ui.comun.irA
import kotlinx.coroutines.launch

class ResultadosFragment : Fragment(R.layout.fragment_resultados) {

    private val viewModel: ResultadosViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentResultadosBinding.bind(view)
        b.raiz.ajustarPorBarrasDelSistema(arriba = true, abajo = true)
        val nav = findNavController()

        b.botonAtras.setOnClickListener { nav.popBackStack() }
        b.botonFavoritos.setOnClickListener { nav.irA(R.id.favoritos) }
        b.buscador.botonFiltros.setOnClickListener { viewModel.limpiar() }
        b.buscador.campoBusqueda.setText(viewModel.consultaInicial)
        b.buscador.campoBusqueda.doAfterTextChanged { viewModel.ponerConsulta(it?.toString().orEmpty()) }
        b.selectorOrden.setOnClickListener { mostrarMenuOrden(it) }
        b.textoCarrera.text = if (viewModel.carreraCorta.isEmpty()) "" else getString(R.string.para_tu_carrera, viewModel.carreraCorta)

        val adaptador = ProductoAdapter(
            alAbrir = { nav.irA(R.id.detalleProducto, Bundle().apply { putInt("productoId", it.id) }) },
            alAlternarFavorito = { viewModel.alternarFavorito(it) }
        )
        b.lista.layoutManager = GridLayoutManager(requireContext(), 2)
        b.lista.addItemDecoration(EspaciadoGrid(2, b.root.dp(12)))
        b.lista.adapter = adaptador

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.estado.collect { pintar(b, it, adaptador) } }
                launch { viewModel.favoritos.collect { adaptador.favoritos = it } }
            }
        }
    }

    private fun pintar(b: FragmentResultadosBinding, estado: EstadoResultados, adaptador: ProductoAdapter) {
        val filtros = estado.filtros
        val categoria = filtros.categoria
        b.titulo.text = if (categoria != null) getString(categoria.nombre) else getString(R.string.resultados_titulo)
        b.subtitulo.text = if (estado.productos.size == 1) getString(R.string.resultado_conteo_uno)
        else getString(R.string.resultados_conteo, estado.productos.size)
        b.buscador.campoBusqueda.hint =
            if (categoria != null) getString(R.string.buscar_en, getString(categoria.nombre)) else getString(R.string.buscar_hint)

        // Chips de filtro: los activos van en negro con "x"; tocar alterna.
        b.grupoFiltros.removeAllViews()
        categoria?.let { agregarChip(b, getString(it.nombre), true) { viewModel.quitarCategoria() } }
        filtros.marca?.let { agregarChip(b, it, true) { viewModel.quitarMarca() } }
        agregarChip(b, getString(R.string.filtro_grado_ab), filtros.soloGradosAB) { viewModel.alternarGrados() }
        agregarChip(b, getString(R.string.filtro_precio_10000), filtros.precioMax != null) { viewModel.alternarPrecio() }
        agregarChip(
            b, getString(R.string.filtro_certificado), filtros.soloCertificados,
            icono = R.drawable.ic_escudo_ok, color = ContextCompat.getColor(requireContext(), R.color.verde_insignia)
        ) { viewModel.alternarCertificado() }

        b.selectorOrden.text = filtros.orden.etiqueta
        b.selectorOrden.iconoFin(R.drawable.ic_chevron_abajo)

        adaptador.submitList(estado.productos)
        b.estadoVacio.visibility = if (estado.productos.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun agregarChip(
        b: FragmentResultadosBinding,
        texto: String,
        activo: Boolean,
        icono: Int = 0,
        color: Int = ContextCompat.getColor(requireContext(), R.color.tinta),
        alTocar: () -> Unit
    ) {
        val chip = ChipFiltro(requireContext()).apply {
            text = texto
            this.activo = activo
            setOnClickListener { alTocar() }
        }
        // Chip inactivo con ícono/acento (p. ej. "Certificado" en verde) usa el constructor con atributos.
        if (!activo && icono != 0) {
            chip.setTextColor(color)
            chip.iconoInicio(icono, 14, color)
        }
        b.grupoFiltros.addView(chip)
    }

    private fun mostrarMenuOrden(ancla: View) {
        PopupMenu(requireContext(), ancla).apply {
            Orden.entries.forEachIndexed { i, orden -> menu.add(0, i, i, orden.etiqueta) }
            setOnMenuItemClickListener { item ->
                viewModel.ponerOrden(Orden.entries[item.itemId])
                true
            }
            show()
        }
    }

    private fun android.widget.TextView.iconoFin(recurso: Int) {
        val icono = androidx.appcompat.content.res.AppCompatResources.getDrawable(context, recurso)?.mutate() ?: return
        val tamano = dp(16)
        icono.setBounds(0, 0, tamano, tamano)
        setCompoundDrawablesRelative(null, null, icono, null)
    }
}
