package com.fcc.reco.data.local

import android.content.Context
import org.json.JSONArray
import java.io.File

/** Guarda un arreglo JSON en un archivo interno de la app (persistencia simple de los datos simulados). */
class AlmacenJson(contexto: Context, nombre: String) {
    private val archivo = File(contexto.applicationContext.filesDir, "$nombre.json")

    fun leer(): JSONArray = try {
        if (archivo.exists()) JSONArray(archivo.readText()) else JSONArray()
    } catch (e: Exception) {
        JSONArray()
    }

    fun guardar(datos: JSONArray) {
        try {
            archivo.writeText(datos.toString())
        } catch (_: Exception) {
            // Si falla el guardado, la app sigue funcionando con los datos en memoria.
        }
    }
}
