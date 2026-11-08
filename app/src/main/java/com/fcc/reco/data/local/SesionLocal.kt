package com.fcc.reco.data.local

import android.content.Context
import androidx.core.content.edit
import com.fcc.reco.data.model.ModoUsuario
import com.fcc.reco.data.model.PerfilVendedor
import org.json.JSONArray
import org.json.JSONObject
import com.fcc.reco.data.model.Sesion

/** Guarda la sesión simulada en SharedPreferences (sin contraseñas). */
class SesionLocal(contexto: Context) {
    private val prefs = contexto.applicationContext.getSharedPreferences("reco_sesion", Context.MODE_PRIVATE)

    fun leer(): Sesion? {
        val correo = prefs.getString(CORREO, null) ?: return null
        return Sesion(
            correo = correo,
            nombre = prefs.getString(NOMBRE, "") ?: "",
            verificado = prefs.getBoolean(VERIFICADO, false),
            universidad = prefs.getString(UNIVERSIDAD, null),
            carrera = prefs.getString(CARRERA, null),
            ultimoModo = prefs.getString(MODO, null)?.let { runCatching { ModoUsuario.valueOf(it) }.getOrNull() },
            vendedor = prefs.getString(VENDEDOR, null)?.let { leerVendedor(it) },
            creado = prefs.getLong(CREADO, 0L).takeIf { it > 0 } ?: System.currentTimeMillis().also { prefs.edit { putLong(CREADO, it) } }
        )
    }

    fun guardar(sesion: Sesion) = prefs.edit {
        putString(CORREO, sesion.correo)
        putString(NOMBRE, sesion.nombre)
        putBoolean(VERIFICADO, sesion.verificado)
        putString(UNIVERSIDAD, sesion.universidad)
        putString(CARRERA, sesion.carrera)
        putString(MODO, sesion.ultimoModo?.name)
        putString(VENDEDOR, sesion.vendedor?.let { escribirVendedor(it) })
        if (prefs.getLong(CREADO, 0L) == 0L) putLong(CREADO, sesion.creado.takeIf { it > 0 } ?: System.currentTimeMillis())
    }

    private fun escribirVendedor(v: PerfilVendedor): String = JSONObject().apply {
        put("telefono", v.telefono); put("punto", v.puntoEntrega); put("clabe", v.clabe)
        put("dias", JSONArray(v.dias)); put("franjas", JSONArray(v.franjas))
    }.toString()

    private fun leerVendedor(texto: String): PerfilVendedor? = runCatching {
        val o = JSONObject(texto)
        fun lista(clave: String) = o.getJSONArray(clave).let { a -> (0 until a.length()).map { a.getString(it) } }
        PerfilVendedor(o.getString("telefono"), o.getString("punto"), lista("dias"), lista("franjas"), o.getString("clabe"))
    }.getOrNull()

    fun borrar() = prefs.edit { clear() }

    private companion object {
        const val CORREO = "correo"
        const val NOMBRE = "nombre"
        const val VERIFICADO = "verificado"
        const val UNIVERSIDAD = "universidad"
        const val CARRERA = "carrera"
        const val MODO = "ultimo_modo"
        const val VENDEDOR = "vendedor"
        const val CREADO = "creado"
    }
}
