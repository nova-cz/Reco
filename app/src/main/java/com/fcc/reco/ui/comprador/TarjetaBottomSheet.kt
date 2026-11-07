package com.fcc.reco.ui.comprador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.fcc.reco.R
import com.fcc.reco.databinding.SheetTarjetaBinding
import com.fcc.reco.domain.ValidadorPago
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Calendar

/** Captura la tarjeta de prueba (validada, pero no se guarda más que los últimos 4 dígitos). */
class TarjetaBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: EntregaPagoViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override fun onCreateView(inflater: LayoutInflater, contenedor: ViewGroup?, estado: Bundle?): View {
        val b = SheetTarjetaBinding.inflate(inflater, contenedor, false)
        b.botonGuardar.setOnClickListener {
            val hoy = Calendar.getInstance()
            val numeroOk = ValidadorPago.numeroValido(b.campoNumero.texto)
            val venceOk = ValidadorPago.vencimientoValido(b.campoVence.texto, hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH) + 1)
            val cvcOk = ValidadorPago.cvcValido(b.campoCvc.texto)
            b.campoNumero.ponerError(if (numeroOk) null else getString(R.string.error_tarjeta_numero))
            b.campoVence.ponerError(if (venceOk) null else getString(R.string.error_tarjeta_vence))
            b.campoCvc.ponerError(if (cvcOk) null else getString(R.string.error_tarjeta_cvc))
            if (numeroOk && venceOk && cvcOk) {
                viewModel.cambiarTarjeta(ValidadorPago.ultimos4(b.campoNumero.texto))
                dismiss()
            }
        }
        return b.root
    }

    companion object {
        const val TAG = "tarjeta_sheet"
    }
}
