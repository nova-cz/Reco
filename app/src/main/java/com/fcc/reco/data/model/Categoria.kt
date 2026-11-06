package com.fcc.reco.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.fcc.reco.R

/** Categorías de electrónica de Reco, con su ícono y degradado del Figma. */
enum class Categoria(
    @StringRes val nombre: Int,
    @DrawableRes val icono: Int,
    @DrawableRes val degradado: Int
) {
    LAPTOPS(R.string.cat_laptops, R.drawable.ic_laptop, R.drawable.bg_degradado_laptops),
    CELULARES(R.string.cat_celulares, R.drawable.ic_celular, R.drawable.bg_degradado_celulares),
    TABLETS(R.string.cat_tablets, R.drawable.ic_tablet, R.drawable.bg_degradado_tablets),
    AUDIO(R.string.cat_audio, R.drawable.ic_audio, R.drawable.bg_degradado_audio),
    GAMING(R.string.cat_gaming, R.drawable.ic_gaming, R.drawable.bg_degradado_gaming),
    CALCULADORAS(R.string.cat_calculadoras, R.drawable.ic_calculadora, R.drawable.bg_degradado_calculadoras)
}
