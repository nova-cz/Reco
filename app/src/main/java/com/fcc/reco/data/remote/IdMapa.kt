package com.fcc.reco.data.remote

import java.time.OffsetDateTime

/**
 * La app usa ids Int en pantallas y argumentos de navegación; Supabase usa UUID.
 * Este mapa asigna un Int estable (durante el proceso) a cada UUID y permite volver.
 */
object IdMapa {
    private val aInt = HashMap<String, Int>()
    private val aUuid = HashMap<Int, String>()

    @Synchronized
    fun id(uuid: String): Int = aInt.getOrPut(uuid) { (aInt.size + 1).also { aUuid[it] = uuid } }

    @Synchronized
    fun uuid(id: Int): String? = aUuid[id]
}

/** "2026-11-22T03:46:20.66+00:00" -> milisegundos. Si no se entiende, ahora. */
fun epochDe(iso: String): Long = runCatching { OffsetDateTime.parse(iso).toInstant().toEpochMilli() }
    .getOrDefault(System.currentTimeMillis())
