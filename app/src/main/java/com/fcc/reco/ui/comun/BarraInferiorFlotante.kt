package com.fcc.reco.ui.comun

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.navOptions
import com.fcc.reco.R

/**
 * Un elemento de la barra. Para vincularla con Navigation, [id] debe ser el id del destino
 * en el grafo. Si [esAccion] es verdadero se pinta como el botón naranja "+" y no navega:
 * solo avisa por [BarraInferiorFlotante.alSeleccionar].
 */
data class ItemBarra(
    @IdRes val id: Int,
    @DrawableRes val icono: Int,
    @StringRes val titulo: Int,
    val esAccion: Boolean = false
)

/**
 * Barra inferior flotante con efecto glass. La pestaña activa se muestra como píldora negra
 * con ícono y texto; las demás, solo ícono. Admite un globito naranja con contador.
 */
class BarraInferiorFlotante @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GlassView(context, attrs, defStyleAttr) {

    private val fila = LinearLayout(context)
    private var items: List<ItemBarra> = emptyList()
    private val insignias = mutableMapOf<Int, Int>()

    var seleccionadoId: Int = View.NO_ID
        private set

    /** Se llama al tocar cualquier elemento (también el botón de acción). */
    var alSeleccionar: ((ItemBarra) -> Unit)? = null

    private var nav: NavController? = null
    private val escuchaDestino = NavController.OnDestinationChangedListener { _, destino, _ ->
        val coincidencia = items.firstOrNull { !it.esAccion && contiene(destino, it.id) }
        if (coincidencia != null) seleccionar(coincidencia.id)
    }

    init {
        ponerRadio(dpF(32f))
        setPadding(dp(8), dp(8), dp(8), dp(8))
        elevation = dpF(8f)
        fila.orientation = LinearLayout.HORIZONTAL
        fila.gravity = Gravity.CENTER_VERTICAL
        addView(fila, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    /** Define los elementos de la barra y cuál empieza seleccionado. */
    fun configurar(
        lista: List<ItemBarra>,
        seleccionInicial: Int = lista.firstOrNull { !it.esAccion }?.id ?: View.NO_ID
    ) {
        items = lista
        seleccionadoId = seleccionInicial
        reconstruir(animar = false)
    }

    fun seleccionar(@IdRes id: Int) {
        if (id == seleccionadoId) return
        seleccionadoId = id
        reconstruir(animar = true)
    }

    /** Muestra un globito naranja con [cantidad] sobre el elemento. Con 0 lo oculta. */
    fun ponerInsignia(@IdRes id: Int, cantidad: Int) {
        if (cantidad <= 0) insignias.remove(id) else insignias[id] = cantidad
        reconstruir(animar = false)
    }

    /** Hace que tocar un elemento navegue a su destino y que la selección siga al destino actual. */
    fun vincular(navController: NavController) {
        nav?.removeOnDestinationChangedListener(escuchaDestino)
        nav = navController
        navController.addOnDestinationChangedListener(escuchaDestino)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        nav?.let {
            it.removeOnDestinationChangedListener(escuchaDestino)
            it.addOnDestinationChangedListener(escuchaDestino)
        }
    }

    override fun onDetachedFromWindow() {
        nav?.removeOnDestinationChangedListener(escuchaDestino)
        super.onDetachedFromWindow()
    }

    private fun contiene(destino: NavDestination, id: Int): Boolean {
        var actual: NavDestination? = destino
        while (actual != null) {
            if (actual.id == id) return true
            actual = actual.parent
        }
        return false
    }

    private fun alTocar(item: ItemBarra) {
        alSeleccionar?.invoke(item)
        if (item.esAccion) return
        val controlador = nav
        if (controlador == null) {
            seleccionar(item.id)
        } else {
            controlador.navigate(item.id, null, navOptions {
                launchSingleTop = true
                restoreState = true
                popUpTo(controlador.graph.startDestinationId) { saveState = true }
            })
        }
    }

    private fun reconstruir(animar: Boolean) {
        if (animar && isLaidOut) {
            TransitionManager.beginDelayedTransition(fila, AutoTransition().setDuration(180))
        }
        fila.removeAllViews()
        items.forEach { item ->
            val vista = crearVista(item)
            vista.setOnClickListener { alTocar(item) }
            vista.contentDescription = context.getString(item.titulo)
            val activo = item.id == seleccionadoId && !item.esAccion
            val parametros = when {
                item.esAccion -> LinearLayout.LayoutParams(dp(48), dp(48)).apply {
                    marginStart = dp(4)
                    marginEnd = dp(4)
                }
                activo -> LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, dp(48))
                else -> LinearLayout.LayoutParams(0, dp(48), 1f)
            }
            fila.addView(vista, parametros)
        }
    }

    private fun crearVista(item: ItemBarra): View {
        val blanco = ColorStateList.valueOf(Color.WHITE)
        if (item.esAccion) {
            return ImageView(context).apply {
                setImageResource(item.icono)
                scaleType = ImageView.ScaleType.CENTER
                setBackgroundResource(R.drawable.bg_boton_naranja)
                ImageViewCompat.setImageTintList(this, blanco)
            }
        }

        val envoltorio = FrameLayout(context)
        val activo = item.id == seleccionadoId
        if (activo) {
            val pildora = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                setBackgroundResource(R.drawable.bg_boton_primario)
                setPadding(dp(16), 0, dp(18), 0)
            }
            pildora.addView(ImageView(context).apply {
                setImageResource(item.icono)
                ImageViewCompat.setImageTintList(this, blanco)
            }, LinearLayout.LayoutParams(dp(22), dp(22)))
            pildora.addView(TextView(context).apply {
                setTextAppearance(R.style.Reco_Texto_Boton)
                textSize = 14f
                setTextColor(Color.WHITE)
                setText(item.titulo)
                maxLines = 1
            }, LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                marginStart = dp(8)
            })
            envoltorio.addView(pildora, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT))
        } else {
            val icono = ImageView(context).apply {
                setImageResource(item.icono)
                ImageViewCompat.setImageTintList(
                    this,
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.tinta))
                )
            }
            envoltorio.addView(icono, LayoutParams(dp(22), dp(22), Gravity.CENTER))
        }

        val cantidad = insignias[item.id] ?: 0
        if (cantidad > 0) {
            val globito = TextView(context).apply {
                setTextAppearance(R.style.Reco_Texto_Etiqueta)
                setTextColor(Color.WHITE)
                textSize = 10f
                gravity = Gravity.CENTER
                text = cantidad.toString()
                setBackgroundResource(R.drawable.bg_badge_naranja)
            }
            val posicion = if (activo) Gravity.TOP or Gravity.END else Gravity.TOP or Gravity.CENTER_HORIZONTAL
            envoltorio.addView(globito, LayoutParams(dp(18), dp(18), posicion).apply {
                topMargin = dp(2)
                if (activo) marginEnd = dp(4) else marginStart = dp(24)
            })
        }
        return envoltorio
    }
}
