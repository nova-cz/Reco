package com.fcc.reco.ui.comun

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.fcc.reco.R
import com.fcc.reco.data.model.Resena
import com.fcc.reco.databinding.ItemResenaBinding
import com.fcc.reco.databinding.LayoutTarjetaPerfilBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val local = Locale.forLanguageTag("es-MX")

/** 1731283320000 -> "nov 2026". */
fun Long.comoMesYAnio(): String = SimpleDateFormat("MMM yyyy", local).format(Date(this)).replace(".", "")

/** Pinta lo común de la tarjeta de perfil (propio y público): avatar, nombre, carrera y "Miembro desde". */
fun LayoutTarjetaPerfilBinding.pintarBase(nombre: String, carrera: String, miembroDesde: Long, colorInicio: Int, colorFin: Int) {
    avatar.configurarConNombre(nombre, colorInicio, colorFin)
    this.nombre.text = nombre
    this.carrera.text = carrera
    miembro.text = root.context.getString(R.string.perfil_miembro, miembroDesde.comoMesYAnio())
}

/** Tres cifras en fila (compras, ventas, calificación...). */
fun LayoutTarjetaPerfilBinding.pintarCifras(vararg cifras: Pair<String, String>) {
    val vistas = listOf(stat1Valor to stat1Etiqueta, stat2Valor to stat2Etiqueta, stat3Valor to stat3Etiqueta)
    vistas.forEachIndexed { i, (valor, etiqueta) ->
        val c = cifras.getOrNull(i)
        valor.text = c?.first.orEmpty()
        etiqueta.text = c?.second.orEmpty()
    }
}

/** Agrega una tarjeta por reseña. */
fun LinearLayout.llenarResenas(resenas: List<Resena>) {
    removeAllViews()
    val inflater = LayoutInflater.from(context)
    resenas.forEach { r ->
        val c = ItemResenaBinding.inflate(inflater, this, false)
        c.avatar.configurarConNombre(r.autor, ContextCompat.getColor(context, R.color.naranja), ContextCompat.getColor(context, R.color.rosa))
        c.autor.text = r.autor
        c.producto.text = if (r.producto.isEmpty()) "" else context.getString(R.string.perfil_compro, r.producto)
        c.producto.visibility = if (r.producto.isEmpty()) View.GONE else View.VISIBLE
        c.estrellas.text = "★".repeat(r.estrellas)
        c.fecha.text = r.fecha.comoFechaCorta()
        r.etiquetas.forEach { etiqueta ->
            c.etiquetas.addView(ChipInsignia(context).apply { configurar(ChipInsignia.Tipo.NEUTRO_GRIS, etiqueta) })
        }
        c.comentario.text = r.comentario
        c.comentario.visibility = if (r.comentario.isEmpty()) View.GONE else View.VISIBLE
        addView(c.root)
    }
}
