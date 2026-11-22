package com.fcc.reco.data.repository

import com.fcc.reco.data.remote.FavoritoDto
import com.fcc.reco.data.remote.IdMapa
import com.fcc.reco.data.remote.Nube
import com.fcc.reco.data.remote.NubeDatos
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Favoritos en la tabla `favoritos`. Se actualiza en pantalla al instante y se guarda en segundo plano. */
class FavoritoRepositorySupabase(
    private val datos: NubeDatos,
    private val productos: ProductoRepositorySupabase
) : FavoritoRepository {

    private val _ids = MutableStateFlow<Set<Int>>(emptySet())
    override val ids: StateFlow<Set<Int>> = _ids.asStateFlow()

    suspend fun cargar() {
        val uuids = Nube.cliente.from("favoritos").select().decodeList<FavoritoDto>().map { it.productoId }
        productos.asegurar(uuids)
        _ids.value = uuids.map { IdMapa.id(it) }.toSet()
    }

    override fun alternar(productoId: Int) {
        val uuid = IdMapa.uuid(productoId) ?: return
        val yaEra = productoId in _ids.value
        _ids.update { if (yaEra) it - productoId else it + productoId }
        val uid = datos.miUid ?: return
        Nube.alcance.launch {
            runCatching {
                if (yaEra) {
                    Nube.cliente.from("favoritos").delete { filter { eq("usuario_id", uid); eq("producto_id", uuid) } }
                } else {
                    Nube.cliente.from("favoritos").insert(buildJsonObject { put("usuario_id", uid); put("producto_id", uuid) })
                }
            }.onFailure { _ids.update { if (yaEra) it + productoId else it - productoId } }  // deshace si falló
        }
    }
}
