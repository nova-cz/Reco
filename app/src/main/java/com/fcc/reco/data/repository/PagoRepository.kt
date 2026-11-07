package com.fcc.reco.data.repository

import com.fcc.reco.domain.Comision
import kotlinx.coroutines.delay

interface PagoRepository {
    fun calcularComision(monto: Int): Int
    /** Devuelve el id del pago. Aquí se simula; con Stripe sería un PaymentIntent. */
    suspend fun procesarPago(monto: Int, ultimos4: String): Result<String>
}

class PagoRepositorySimulado : PagoRepository {
    override fun calcularComision(monto: Int) = Comision.calcular(monto)

    override suspend fun procesarPago(monto: Int, ultimos4: String): Result<String> {
        delay(1200) // simula la red
        return Result.success("pi_sim_${System.currentTimeMillis()}")
    }
}
