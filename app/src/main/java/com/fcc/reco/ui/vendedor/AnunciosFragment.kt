package com.fcc.reco.ui.vendedor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.data.model.EstadoAnuncio
import com.fcc.reco.databinding.FragmentAnunciosBinding
import com.fcc.reco.databinding.ItemAnuncioBinding
import com.fcc.reco.ui.comun.ChipFiltro
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.comun.comoFechaCorta
import com.fcc.reco.ui.comun.comoPrecio
import com.fcc.reco.ui.comun.dp
import com.fcc.reco.ui.comun.estilo
import com.fcc.reco.ui.comun.irA
import com.fcc.reco.ui.comun.mostrarFoto
import com.fcc.reco.ui.comun.ponerEstado
import com.fcc.reco.domain.ValidadorPublicacion
import kotlinx.coroutines.launch

/** Mis anuncios: lista con estados y acciones (Figma 35:233). */
class AnunciosFragment : Fragment(R.layout.fragment_anuncios) {

    private val viewModel: AnunciosViewModel by viewModels()

    private val motivosDeRechazo = listOf("Fotos borrosas", "Precio fuera de rango", "Información incompleta")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentAnunciosBinding.bind(view)
        b.raiz.ajustarPorBarrasDelSistema(arriba = true, abajo = true)

        // Un chip por filtro; se crean una vez y solo cambia cuál está activo.
        val nombres = mapOf(
            FiltroAnuncios.TODOS to R.string.filtro_anuncios_todos, FiltroAnuncios.PUBLICADOS to R.string.filtro_publicados,
            FiltroAnuncios.EN_REVISION to R.string.filtro_en_revision, FiltroAnuncios.RECHAZADOS to R.string.filtro_rechazados,
            FiltroAnuncios.PAUSADOS to R.string.filtro_pausados, FiltroAnuncios.VENDIDOS to R.string.panel_vendidos,
            FiltroAnuncios.BORRADORES to R.string.filtro_borradores
        )
        val chips = FiltroAnuncios.entries.associateWith { filtro ->
            ChipFiltro(requireContext()).apply {
                cerrarAlActivar = false
                setText(nombres.getValue(filtro))
                setOnClickListener { viewModel.ponerFiltro(filtro) }
                b.filaFiltros.addView(this, android.widget.LinearLayout.LayoutParams(-2, -2).apply { if (b.filaFiltros.childCount > 0) marginStart = dp(8) })
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estado.collect { e ->
                    chips.forEach { (f, chip) -> chip.activo = f == e.filtro }
                    chips.getValue(FiltroAnuncios.TODOS).text = getString(R.string.filtro_anuncios_todos, e.total)
                    b.subtitulo.text = getString(R.string.anuncios_subtitulo, e.activos, ValidadorPublicacion.MAX_ACTIVOS)
                    b.estadoVacio.visibility = if (e.items.isEmpty()) View.VISIBLE else View.GONE
                    b.lista.removeAllViews()
                    val inflater = LayoutInflater.from(requireContext())
                    e.items.forEach { item -> b.lista.addView(crearFila(inflater, b, item)) }
                }
            }
        }
    }

    private fun crearFila(inflater: LayoutInflater, b: FragmentAnunciosBinding, item: AnuncioConEstado): View {
        val a = item.anuncio
        val estado = item.estado
        val c = ItemAnuncioBinding.inflate(inflater, b.lista, false)
        c.foto.mostrarFoto(a.fotos, a.categoria.icono)
        c.chipEstado.ponerEstado(estado.etiqueta, estado.estilo)
        c.titulo.text = a.titulo
        val nota = when (estado) {
            EstadoAnuncio.EN_REVISION -> getString(if (a.revisionManual) R.string.nota_revision_manual else R.string.nota_en_revision)
            EstadoAnuncio.RECHAZADO -> a.motivoRechazo.orEmpty()
            EstadoAnuncio.PAUSADO -> getString(R.string.nota_pausado)
            EstadoAnuncio.VENDIDO -> getString(R.string.nota_vendido)
            EstadoAnuncio.BORRADOR -> getString(R.string.nota_borrador)
            else -> a.creado.comoFechaCorta()
        }
        c.detalle.text = "${a.precio.comoPrecio()} · $nota".takeIf { a.precio > 0 } ?: nota

        // Acción principal de la derecha
        val accion: Pair<Int, () -> Unit>? = when (estado) {
            EstadoAnuncio.PUBLICADO -> R.string.anuncio_pausar to { viewModel.pausar(a.id) }
            EstadoAnuncio.PAUSADO -> R.string.anuncio_reactivar to { viewModel.reanudar(a.id) }
            EstadoAnuncio.RECHAZADO -> R.string.anuncio_corregir to { abrirAsistente(a.id) }
            EstadoAnuncio.BORRADOR -> R.string.anuncio_continuar to { abrirAsistente(a.id) }
            else -> null
        }
        if (accion != null) {
            c.accion.visibility = View.VISIBLE
            c.accion.setText(accion.first)
            c.accion.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), when (estado) {
                EstadoAnuncio.RECHAZADO -> R.color.estado_rojo
                EstadoAnuncio.PAUSADO, EstadoAnuncio.PUBLICADO, EstadoAnuncio.BORRADOR -> R.color.azul
                else -> R.color.tinta
            }))
            c.accion.setOnClickListener { accion.second() }
        }

        // Mantener presionado: eliminar (menos lo que ya se vendió)
        if (estado != EstadoAnuncio.VENDIDO) {
            c.filaPrincipal.setOnLongClickListener { confirmarEliminar(a.id); true }
        }

        // Solo en debug: moderación y compra de otro estudiante
        if (esDebug()) {
            when (estado) {
                EstadoAnuncio.EN_REVISION -> {
                    c.filaDebug.visibility = View.VISIBLE
                    c.debugUno.setText(R.string.debug_aprobar)
                    c.debugUno.setOnClickListener { viewModel.aprobar(a.id) }
                    c.debugDos.visibility = View.VISIBLE
                    c.debugDos.setText(R.string.debug_rechazar)
                    c.debugDos.setOnClickListener { elegirMotivo(c.debugDos, a.id) }
                }
                EstadoAnuncio.PUBLICADO -> {
                    c.filaDebug.visibility = View.VISIBLE
                    c.debugUno.setText(R.string.debug_simular_compra)
                    c.debugUno.setOnClickListener { viewModel.simularCompra(a) }
                    c.debugDos.visibility = View.GONE
                }
                else -> Unit
            }
        }
        return c.root
    }

    private fun abrirAsistente(anuncioId: Int) {
        findNavController().irA(R.id.publicar1, Bundle().apply { putInt("anuncioId", anuncioId) })
    }

    private fun elegirMotivo(ancla: View, id: Int) {
        PopupMenu(requireContext(), ancla).apply {
            motivosDeRechazo.forEachIndexed { i, m -> menu.add(0, i, i, m) }
            setOnMenuItemClickListener { viewModel.rechazar(id, motivosDeRechazo[it.itemId]); true }
            show()
        }
    }

    private fun confirmarEliminar(id: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.anuncio_eliminar_titulo)
            .setMessage(R.string.anuncio_eliminar_desc)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.eliminar) { _, _ -> viewModel.eliminar(id) }
            .show()
    }
}
