package com.fcc.reco.ui.comprador

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fcc.reco.R
import com.fcc.reco.data.local.SesionLocal
import com.fcc.reco.data.model.ModoUsuario
import com.fcc.reco.data.repository.PerfilRepositorySimulado
import com.fcc.reco.databinding.ActivityModoPlaceholderBinding
import com.fcc.reco.ui.acceso.AccesoActivity
import com.fcc.reco.ui.acceso.abrirModoYTerminar
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.comun.edgeToEdgeClaro

/** Marcador temporal: la Fase 3 lo reemplaza por la navegación real del comprador. */
class CompradorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        edgeToEdgeClaro()
        val b = ActivityModoPlaceholderBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.raiz.ajustarPorBarrasDelSistema()

        val local = SesionLocal(this)
        val sesion = local.leer()
        b.titulo.setText(R.string.modo_comprador)
        b.detalle.text = listOfNotNull(sesion?.nombre, sesion?.carrera, sesion?.universidad).joinToString("\n")
        b.botonCambiar.setText(R.string.cambiar_a_vendedor)
        b.botonCambiar.setOnClickListener {
            PerfilRepositorySimulado(local).cambiarModo(ModoUsuario.VENDEDOR)
            abrirModoYTerminar(ModoUsuario.VENDEDOR)
        }
        b.botonSalir.setOnClickListener {
            local.borrar()
            startActivity(Intent(this, AccesoActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
            finish()
        }
    }
}
