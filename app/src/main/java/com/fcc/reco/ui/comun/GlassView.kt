package com.fcc.reco.ui.comun

import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.view.ViewParent
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.fcc.reco.R

/** Verdadero mientras un GlassView dibuja el fondo que va a desenfocar (evita que se dibuje a sí mismo). */
private var dibujandoFondo = false

/**
 * Panel con efecto "glass": blanco translúcido con borde de luz.
 *
 * - Base (todas las versiones): relleno blanco al 72 % + borde de luz.
 * - API 31+: si se le indica un fondo con [vincularFondo], dibuja una copia desenfocada de ese
 *   fondo (RenderEffect) debajo del relleno, que baja al 55 %.
 *
 * Regla del fondo: debe ser un **hermano** del GlassView (no un ancestro), por ejemplo el
 * contenedor del NavHost o un ScrollView, y llevar él mismo el fondo de la pantalla.
 * Si es ancestro se ignora y queda solo la versión translúcida.
 */
open class GlassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var radio = 0f
    private var desenfoque = 0f
    private var opaco = false

    private val rect = RectF()
    private val ruta = Path()
    private val pintorRelleno = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pintorBorde = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

    private var fondo: View? = null
    private var nodo: Any? = null
    private var observador: ViewTreeObserver? = null
    private val alDesplazar = ViewTreeObserver.OnScrollChangedListener { invalidate() }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.GlassView, defStyleAttr, 0)
        radio = a.getDimension(R.styleable.GlassView_glassRadio, context.dpF(24f))
        desenfoque = a.getDimension(
            R.styleable.GlassView_glassDesenfoque,
            resources.getDimension(R.dimen.desenfoque_glass)
        )
        opaco = a.getBoolean(R.styleable.GlassView_glassOpaco, false)
        a.recycle()

        setWillNotDraw(false)
        pintorBorde.strokeWidth = context.dpF(1f)
        pintorBorde.color = ContextCompat.getColor(context, R.color.glass_borde)
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radio)
            }
        }
    }

    /** Cambia el radio de las esquinas (en px). */
    fun ponerRadio(px: Float) {
        radio = px
        invalidateOutline()
        invalidate()
    }

    /** Indica qué vista (hermana) se desenfoca por detrás. Pasa null para quitarlo. */
    fun vincularFondo(vista: View?) {
        fondo = vista
        invalidate()
    }

    /** Vuelve a dibujar el desenfoque (útil si el fondo cambia sin hacer scroll). */
    fun refrescar() = invalidate()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        observador = viewTreeObserver.also { it.addOnScrollChangedListener(alDesplazar) }
    }

    override fun onDetachedFromWindow() {
        observador?.takeIf { it.isAlive }?.removeOnScrollChangedListener(alDesplazar)
        observador = null
        super.onDetachedFromWindow()
    }

    private fun fondoUsable(): Boolean {
        val f = fondo ?: return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
        if (width == 0 || height == 0 || f.width == 0 || f.height == 0) return false
        var padre: ViewParent? = parent
        while (padre != null) {
            if (padre === f) return false
            padre = padre.parent
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        if (dibujandoFondo) return
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        ruta.reset()
        ruta.addRoundRect(rect, radio, radio, Path.Direction.CW)

        val conDesenfoque = canvas.isHardwareAccelerated && fondoUsable()
        if (conDesenfoque && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dibujarDesenfoque(canvas)
        }

        val relleno = if (opaco || !conDesenfoque) R.color.glass_relleno_opaco else R.color.glass_relleno
        pintorRelleno.color = ContextCompat.getColor(context, relleno)
        canvas.drawPath(ruta, pintorRelleno)

        val mitad = pintorBorde.strokeWidth / 2f
        rect.inset(mitad, mitad)
        canvas.drawRoundRect(rect, radio - mitad, radio - mitad, pintorBorde)
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (dibujandoFondo) return
        super.dispatchDraw(canvas)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun dibujarDesenfoque(canvas: Canvas) {
        val fondo = fondo ?: return
        // Margen extra para que los bordes del desenfoque no se "estiren" (TileMode.CLAMP).
        val margen = (desenfoque * 3).toInt()
        val nodoActual = (nodo as? RenderNode) ?: RenderNode("glass").also { nodo = it }
        nodoActual.setPosition(0, 0, width + 2 * margen, height + 2 * margen)
        nodoActual.setRenderEffect(
            RenderEffect.createBlurEffect(desenfoque, desenfoque, Shader.TileMode.CLAMP)
        )

        val mio = IntArray(2)
        val delFondo = IntArray(2)
        getLocationInWindow(mio)
        fondo.getLocationInWindow(delFondo)

        val grabacion = nodoActual.beginRecording()
        grabacion.translate(
            (margen - (mio[0] - delFondo[0])).toFloat(),
            (margen - (mio[1] - delFondo[1])).toFloat()
        )
        dibujandoFondo = true
        try {
            fondo.draw(grabacion)
        } finally {
            dibujandoFondo = false
            nodoActual.endRecording()
        }

        canvas.save()
        canvas.clipPath(ruta)
        canvas.translate(-margen.toFloat(), -margen.toFloat())
        canvas.drawRenderNode(nodoActual)
        canvas.restore()
    }
}
