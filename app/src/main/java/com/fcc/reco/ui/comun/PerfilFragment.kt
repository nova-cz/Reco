package com.fcc.reco.ui.comun

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.data.model.ModoUsuario
import com.fcc.reco.data.repository.PerfilRepositorySimulado
import com.fcc.reco.databinding.FragmentPerfilBinding
import com.fcc.reco.databinding.ItemAnuncioPanelBinding
import com.fcc.reco.databinding.ItemMenuPerfilBinding
import com.fcc.reco.ui.acceso.abrirModoYTerminar
import com.fcc.reco.ui.acceso.cerrarSesionYSalir
import com.fcc.reco.ui.vendedor.VendedorActivity
import kotlinx.coroutines.launch

/**
 * Perfil: un solo Fragment que cambia si el estudiante ya activó el modo vendedor
 * (Figma 7:101 sin vender y 38:265 con vendedor activo).
 */
class PerfilFragment : Fragment(R.layout.fragment_perfil) {

    private val viewModel: PerfilViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentPerfilBinding.bind(view)
        b.raiz.ajustarPorBarrasDelSistema(arriba = true, abajo = true)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estado.collect { pintar(b, it) }
            }
        }
    }

    private fun pintar(b: FragmentPerfilBinding, e: EstadoPerfil) {
        val s = e.sesion ?: return
        val ctx = requireContext()
        val nav = findNavController()
        val enModoVendedor = requireActivity() is VendedorActivity
        val esVendedor = s.esVendedor

        // Tarjeta
        b.tarjeta.pintarBase(s.nombre, s.carrera.orEmpty(), s.creado, ContextCompat.getColor(ctx, R.color.azul), ContextCompat.getColor(ctx, R.color.verde_reco))
        b.tarjeta.insigniaVerificado.configurar(ChipInsignia.Tipo.VERIFICADO_AZUL, getString(R.string.verificado_universidad, s.universidad ?: "BUAP"))
        if (esVendedor) {
            b.tarjeta.pintarCifras(
                e.compras.toString() to getString(R.string.perfil_compras),
                e.ventas.toString() to getString(R.string.perfil_ventas),
                (e.calificacion?.let { getString(R.string.perfil_calificacion_valor, it) } ?: "—") to getString(R.string.perfil_calificacion)
            )
        } else {
            b.tarjeta.pintarCifras(
                e.compras.toString() to getString(R.string.perfil_compras),
                e.favoritos.toString() to getString(R.string.perfil_favoritos),
                e.resenasDadas.toString() to getString(R.string.perfil_resenas)
            )
        }

        // "¿Quieres vender algo?" solo si aún no es vendedor
        b.tarjetaVender.visibility = if (esVendedor) View.GONE else View.VISIBLE
        b.tarjetaVender.setOnClickListener { nav.irA(R.id.registroVendedor) }

        // Vendedor: artículos y reseñas recibidas
        b.seccionVendedor.visibility = if (esVendedor) View.VISIBLE else View.GONE
        if (esVendedor) {
            val inflater = LayoutInflater.from(ctx)
            CuadriculaProductos.armarFilas(b.listaArticulos, e.articulos, igualarAlturas = false) { fila, item ->
                val c = ItemAnuncioPanelBinding.inflate(inflater, fila, false)
                c.titulo.text = item.anuncio.titulo
                c.precio.text = item.anuncio.precio.comoPrecio()
                c.foto.mostrarFoto(item.anuncio.fotos, item.anuncio.categoria.icono)
                c.chipEstado.ponerEstado(item.estado.etiqueta, item.estado.estilo)
                c.root.setOnClickListener { irAAnuncios(nav, enModoVendedor) }
                c.root
            }
            b.sinArticulos.visibility = if (e.articulos.isEmpty()) View.VISIBLE else View.GONE
            b.verTodoArticulos.setOnClickListener { irAAnuncios(nav, enModoVendedor) }
            b.botonPerfilPublico.setOnClickListener {
                nav.irA(R.id.perfilPublico, Bundle().apply { putString("vendedor", s.nombreCorto) })
            }
            b.listaResenas.llenarResenas(e.resenas.take(1))
            b.sinResenas.visibility = if (e.resenas.isEmpty()) View.VISIBLE else View.GONE
            b.verResenas.visibility = if (e.resenas.size > 1) View.VISIBLE else View.GONE
            b.verResenas.text = getString(R.string.perfil_ver_las, e.resenas.size)
            b.verResenas.setOnClickListener { nav.irA(R.id.perfilPublico, Bundle().apply { putString("vendedor", s.nombreCorto); putBoolean("resenas", true) }) }
        }

        // Menú
        b.menu.removeAllViews()
        val filas = buildList<Fila> {
            if (!enModoVendedor) {
                add(Fila(R.drawable.ic_compras, getString(R.string.perfil_mis_compras), e.porEntregar.takeIf { it > 0 }?.let { getString(R.string.perfil_por_entregar, it) }) { nav.irA(R.id.compras) })
                add(Fila(R.drawable.ic_favorito, getString(R.string.tab_favoritos), null) { nav.irA(R.id.favoritos) })
            }
            if (esVendedor) {
                val v = s.vendedor!!
                add(Fila(R.drawable.ic_ubicacion, getString(R.string.perfil_puntos), v.puntoEntrega) { proximamente() })
                add(Fila(R.drawable.ic_tarjeta, getString(R.string.perfil_cobro), getString(R.string.perfil_clabe, v.clabe.takeLast(4))) { proximamente() })
            }
            add(Fila(R.drawable.ic_birrete, getString(R.string.perfil_universidad), getString(R.string.perfil_universidad_valor, s.universidad ?: "BUAP"), conChevron = false) {})
            add(Fila(R.drawable.ic_perfil, getString(R.string.perfil_mis_datos), getString(R.string.perfil_mis_datos_valor)) { proximamente() })
            add(Fila(R.drawable.ic_info, getString(R.string.perfil_ayuda), null) { mostrarReglas() })
            if (esVendedor) {
                val otro = if (enModoVendedor) ModoUsuario.COMPRADOR else ModoUsuario.VENDEDOR
                add(Fila(R.drawable.ic_inicio, getString(if (enModoVendedor) R.string.cambiar_a_comprador else R.string.cambiar_a_vendedor), null) {
                    PerfilRepositorySimulado(SesionLocal(requireContext())).cambiarModo(otro)
                    requireActivity().abrirModoYTerminar(otro)
                })
            }
            add(Fila(R.drawable.ic_cerrar, getString(R.string.cerrar_sesion), null, rojo = true, conChevron = false) { requireActivity().cerrarSesionYSalir() })
        }
        val inflater = LayoutInflater.from(ctx)
        filas.forEachIndexed { i, f ->
            val r = ItemMenuPerfilBinding.inflate(inflater, b.menu, false)
            r.icono.setImageResource(f.icono)
            r.titulo.text = f.titulo
            r.valor.text = f.valor.orEmpty()
            r.valor.visibility = if (f.valor == null) View.GONE else View.VISIBLE
            r.chevron.visibility = if (f.conChevron) View.VISIBLE else View.GONE
            if (f.rojo) {
                val rojo = ContextCompat.getColor(ctx, R.color.estado_rojo)
                r.titulo.setTextColor(rojo)
                r.icono.setBackgroundResource(R.drawable.bg_circulo_rojo_suave)
                androidx.core.widget.ImageViewCompat.setImageTintList(r.icono, android.content.res.ColorStateList.valueOf(rojo))
            }
            r.root.setOnClickListener { f.alTocar() }
            b.menu.addView(r.root)
            if (i < filas.lastIndex) b.menu.addView(View(ctx).apply { setBackgroundColor(ContextCompat.getColor(ctx, R.color.borde_suave)) }, android.widget.LinearLayout.LayoutParams(-1, 1))
        }
    }

    private class Fila(val icono: Int, val titulo: String, val valor: String?, val rojo: Boolean = false, val conChevron: Boolean = true, val alTocar: () -> Unit)

    /** Desde modo comprador, "Ver todo" cambia a modo vendedor para abrir Mis anuncios. */
    private fun irAAnuncios(nav: androidx.navigation.NavController, enModoVendedor: Boolean) {
        if (enModoVendedor) nav.irA(R.id.anuncios) else {
            PerfilRepositorySimulado(SesionLocal(requireContext())).cambiarModo(ModoUsuario.VENDEDOR)
            requireActivity().abrirModoYTerminar(ModoUsuario.VENDEDOR)
        }
    }

    private fun proximamente() = Toast.makeText(requireContext(), R.string.proximamente, Toast.LENGTH_SHORT).show()

    private fun mostrarReglas() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.perfil_reglas_titulo)
            .setMessage(R.string.perfil_reglas)
            .setPositiveButton(R.string.entendido, null)
            .show()
    }
}
