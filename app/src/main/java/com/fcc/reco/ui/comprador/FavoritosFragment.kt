package com.fcc.reco.ui.comprador

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.databinding.FragmentFavoritosBinding
import com.fcc.reco.ui.comun.CuadriculaProductos
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.comun.irA
import kotlinx.coroutines.launch

/** Favoritos: equipos guardados, con filtro Todos / Disponibles / Vendidos (Figma 33:156). */
class FavoritosFragment : Fragment(R.layout.fragment_favoritos) {

    private val viewModel: FavoritosViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentFavoritosBinding.bind(view)
        b.raiz.ajustarPorBarrasDelSistema(arriba = true, abajo = true)
        val nav = findNavController()

        val chips = mapOf(
            FiltroFavoritos.TODOS to b.filtroTodos,
            FiltroFavoritos.DISPONIBLES to b.filtroDisponibles,
            FiltroFavoritos.VENDIDOS to b.filtroVendidos
        )
        chips.forEach { (filtro, chip) ->
            chip.cerrarAlActivar = false
            chip.setOnClickListener { viewModel.ponerFiltro(filtro) }
        }
        b.botonExplorar.setOnClickListener { nav.irA(R.id.resultados) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estado.collect { estado ->
                    chips.forEach { (filtro, chip) -> chip.activo = filtro == estado.filtro }
                    b.subtitulo.text = resources.getQuantityString(
                        R.plurals.favoritos_guardados, estado.total, estado.total
                    )
                    val hayProductos = estado.productos.isNotEmpty()
                    b.lista.visibility = if (hayProductos) View.VISIBLE else View.GONE
                    b.estadoVacio.visibility = if (hayProductos) View.GONE else View.VISIBLE
                    // Sin favoritos: invitación a explorar. Con favoritos pero filtro vacío: solo un aviso.
                    val sinFavoritos = estado.total == 0
                    b.pintarVacio(sinFavoritos)
                    b.botonExplorar.visibility = if (sinFavoritos) View.VISIBLE else View.GONE
                    CuadriculaProductos.mostrar(
                        b.lista, estado.productos, favoritos = estado.productos.map { it.id }.toSet(),
                        vendidos = estado.vendidos,
                        alAbrir = { nav.irA(R.id.detalleProducto, Bundle().apply { putInt("productoId", it.id) }) },
                        alAlternarFavorito = { viewModel.alternarFavorito(it) }
                    )
                }
            }
        }
    }

    private fun FragmentFavoritosBinding.pintarVacio(sinFavoritos: Boolean) {
        vacioTitulo.setText(if (sinFavoritos) R.string.favoritos_vacio else R.string.favoritos_filtro_vacio)
        vacioDesc.visibility = if (sinFavoritos) View.VISIBLE else View.GONE
    }
}
