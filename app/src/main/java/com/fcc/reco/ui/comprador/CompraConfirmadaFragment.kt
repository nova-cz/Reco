package com.fcc.reco.ui.comprador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.data.Repositorios
import com.fcc.reco.databinding.FragmentCompraConfirmadaBinding
import com.fcc.reco.databinding.ItemPedidoResumenBinding
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.comun.comoPrecio
import com.fcc.reco.ui.comun.configurarVendedor
import com.fcc.reco.ui.comun.irA

/** Resumen de la compra: un pedido por vendedor, con el pago retenido. */
class CompraConfirmadaFragment : Fragment(R.layout.fragment_compra_confirmada) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentCompraConfirmadaBinding.bind(view)
        b.raiz.ajustarPorBarrasDelSistema()
        val nav = findNavController()

        val inflater = LayoutInflater.from(requireContext())
        (arguments?.getIntArray("pedidoIds") ?: intArrayOf()).toList().mapNotNull { Repositorios.pedidos.obtener(it) }.forEach { p ->
            val r = ItemPedidoResumenBinding.inflate(inflater, b.contenedorPedidos, false)
            r.avatar.configurarVendedor(p.vendedor)
            r.titulo.text = getString(R.string.pedido_vendedor, p.id, p.vendedor.nombre)
            r.detalle.text = getString(R.string.pedido_resumen_entrega, p.puntoEntrega, p.horario)
            r.monto.text = p.monto.comoPrecio()
            b.contenedorPedidos.addView(r.root)
        }

        b.botonCompras.setOnClickListener { nav.irA(R.id.compras, popHasta = R.id.inicio) }
        b.botonExplorar.setOnClickListener { nav.irA(R.id.inicio, popHasta = R.id.inicio) }
    }
}
