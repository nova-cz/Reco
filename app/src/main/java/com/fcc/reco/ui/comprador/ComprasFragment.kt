package com.fcc.reco.ui.comprador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.data.Repositorios
import com.fcc.reco.data.model.EstadoPedido
import com.fcc.reco.data.model.Pedido
import com.fcc.reco.databinding.FragmentComprasBinding
import com.fcc.reco.databinding.ItemCompraBinding
import com.fcc.reco.ui.comun.BotonPildora
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.comun.comoFechaCorta
import com.fcc.reco.ui.comun.comoPrecio
import com.fcc.reco.ui.comun.configurarVendedor
import com.fcc.reco.ui.comun.estiloComprador
import com.fcc.reco.ui.comun.iconoInicio
import com.fcc.reco.ui.comun.irA
import com.fcc.reco.ui.comun.ponerEstado
import com.fcc.reco.ui.comun.textoComprador
import kotlinx.coroutines.launch

/** Mis compras: pedidos En curso y Completadas (Figma 33:445). */
class ComprasFragment : Fragment(R.layout.fragment_compras) {

    private val viewModel: ComprasViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentComprasBinding.bind(view)
        b.raiz.ajustarPorBarrasDelSistema(arriba = true, abajo = true)
        val nav = findNavController()

        b.filtroEnCurso.cerrarAlActivar = false
        b.filtroCompletadas.cerrarAlActivar = false
        b.filtroEnCurso.setOnClickListener { viewModel.ponerFiltro(FiltroCompras.EN_CURSO) }
        b.filtroCompletadas.setOnClickListener { viewModel.ponerFiltro(FiltroCompras.COMPLETADAS) }
        b.botonExplorar.setOnClickListener { nav.irA(R.id.inicio, popHasta = R.id.inicio) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estado.collect { estado ->
                    b.filtroEnCurso.text = getString(R.string.compras_en_curso, estado.totalEnCurso)
                    b.filtroEnCurso.activo = estado.filtro == FiltroCompras.EN_CURSO
                    b.filtroCompletadas.activo = estado.filtro == FiltroCompras.COMPLETADAS

                    b.lista.removeAllViews()
                    val inflater = LayoutInflater.from(requireContext())
                    estado.pedidos.forEach { b.lista.addView(crearTarjeta(inflater, b, it)) }

                    val vacio = estado.pedidos.isEmpty()
                    b.estadoVacio.visibility = if (vacio) View.VISIBLE else View.GONE
                    val sinNada = estado.totalPedidos == 0
                    b.vacioTitulo.setText(
                        when {
                            sinNada -> R.string.compras_vacio
                            estado.filtro == FiltroCompras.EN_CURSO -> R.string.compras_en_curso_vacio
                            else -> R.string.compras_completadas_vacio
                        }
                    )
                    b.vacioDesc.visibility = if (sinNada) View.VISIBLE else View.GONE
                    b.botonExplorar.visibility = if (sinNada) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun crearTarjeta(inflater: LayoutInflater, b: FragmentComprasBinding, p: Pedido): View {
        val c = ItemCompraBinding.inflate(inflater, b.lista, false)
        val productos = p.productoIds.mapNotNull { Repositorios.productos.obtenerPorId(it) }
        val primero = productos.firstOrNull()

        c.avatar.configurarVendedor(p.vendedor)
        c.vendedor.text = p.vendedor.nombre
        c.pedido.text = getString(R.string.pedido_codigo_fecha, p.codigo, p.creado.comoFechaCorta())
        c.chipEstado.ponerEstado(getString(p.estado.textoComprador), p.estado.estiloComprador)
        primero?.let { c.icono.setImageResource(it.categoria.icono) }
        c.titulo.text = primero?.titulo.orEmpty()
        if (productos.size > 1) {
            c.masEquipos.text = getString(R.string.pedido_mas_equipos, productos.size - 1)
            c.masEquipos.visibility = View.VISIBLE
        }
        c.monto.text = p.monto.comoPrecio()
        c.entrega.text = getString(R.string.pedido_resumen_entrega, p.puntoEntrega, p.horario)
        c.entrega.iconoInicio(R.drawable.ic_ubicacion, 16, androidx.core.content.ContextCompat.getColor(requireContext(), R.color.texto_secundario))

        val sinCalificar = p.estado == EstadoPedido.LIBERADO && p.estrellas == null
        when {
            p.estado == EstadoPedido.ENTREGADO -> configurarAccion(c, R.string.confirmar_entrega, BotonPildora.Estilo.PRIMARIO, R.drawable.ic_check) {
                // Confirma y abre el detalle con la hoja de calificación.
                if (viewModel.confirmarEntrega(p.id)) abrirDetalle(p.id, calificar = true)
            }
            sinCalificar -> configurarAccion(c, R.string.calificar_vendedor, BotonPildora.Estilo.SECUNDARIO, R.drawable.ic_estrella_llena) {
                abrirDetalle(p.id, calificar = true)
            }
        }
        c.root.setOnClickListener { abrirDetalle(p.id) }
        return c.root
    }

    private fun configurarAccion(c: ItemCompraBinding, texto: Int, estilo: BotonPildora.Estilo, icono: Int, alTocar: () -> Unit) {
        c.botonAccion.visibility = View.VISIBLE
        c.botonAccion.estilo = estilo
        c.botonAccion.setText(texto)
        c.botonAccion.ponerIcono(icono)
        c.botonAccion.setOnClickListener { alTocar() }
    }

    private fun abrirDetalle(pedidoId: Int, calificar: Boolean = false) {
        findNavController().irA(R.id.detallePedido, Bundle().apply {
            putInt("pedidoId", pedidoId)
            putBoolean("calificar", calificar)
        })
    }
}
