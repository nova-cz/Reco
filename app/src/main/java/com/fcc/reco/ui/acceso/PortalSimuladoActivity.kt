package com.fcc.reco.ui.acceso

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.fcc.reco.R
import com.fcc.reco.data.DatosSimulados
import com.fcc.reco.databinding.ActivityPortalSimuladoBinding
import com.fcc.reco.domain.ValidadorAcceso
import com.fcc.reco.ui.comun.ajustarPorBarrasDelSistema
import com.fcc.reco.ui.comun.edgeToEdgeClaro

/**
 * Plan B de la verificación: página de login institucional simulada.
 * Valida el dominio y regresa a Reco con el deep link `reco://verificado?correo=...`.
 * La contraseña no se lee ni se guarda. Con el login real esto es una Chrome Custom Tab.
 */
class PortalSimuladoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        edgeToEdgeClaro()
        val b = ActivityPortalSimuladoBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.raiz.ajustarPorBarrasDelSistema()

        b.botonCerrar.setOnClickListener { finish() }
        // El mensaje de error se quita en cuanto el estudiante corrige el correo.
        b.campoCorreo.editText.addTextChangedListener { b.campoCorreo.ponerError(null) }
        b.botonEntrar.setOnClickListener {
            val correo = b.campoCorreo.texto.trim()
            if (!ValidadorAcceso.correoInstitucionalValido(correo)) {
                b.campoCorreo.ponerError(getString(R.string.error_dominio))
                return@setOnClickListener
            }
            val enlace = Uri.Builder().scheme("reco").authority("verificado")
                .appendQueryParameter("correo", correo.lowercase()).build()
            startActivity(
                Intent(Intent.ACTION_VIEW, enlace)
                    .setPackage(packageName)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
            finish()
        }
    }
}
