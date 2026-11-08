package com.fcc.reco.ui.vendedor

import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fcc.reco.R
import com.fcc.reco.data.repository.ErrorEnvio
import com.fcc.reco.databinding.LayoutCabeceraPublicarBinding
import com.fcc.reco.databinding.LayoutModeloPublicarBinding
import com.fcc.reco.domain.BorradorAnuncio
import com.fcc.reco.domain.ValidadorPublicacion.ErrorIdentificador
import com.fcc.reco.ui.comun.ChipFiltro
import com.fcc.reco.ui.comun.dp
import com.fcc.reco.ui.comun.dpF
import com.fcc.reco.ui.comun.iconoInicio
import com.google.android.material.chip.ChipGroup

/** Cabecera común de los cinco pasos: cerrar, "Borrador" y la barra de progreso. */
fun LayoutCabeceraPublicarBinding.configurar(fragment: Fragment, vm: PublicarViewModel, paso: Int, @StringRes nombre: Int) {
    pasoTexto.text = fragment.getString(R.string.pub_paso, paso)
    pasoNombre.setText(nombre)
    barraPasos.configurar(5, paso)
    cerrar.setOnClickListener { fragment.salirDelAsistente() }
    borrador.setOnClickListener {
        if (vm.guardarBorrador()) {
            Toast.makeText(fragment.requireContext(), R.string.pub_borrador_guardado, Toast.LENGTH_SHORT).show()
            fragment.salirDelAsistente()
        } else {
            Toast.makeText(fragment.requireContext(), R.string.pub_elige_modelo, Toast.LENGTH_SHORT).show()
        }
    }
}

fun Fragment.salirDelAsistente() {
    findNavController().popBackStack(R.id.publicar1, true)
}

fun Fragment.esDebug() = (requireContext().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

/** Tarjeta con el modelo elegido (pasos 2 y 3). "Cambiar" regresa al paso 1. */
fun LayoutModeloPublicarBinding.pintar(fragment: Fragment, b: BorradorAnuncio) {
    val m = b.modelo ?: return
    modeloIcono.setImageResource(m.categoria.icono)
    ImageViewCompat.setImageTintList(modeloIcono, android.content.res.ColorStateList.valueOf(Color.WHITE))
    modeloTitulo.text = m.titulo
    modeloSub.text = fragment.getString(R.string.pub_modelo_sub, fragment.getString(m.categoria.nombre), m.marca)
    modeloCambiar.setOnClickListener { fragment.findNavController().popBackStack(R.id.publicar1, false) }
}

/** Chips de selección única: el elegido va en negro. */
fun ChipGroup.llenarUnico(opciones: List<String>, elegido: String?, alElegir: (String) -> Unit) {
    removeAllViews()
    opciones.forEach { opcion ->
        addView(ChipFiltro(context).apply {
            text = opcion
            cerrarAlActivar = false
            activo = opcion == elegido
            setOnClickListener { alElegir(opcion) }
        })
    }
}

/** Chips de selección múltiple: los elegidos van en negro con palomita. */
fun ChipGroup.llenarMultiple(opciones: List<String>, elegidos: Set<String>, alAlternar: (String) -> Unit) {
    removeAllViews()
    opciones.forEach { opcion ->
        addView(ChipFiltro(context).apply {
            text = opcion
            cerrarAlActivar = false
            val activoAhora = opcion in elegidos
            ponerIcono(if (activoAhora) R.drawable.ic_check else 0)
            activo = activoAhora
            setOnClickListener { alAlternar(opcion) }
        })
    }
}

/** Texto del error de un IMEI o número de serie. */
@StringRes
fun ErrorIdentificador.mensaje(esImei: Boolean): Int = when (this) {
    ErrorIdentificador.VACIO -> R.string.err_id_vacio
    ErrorIdentificador.FORMATO -> if (esImei) R.string.err_id_formato_imei else R.string.err_id_formato_serie
    ErrorIdentificador.LUHN -> R.string.err_id_luhn
    ErrorIdentificador.BLOQUEADO -> R.string.err_id_bloqueado
    ErrorIdentificador.DUPLICADO -> R.string.err_id_duplicado
}

@StringRes
fun ErrorEnvio.mensaje(): Int = when (this) {
    ErrorEnvio.LIMITE_ACTIVOS -> R.string.err_envio_limite
    ErrorEnvio.FOTOS -> R.string.err_envio_fotos
    ErrorEnvio.PRECIO -> R.string.err_envio_precio
    ErrorEnvio.FALTAN_DATOS -> R.string.err_envio_datos
    ErrorEnvio.ID_VACIO -> R.string.err_id_vacio
    ErrorEnvio.ID_FORMATO -> R.string.err_id_formato_serie
    ErrorEnvio.ID_LUHN -> R.string.err_id_luhn
    ErrorEnvio.ID_BLOQUEADO -> R.string.err_id_bloqueado
    ErrorEnvio.ID_DUPLICADO -> R.string.err_id_duplicado
}

/** Casilla cuadrada negra con palomita (confirmación del paso 5, reglas del registro). */
fun ImageView.pintarCasilla(marcada: Boolean) {
    background = GradientDrawable().apply {
        cornerRadius = dpF(7f)
        if (marcada) setColor(ContextCompat.getColor(context, R.color.tinta)) else {
            setColor(Color.WHITE)
            setStroke(dp(2), ContextCompat.getColor(context, R.color.borde_suave))
        }
    }
    setImageResource(if (marcada) R.drawable.ic_check else 0)
    ImageViewCompat.setImageTintList(this, android.content.res.ColorStateList.valueOf(Color.WHITE))
}

/** Pinta un aviso con fondo suave del color del estado (verde, naranja, rojo). */
fun TextView.pintarAviso(texto: CharSequence, colorTexto: Int, colorFondo: Int, icono: Int) {
    text = texto
    setTextColor(ContextCompat.getColor(context, colorTexto))
    background = GradientDrawable().apply {
        cornerRadius = dpF(14f)
        setColor(ContextCompat.getColor(context, colorFondo))
    }
    iconoInicio(icono, 18, ContextCompat.getColor(context, colorTexto))
}

/** dp desde un Fragment. */
fun Fragment.dp(valor: Int): Int = requireContext().dp(valor)
