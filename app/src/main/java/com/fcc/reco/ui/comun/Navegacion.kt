package com.fcc.reco.ui.comun

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.navOptions
import com.fcc.reco.R

/** Navega con las animaciones de Reco; opcionalmente limpia la pila hasta [popHasta]. */
fun NavController.irA(
    destino: Int,
    argumentos: Bundle? = null,
    popHasta: Int? = null,
    inclusivo: Boolean = false
) {
    navigate(destino, argumentos, navOptions {
        anim {
            enter = R.anim.nav_entra
            exit = R.anim.nav_sale
            popEnter = R.anim.nav_pop_entra
            popExit = R.anim.nav_pop_sale
        }
        if (popHasta != null) popUpTo(popHasta) { this.inclusive = inclusivo }
    })
}
