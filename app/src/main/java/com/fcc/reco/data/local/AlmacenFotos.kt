package com.fcc.reco.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import java.io.File
import java.util.UUID

/**
 * Fotos de los anuncios: se copian a la carpeta interna de la app, reducidas a un máximo de 1280 px
 * (con Supabase se suben a Storage).
 */
object AlmacenFotos {
    private const val LADO_MAX = 1280

    private fun carpeta(contexto: Context) = File(contexto.applicationContext.filesDir, "fotos").apply { mkdirs() }

    /** Copia la imagen elegida y devuelve la ruta, o null si no se pudo leer. */
    fun guardar(contexto: Context, uri: Uri): String? = runCatching {
        val resolver = contexto.contentResolver
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
        var muestreo = 1
        while (maxOf(limites.outWidth, limites.outHeight) / muestreo > LADO_MAX * 2) muestreo *= 2
        val bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = muestreo })
        } ?: return null
        escribir(contexto, bitmap)
    }.getOrNull()

    /** Solo para pruebas en debug: una "foto" con degradado y número. */
    fun generarDePrueba(contexto: Context, numero: Int): String? = runCatching {
        val bitmap = Bitmap.createBitmap(900, 900, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val colores = listOf(Color.parseColor("#2F7BF6") to Color.parseColor("#FF8DB3"), Color.parseColor("#FF7A1A") to Color.parseColor("#D29A5C"), Color.parseColor("#1C1C1E") to Color.parseColor("#6E6E76"))
        val (a, b) = colores[numero % colores.size]
        canvas.drawRect(0f, 0f, 900f, 900f, Paint().apply { shader = LinearGradient(0f, 0f, 900f, 900f, a, b, Shader.TileMode.CLAMP) })
        canvas.drawText("Foto de prueba $numero", 80f, 460f, Paint().apply { color = Color.WHITE; textSize = 64f; isAntiAlias = true })
        escribir(contexto, bitmap)
    }.getOrNull()

    fun borrar(ruta: String) {
        runCatching { File(ruta).delete() }
    }

    private fun escribir(contexto: Context, original: Bitmap): String {
        val escala = LADO_MAX.toFloat() / maxOf(original.width, original.height)
        val bitmap = if (escala < 1f) Bitmap.createScaledBitmap(original, (original.width * escala).toInt(), (original.height * escala).toInt(), true) else original
        val archivo = File(carpeta(contexto), "${UUID.randomUUID()}.jpg")
        archivo.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        return archivo.absolutePath
    }
}
