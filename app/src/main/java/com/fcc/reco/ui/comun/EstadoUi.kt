package com.fcc.reco.ui.comun

import android.graphics.drawable.GradientDrawable
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.fcc.reco.R
import com.fcc.reco.data.model.EstadoPedido

/** Pares texto/fondo de los chips de estado (Figma). */
enum class EstiloEstado(@ColorRes val texto: Int, @ColorRes val fondo: Int) {
    VERDE(R.color.estado_verde, R.color.estado_verde_fondo),
    NARANJA(R.color.estado_naranja, R.color.estado_naranja_fondo),
    AZUL(R.color.estado_azul, R.color.estado_azul_fondo),
    ROJO(R.color.estado_rojo, R.color.estado_rojo_fondo),
    GRIS(R.color.estado_gris, R.color.estado_gris_fondo)
}

/** Pinta un TextView como chip de estado (píldora con texto de color). */
fun TextView.ponerEstado(texto: CharSequence, estilo: EstiloEstado) {
    text = texto
    setTextColor(ContextCompat.getColor(context, estilo.texto))
    background = GradientDrawable().apply {
        cornerRadius = dpF(100f)
        setColor(ContextCompat.getColor(context, estilo.fondo))
    }
    setPadding(dp(10), dp(5), dp(10), dp(5))
}

/** Cómo ve el comprador cada estado del pedido. */
@get:StringRes
val EstadoPedido.textoComprador: Int
    get() = when (this) {
        EstadoPedido.PAGADO_RETENIDO -> R.string.estado_pago_retenido
        EstadoPedido.ENTREGADO -> R.string.estado_listo_entregar
        EstadoPedido.LIBERADO -> R.string.estado_entregado
        EstadoPedido.EN_RECLAMO -> R.string.estado_en_reclamo
        EstadoPedido.CANCELADO -> R.string.estado_cancelado
    }

val EstadoPedido.estiloComprador: EstiloEstado
    get() = when (this) {
        EstadoPedido.PAGADO_RETENIDO -> EstiloEstado.AZUL
        EstadoPedido.ENTREGADO -> EstiloEstado.NARANJA
        EstadoPedido.LIBERADO -> EstiloEstado.VERDE
        EstadoPedido.EN_RECLAMO -> EstiloEstado.ROJO
        EstadoPedido.CANCELADO -> EstiloEstado.GRIS
    }
