package com.fcc.reco.data

import android.content.Context
import com.fcc.reco.data.local.AlmacenJson
import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.data.model.EstadoPedido
import com.fcc.reco.data.remote.Nube
import com.fcc.reco.data.remote.NubeDatos
import com.fcc.reco.data.repository.AnuncioRepository
import com.fcc.reco.data.repository.AnuncioRepositorySimulado
import com.fcc.reco.data.repository.AnuncioRepositorySupabase
import com.fcc.reco.data.repository.AuthRepository
import com.fcc.reco.data.repository.AuthRepositorySimulado
import com.fcc.reco.data.repository.AuthRepositorySupabase
import com.fcc.reco.data.repository.CalificacionRepository
import com.fcc.reco.data.repository.CalificacionRepositorySimulado
import com.fcc.reco.data.repository.CalificacionRepositorySupabase
import com.fcc.reco.data.repository.FavoritoRepository
import com.fcc.reco.data.repository.FavoritoRepositorySimulado
import com.fcc.reco.data.repository.FavoritoRepositorySupabase
import com.fcc.reco.data.repository.PagoRepository
import com.fcc.reco.data.repository.PagoRepositorySimulado
import com.fcc.reco.data.repository.PedidoRepository
import com.fcc.reco.data.repository.PedidoRepositorySimulado
import com.fcc.reco.data.repository.PedidoRepositorySupabase
import com.fcc.reco.data.repository.PerfilRepository
import com.fcc.reco.data.repository.PerfilRepositorySimulado
import com.fcc.reco.data.repository.PerfilRepositorySupabase
import com.fcc.reco.data.repository.ProductoRepository
import com.fcc.reco.data.repository.ProductoRepositorySimulado
import com.fcc.reco.data.repository.ProductoRepositorySupabase
import com.fcc.reco.data.repository.SesionNube
import com.fcc.reco.data.repository.VerificacionRepository
import com.fcc.reco.data.repository.VerificacionRepositorySimulado
import com.fcc.reco.data.repository.VerificacionRepositorySupabase
import io.github.jan.supabase.auth.auth

/**
 * Punto único de acceso a los repositorios (Singleton). Se inicia en RecoApp.
 * Según el switch de [Nube] usa las implementaciones simuladas (JSON local) o las de Supabase.
 * Con Supabase todo es real: cuenta, catálogo, favoritos, pedidos, reseñas y los anuncios del vendedor (los borradores
 * se guardan en el teléfono hasta enviarlos a revisión).
 */
object Repositorios {
    lateinit var auth: AuthRepository
        private set
    lateinit var verificacion: VerificacionRepository
        private set
    lateinit var perfil: PerfilRepository
        private set
    lateinit var pedidos: PedidoRepository
        private set
    lateinit var productos: ProductoRepository
        private set
    lateinit var anuncios: AnuncioRepository
        private set
    lateinit var favoritos: FavoritoRepository
        private set
    lateinit var pagos: PagoRepository
        private set
    lateinit var calificaciones: CalificacionRepository
        private set

    /** Verdadero si esta ejecución usa Supabase. */
    var usaNube: Boolean = false
        private set

    /** En modo Supabase, falso hasta que [precargar] termine; en modo simulado siempre es verdadero. */
    var listo: Boolean = true
        private set

    private lateinit var local: SesionLocal
    private var nubeProductos: ProductoRepositorySupabase? = null
    private var nubePedidos: PedidoRepositorySupabase? = null
    private var nubeFavoritos: FavoritoRepositorySupabase? = null
    private var nubeCalificaciones: CalificacionRepositorySupabase? = null
    private var nubeDatos: NubeDatos? = null
    private var nubeAnuncios: AnuncioRepositorySupabase? = null
    private var sesionNube: SesionNube? = null

    fun iniciar(contexto: Context) {
        local = SesionLocal(contexto)
        usaNube = Nube.usarNube(contexto)
        pagos = PagoRepositorySimulado()
        val anunciosJson = AlmacenJson(contexto, "anuncios")
        if (usaNube) iniciarNube(anunciosJson) else iniciarSimulado(contexto, anunciosJson)
    }

    private fun iniciarSimulado(contexto: Context, anunciosJson: AlmacenJson) {
        listo = true
        auth = AuthRepositorySimulado(local)
        verificacion = VerificacionRepositorySimulado(local)
        perfil = PerfilRepositorySimulado(local)
        pedidos = PedidoRepositorySimulado(AlmacenJson(contexto, "pedidos"))
        val vendidos = {
            pedidos.pedidos.value.filter { it.estado != EstadoPedido.CANCELADO }.flatMap { it.productoIds }.toSet()
        }
        anuncios = AnuncioRepositorySimulado(anunciosJson, vendidos)
        productos = ProductoRepositorySimulado(vendidos, anuncios) { local.leer()?.correo }
        favoritos = FavoritoRepositorySimulado(AlmacenJson(contexto, "favoritos"))
        calificaciones = CalificacionRepositorySimulado(AlmacenJson(contexto, "calificaciones"), pedidos) { productos.obtenerPorId(it)?.titulo.orEmpty() }
    }

    private fun iniciarNube(anunciosJson: AlmacenJson) {
        listo = false
        val datos = NubeDatos()
        val sesion = SesionNube(local)
        val prod = ProductoRepositorySupabase(datos)
        val ped = PedidoRepositorySupabase(datos, prod)
        val fav = FavoritoRepositorySupabase(datos, prod)
        val cal = CalificacionRepositorySupabase(datos, ped)
        nubeDatos = datos; sesionNube = sesion
        nubeProductos = prod; nubePedidos = ped; nubeFavoritos = fav; nubeCalificaciones = cal
        auth = AuthRepositorySupabase(local, sesion)
        verificacion = VerificacionRepositorySupabase(sesion)
        perfil = PerfilRepositorySupabase(local, sesion, datos)
        productos = prod; pedidos = ped; favoritos = fav; calificaciones = cal
        val anun = AnuncioRepositorySupabase(AnuncioRepositorySimulado(anunciosJson) { emptySet() }, datos, local)
        nubeAnuncios = anun; anuncios = anun
    }

    /**
     * Carga desde Supabase todo lo que las pantallas leen de forma síncrona (catálogo, pedidos, favoritos, reseñas) y
     * deja la sesión local al día. Devuelve falso si no hay sesión de Supabase (hay que iniciar sesión otra vez).
     */
    suspend fun precargar(): Boolean {
        if (!usaNube) return true
        val prod = nubeProductos ?: return true
        Nube.cliente.auth.awaitInitialization()
        if (Nube.cliente.auth.currentSessionOrNull() == null) return false
        nubeDatos!!.cargarReferencias()
        sesionNube!!.refrescar()
        prod.refrescar()
        nubePedidos!!.refrescar()
        nubeFavoritos!!.cargar()
        runCatching { nubeAnuncios!!.refrescar() }
        runCatching { nubeCalificaciones!!.cargar() }
        listo = true
        return true
    }

    /** Cierra la sesión (y la de Supabase) y deja listo el estado para entrar con otra cuenta. */
    fun cerrarSesion() {
        auth.cerrarSesion()
        if (usaNube) listo = false
    }
}
