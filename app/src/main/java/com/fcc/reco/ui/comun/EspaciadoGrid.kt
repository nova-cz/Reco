package com.fcc.reco.ui.comun

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/** Separación uniforme entre celdas de una cuadrícula (sin borde exterior). */
class EspaciadoGrid(private val columnas: Int, private val espacioPx: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(salida: Rect, vista: View, padre: RecyclerView, estado: RecyclerView.State) {
        val posicion = padre.getChildAdapterPosition(vista)
        if (posicion == RecyclerView.NO_POSITION) return
        val columna = posicion % columnas
        salida.left = columna * espacioPx / columnas
        salida.right = espacioPx - (columna + 1) * espacioPx / columnas
        if (posicion >= columnas) salida.top = espacioPx
    }
}
