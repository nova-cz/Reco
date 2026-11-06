package com.fcc.reco.data

import com.fcc.reco.data.repository.CarritoRepositorioSimulado
import com.fcc.reco.data.repository.CarritoRepository
import com.fcc.reco.data.repository.FavoritoRepository
import com.fcc.reco.data.repository.FavoritoRepositorySimulado
import com.fcc.reco.data.repository.ProductoRepository
import com.fcc.reco.data.repository.ProductoRepositorySimulado

/** Punto único de acceso a los repositorios (Singleton). Aquí se cambiará a Supabase. */
object Repositorios {
    val productos: ProductoRepository = ProductoRepositorySimulado()
    val favoritos: FavoritoRepository = FavoritoRepositorySimulado()
    val carrito: CarritoRepository = CarritoRepositorioSimulado()
}
