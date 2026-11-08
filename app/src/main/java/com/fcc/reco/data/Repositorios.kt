package com.fcc.reco.data

import android.content.Context
import com.fcc.reco.data.local.AlmacenJson
import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.data.model.EstadoPedido
import com.fcc.reco.data.repository.CarritoRepositorioSimulado
import com.fcc.reco.data.repository.AnuncioRepository
import com.fcc.reco.data.repository.AnuncioRepositorySimulado
import com.fcc.reco.data.repository.CalificacionRepository
import com.fcc.reco.data.repository.CalificacionRepositorySimulado
import com.fcc.reco.data.repository.CarritoRepository
import com.fcc.reco.data.repository.FavoritoRepository
import com.fcc.reco.data.repository.FavoritoRepositorySimulado
import com.fcc.reco.data.repository.PagoRepository
import com.fcc.reco.data.repository.PagoRepositorySimulado
import com.fcc.reco.data.repository.PedidoRepository
import com.fcc.reco.data.repository.PedidoRepositorySimulado
import com.fcc.reco.data.repository.ProductoRepository
import com.fcc.reco.data.repository.ProductoRepositorySimulado

/**
 * Punto único de acceso a los repositorios (Singleton). Se inicia en RecoApp.
 * Aquí se cambiará la implementación simulada por la de Supabase.
 */
object Repositorios {
    lateinit var pedidos: PedidoRepository
        private set
    lateinit var productos: ProductoRepository
        private set
    lateinit var anuncios: AnuncioRepository
        private set
    lateinit var favoritos: FavoritoRepository
        private set
    lateinit var carrito: CarritoRepository
        private set
    lateinit var pagos: PagoRepository
        private set
    lateinit var calificaciones: CalificacionRepository
        private set

    fun iniciar(contexto: Context) {
        pedidos = PedidoRepositorySimulado(AlmacenJson(contexto, "pedidos"))
        val vendidos = {
            pedidos.pedidos.value.filter { it.estado != EstadoPedido.CANCELADO }.flatMap { it.productoIds }.toSet()
        }
        val sesion = SesionLocal(contexto)
        anuncios = AnuncioRepositorySimulado(AlmacenJson(contexto, "anuncios"), vendidos)
        productos = ProductoRepositorySimulado(vendidos, anuncios) { sesion.leer()?.correo }
        favoritos = FavoritoRepositorySimulado(AlmacenJson(contexto, "favoritos"))
        carrito = CarritoRepositorioSimulado(AlmacenJson(contexto, "carrito"), productos)
        pagos = PagoRepositorySimulado()
        calificaciones = CalificacionRepositorySimulado(AlmacenJson(contexto, "calificaciones"), pedidos)
    }
}
