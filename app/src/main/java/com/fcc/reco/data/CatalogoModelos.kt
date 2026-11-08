package com.fcc.reco.data

import com.fcc.reco.data.model.Categoria

/** Modelo del catálogo con su precio de referencia (sirve para validar el precio al publicar). */
data class ModeloCatalogo(
    val categoria: Categoria,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val precioReferencia: Int,
    val almacenamientos: List<String>,
    val rams: List<String>,
    val colores: List<String>,
    val procesador: String?,
    val pantalla: String?
) {
    val titulo: String get() = "$modelo · $anio"
}

/** 18 modelos (3 por categoría). Con Supabase sale de la tabla `modelos`. */
object CatalogoModelos {

    private fun m(
        c: Categoria, marca: String, modelo: String, anio: Int, ref: Int,
        alm: List<String>, ram: List<String>, colores: List<String>, proc: String?, pant: String?
    ) = ModeloCatalogo(c, marca, modelo, anio, ref, alm, ram, colores, proc, pant)

    val modelos: List<ModeloCatalogo> = listOf(
        m(Categoria.LAPTOPS, "Apple", "MacBook Air M1", 2020, 10500, listOf("256 GB", "512 GB"), listOf("8 GB", "16 GB"), listOf("Gris espacial", "Plata", "Oro"), "Apple M1", "13.3\""),
        m(Categoria.LAPTOPS, "Lenovo", "IdeaPad 3 Ryzen 5", 2022, 7500, listOf("256 GB", "512 GB"), listOf("8 GB", "16 GB"), listOf("Gris", "Azul"), "Ryzen 5 5500U", "15.6\""),
        m(Categoria.LAPTOPS, "HP", "Pavilion 14", 2021, 10500, listOf("512 GB"), listOf("8 GB", "16 GB"), listOf("Plata", "Negro"), "Core i5-1135G7", "14\""),
        m(Categoria.CELULARES, "Apple", "iPhone 12", 2020, 10000, listOf("64 GB", "128 GB", "256 GB"), listOf("4 GB"), listOf("Azul", "Negro", "Blanco", "Rojo"), "A14 Bionic", "6.1\""),
        m(Categoria.CELULARES, "Samsung", "Galaxy A54", 2023, 6500, listOf("128 GB", "256 GB"), listOf("6 GB", "8 GB"), listOf("Verde lima", "Negro", "Blanco"), "Exynos 1380", "6.4\""),
        m(Categoria.CELULARES, "Xiaomi", "Redmi Note 12", 2023, 4000, listOf("128 GB"), listOf("4 GB", "6 GB"), listOf("Gris", "Azul"), "Snapdragon 685", "6.67\""),
        m(Categoria.TABLETS, "Apple", "iPad 9.ª gen", 2021, 6500, listOf("64 GB", "256 GB"), listOf("3 GB"), listOf("Gris espacial", "Plata"), "A13 Bionic", "10.2\""),
        m(Categoria.TABLETS, "Samsung", "Galaxy Tab A8", 2022, 4000, listOf("32 GB", "64 GB"), listOf("3 GB", "4 GB"), listOf("Gris", "Plata"), "Unisoc T618", "10.5\""),
        m(Categoria.TABLETS, "Lenovo", "Tab M10", 2021, 2500, listOf("32 GB", "64 GB"), listOf("3 GB"), listOf("Gris"), "Helio P22T", "10.1\""),
        m(Categoria.AUDIO, "Apple", "AirPods Pro", 2019, 3500, emptyList(), emptyList(), listOf("Blanco"), "In-ear con cancelación de ruido", null),
        m(Categoria.AUDIO, "Sony", "WH-CH520", 2023, 1200, emptyList(), emptyList(), listOf("Negro", "Azul", "Blanco"), "On-ear inalámbricos", null),
        m(Categoria.AUDIO, "JBL", "Tune 510BT", 2021, 900, emptyList(), emptyList(), listOf("Negro", "Azul"), "On-ear inalámbricos", null),
        m(Categoria.GAMING, "Nintendo", "Switch V2", 2019, 5500, listOf("32 GB"), emptyList(), listOf("Neón", "Gris"), "Consola híbrida", "6.2\""),
        m(Categoria.GAMING, "Sony", "PlayStation 4 Slim", 2018, 4500, listOf("500 GB", "1 TB"), emptyList(), listOf("Negro"), "Consola de sobremesa", null),
        m(Categoria.GAMING, "Microsoft", "Xbox Series S", 2021, 6000, listOf("512 GB"), emptyList(), listOf("Blanco"), "Consola de sobremesa", null),
        m(Categoria.CALCULADORAS, "Casio", "fx-991EX ClassWiz", 2020, 600, emptyList(), emptyList(), listOf("Negro"), "Científica", "Alta resolución"),
        m(Categoria.CALCULADORAS, "Texas Instruments", "TI-84 Plus", 2018, 1800, emptyList(), emptyList(), listOf("Azul", "Negro"), "Graficadora", "128 × 64 px"),
        m(Categoria.CALCULADORAS, "HP", "Prime G2", 2018, 3000, emptyList(), emptyList(), listOf("Negro"), "Graficadora", "320 × 240 px")
    )

    fun deCategoria(c: Categoria) = modelos.filter { it.categoria == c }

    fun buscar(marca: String, modelo: String) = modelos.firstOrNull { it.marca == marca && it.modelo == modelo }
}
