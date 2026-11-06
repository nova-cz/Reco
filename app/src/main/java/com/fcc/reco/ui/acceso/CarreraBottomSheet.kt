package com.fcc.reco.ui.acceso

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.fcc.reco.data.DatosSimulados
import com.fcc.reco.databinding.SheetCarreraBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/** Hoja para elegir la carrera después de verificarse. No se puede cerrar sin elegir. */
class CarreraBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: AccesoViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, contenedor: ViewGroup?, estado: Bundle?): View {
        isCancelable = false
        val b = SheetCarreraBinding.inflate(inflater, contenedor, false)
        b.lista.layoutManager = LinearLayoutManager(requireContext())
        b.lista.adapter = CarreraAdapter(DatosSimulados.carreras) { carrera ->
            viewModel.elegirCarrera(carrera)
            dismiss()
        }
        return b.root
    }

    companion object {
        const val TAG = "carrera_sheet"
    }
}
