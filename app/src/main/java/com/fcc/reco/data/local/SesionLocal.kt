package com.fcc.reco.data.local

import android.content.Context
import androidx.core.content.edit
import com.fcc.reco.data.model.ModoUsuario
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
            ultimoModo = prefs.getString(MODO, null)?.let { runCatching { ModoUsuario.valueOf(it) }.getOrNull() }
        )
    }

    fun guardar(sesion: Sesion) = prefs.edit {
        putString(CORREO, sesion.correo)
        putString(NOMBRE, sesion.nombre)
        putBoolean(VERIFICADO, sesion.verificado)
        putString(UNIVERSIDAD, sesion.universidad)
        putString(CARRERA, sesion.carrera)
        putString(MODO, sesion.ultimoModo?.name)
    }

    fun borrar() = prefs.edit { clear() }

    private companion object {
        const val CORREO = "correo"
        const val NOMBRE = "nombre"
        const val VERIFICADO = "verificado"
        const val UNIVERSIDAD = "universidad"
        const val CARRERA = "carrera"
        const val MODO = "ultimo_modo"
    }
}
