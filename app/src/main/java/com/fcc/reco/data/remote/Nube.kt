package com.fcc.reco.data.remote

import android.content.Context
import androidx.core.content.edit
import com.fcc.reco.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Cliente único de Supabase (Singleton) y el switch entre datos simulados y Supabase.
 * Las llaves salen de local.properties (BuildConfig); si faltan, la app usa solo datos simulados.
 */
object Nube {
    val configurada: Boolean get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    val cliente: SupabaseClient by lazy {
        createSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }

    /** Para escrituras "en segundo plano" (favoritos, confirmaciones) que no bloquean la pantalla. */
    val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ---- Switch de datos (solo se cambia desde el botón de debug de la Bienvenida) ----
    private const val PREFS = "reco_config"
    private const val USAR_NUBE = "usar_supabase"

    /** Por defecto usa Supabase si hay llaves. */
    fun usarNube(contexto: Context): Boolean =
        configurada && contexto.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(USAR_NUBE, true)

    fun fijarUsarNube(contexto: Context, usar: Boolean) =
        contexto.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putBoolean(USAR_NUBE, usar) }
}
