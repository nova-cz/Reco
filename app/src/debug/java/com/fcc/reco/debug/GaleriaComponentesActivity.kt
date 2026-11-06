package com.fcc.reco.debug

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fcc.reco.R
import com.fcc.reco.data.model.Categoria
import com.fcc.reco.databinding.ActivityGaleriaComponentesBinding
import com.fcc.reco.ui.comun.ItemBarra
import com.fcc.reco.ui.comun.dp
import com.fcc.reco.ui.comun.edgeToEdgeClaro

/** Solo debug: pantalla para revisar en el emulador los componentes de la Fase 1. */
class GaleriaComponentesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGaleriaComponentesBinding
    private var barraVendedor = false

    private val itemsComprador = listOf(
        ItemBarra(R.id.galeria_inicio, R.drawable.ic_inicio, R.string.tab_inicio),
        ItemBarra(R.id.galeria_favoritos, R.drawable.ic_favorito, R.string.tab_favoritos),
        ItemBarra(R.id.galeria_carrito, R.drawable.ic_carrito, R.string.tab_carrito),
        ItemBarra(R.id.galeria_compras, R.drawable.ic_compras, R.string.tab_compras),
        ItemBarra(R.id.galeria_perfil, R.drawable.ic_perfil, R.string.tab_perfil)
    )

    private val itemsVendedor = listOf(
        ItemBarra(R.id.galeria_panel, R.drawable.ic_panel, R.string.tab_panel),
        ItemBarra(R.id.galeria_anuncios, R.drawable.ic_anuncios, R.string.tab_anuncios),
        ItemBarra(R.id.galeria_publicar, R.drawable.ic_mas, R.string.tab_publicar, esAccion = true),
        ItemBarra(R.id.galeria_ventas, R.drawable.ic_ventas, R.string.tab_ventas),
        ItemBarra(R.id.galeria_perfil, R.drawable.ic_perfil, R.string.tab_perfil)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        edgeToEdgeClaro()
        binding = ActivityGaleriaComponentesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Barras del sistema: el contenido deja espacio arriba, la barra flotante sube.
        val relleno = binding.contenido.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(binding.raiz) { _, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.contenido.setPadding(
                binding.contenido.paddingLeft,
                relleno + barras.top,
                binding.contenido.paddingRight,
                binding.contenido.paddingBottom
            )
            (binding.barra.layoutParams as FrameLayout.LayoutParams).bottomMargin =
                dp(16) + barras.bottom
            binding.barra.requestLayout()
            insets
        }

        // Mosaicos con las seis categorías
        listOf(
            binding.mosaico1, binding.mosaico2, binding.mosaico3,
            binding.mosaico4, binding.mosaico5, binding.mosaico6
        ).forEachIndexed { i, mosaico -> mosaico.configurar(Categoria.entries[i]) }

        // Avatar
        binding.avatar.configurar(
            "AR",
            ContextCompat.getColor(this, R.color.naranja),
            ContextCompat.getColor(this, R.color.rosa)
        )

        // Chips de filtro: tocar alterna activo / inactivo
        listOf(
            binding.filtroLaptops, binding.filtroApple, binding.filtroGrado,
            binding.filtroPrecio, binding.filtroCertificado
        ).forEach { chip -> chip.setOnClickListener { chip.activo = !chip.activo } }

        // Barra inferior glass. Con API 31+ desenfoca el ScrollView que va detrás.
        binding.barra.vincularFondo(binding.scroll)
        mostrarBarra()
        binding.barra.ponerInsignia(R.id.galeria_carrito, 3)
        binding.barra.alSeleccionar = { item ->
            if (item.esAccion) {
                android.widget.Toast.makeText(this, "Botón + (publicar)", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        binding.botonCambiarBarra.setOnClickListener {
            barraVendedor = !barraVendedor
            mostrarBarra()
            binding.botonCambiarBarra.text =
                if (barraVendedor) "Cambiar a barra de comprador" else "Cambiar a barra de vendedor"
        }
    }

    private fun mostrarBarra() {
        binding.barra.configurar(if (barraVendedor) itemsVendedor else itemsComprador)
        if (!barraVendedor) binding.barra.ponerInsignia(R.id.galeria_carrito, 3)
    }
}
