package com.fcc.reco.data

import com.fcc.reco.data.model.Categoria
import com.fcc.reco.data.model.Especificacion
import com.fcc.reco.data.model.Grado
import com.fcc.reco.data.model.Producto
import com.fcc.reco.data.model.PuntoEntrega
import com.fcc.reco.data.model.Universidad
import com.fcc.reco.data.model.VendedorResumen

/** Datos de prueba en memoria. Se reemplazan por Supabase más adelante. */
object DatosSimulados {
    val universidades = listOf(Universidad("BUAP", "alumno.buap.mx"))

    val carreras = listOf(
        "Ing. en Ciencias de la Computación",
        "Ing. en Tecnologías de la Información",
        "Lic. en Ciencias de la Computación",
        "Ing. Mecatrónica",
        "Ing. Civil",
        "Ing. Industrial",
        "Administración de Empresas",
        "Diseño Gráfico",
        "Otra carrera"
    )

    val puntosEntrega = listOf(
        PuntoEntrega("Biblioteca Central", "Ciudad Universitaria · planta baja"),
        PuntoEntrega("Cafetería FCC", "Edificio CCO1")
    )

    val horariosEntrega = listOf("Lun 11:00", "Mar 13:00", "Mié 16:00", "Jue 12:00")

    private val vendedores = listOf(
        VendedorResumen("Andrea R.", "Computación", "FCC", 4.9, 12),
        VendedorResumen("Luis M.", "Electrónica", "FCE", 4.7, 8),
        VendedorResumen("Sofía G.", "Diseño", "FAD", 4.8, 5),
        VendedorResumen("Diego P.", "Mecatrónica", "FI", 4.6, 3),
        VendedorResumen("Valeria C.", "Computación", "FCC", 5.0, 21)
    )
    private val puntos = listOf("Cafetería FCC · CU", "Biblioteca Central · CU", "Cafetería FCC · CU", "Biblioteca Central · CU", "Cafetería FCC · CU")

    private fun p(
        id: Int, cat: Categoria, marca: String, modelo: String, anio: Int, alm: String?, ram: String?,
        color: String, grado: Grado, bat: Int?, precio: Int, cert: Boolean, vend: Int,
        proc: String?, pant: String?, imei: String?, dias: Int, accesorios: String
    ): Producto {
        val specs = listOfNotNull(
            proc?.let { Especificacion(if (cat == Categoria.AUDIO || cat == Categoria.CALCULADORAS) "Tipo" else "Procesador", it) },
            ram?.let { Especificacion("Memoria", it) },
            alm?.let { Especificacion("Almacenamiento", it) },
            pant?.let { Especificacion("Pantalla", it) }
        )
        return Producto(
            id, cat, marca, modelo, anio, alm, ram, color, grado, bat, precio, cert, vendedores[vend],
            specs, accesorios, imei, puntos[vend], dias
        )
    }

    val productos: List<Producto> = listOf(
        // Laptops
        p(1, Categoria.LAPTOPS, "Apple", "MacBook Air M1", 2020, "256 GB", "8 GB", "Gris espacial", Grado.A, 89, 9800, true, 0, "Apple M1", "13.3\"", null, 1, "cargador original"),
        p(2, Categoria.LAPTOPS, "Lenovo", "IdeaPad 3 Ryzen 5", 2022, "512 GB", "8 GB", "Gris", Grado.B, 80, 6500, false, 1, "Ryzen 5 5500U", "15.6\"", null, 2, "cargador"),
        p(3, Categoria.LAPTOPS, "HP", "Pavilion 14", 2021, "512 GB", "16 GB", "Plata", Grado.A, 85, 8900, true, 4, "Core i5-1135G7", "14\"", null, 3, "cargador y funda"),
        p(4, Categoria.LAPTOPS, "Dell", "Inspiron 15", 2020, "256 GB", "8 GB", "Negro", Grado.B, 72, 5200, false, 3, "Core i3-1005G1", "15.6\"", null, 5, "cargador"),
        p(5, Categoria.LAPTOPS, "ASUS", "VivoBook 15", 2022, "512 GB", "8 GB", "Azul", Grado.B, 78, 7400, false, 2, "Core i5-1235U", "15.6\"", null, 6, "cargador y mouse"),
        p(6, Categoria.LAPTOPS, "Apple", "MacBook Pro 13\"", 2019, "256 GB", "8 GB", "Gris espacial", Grado.B, 76, 12400, true, 0, "Core i5", "13.3\"", null, 7, "cargador original"),
        p(7, Categoria.LAPTOPS, "Lenovo", "ThinkPad E14", 2021, "512 GB", "16 GB", "Negro", Grado.A, 88, 9200, false, 4, "Ryzen 5 5500U", "14\"", null, 9, "cargador"),
        p(8, Categoria.LAPTOPS, "Acer", "Aspire 5", 2021, "512 GB", "8 GB", "Plata", Grado.C, 70, 5200, false, 3, "Core i5-1135G7", "15.6\"", null, 11, "cargador"),
        // Celulares
        p(9, Categoria.CELULARES, "Apple", "iPhone 12", 2020, "128 GB", "4 GB", "Azul", Grado.A, 87, 8900, true, 0, "A14 Bionic", "6.1\"", "4821", 1, "cable y caja"),
        p(10, Categoria.CELULARES, "Samsung", "Galaxy A54", 2023, "128 GB", "6 GB", "Verde lima", Grado.A, 92, 5600, false, 1, "Exynos 1380", "6.4\"", "3102", 2, "cargador y funda"),
        p(11, Categoria.CELULARES, "Apple", "iPhone 11", 2019, "64 GB", "4 GB", "Negro", Grado.B, 81, 5200, false, 4, "A13 Bionic", "6.1\"", "7714", 4, "cable"),
        p(12, Categoria.CELULARES, "Xiaomi", "Redmi Note 12", 2023, "128 GB", "6 GB", "Gris", Grado.B, 90, 3300, false, 2, "Snapdragon 685", "6.67\"", "9036", 5, "cargador y funda"),
        p(13, Categoria.CELULARES, "Samsung", "Galaxy S21", 2021, "128 GB", "8 GB", "Violeta", Grado.B, 84, 6900, true, 3, "Exynos 2100", "6.2\"", "2250", 6, "cargador"),
        p(14, Categoria.CELULARES, "Motorola", "Moto G84", 2023, "256 GB", "8 GB", "Azul", Grado.A, 95, 4300, false, 1, "Snapdragon 695", "6.5\"", "5528", 8, "cargador y funda"),
        p(15, Categoria.CELULARES, "Apple", "iPhone SE", 2022, "64 GB", "4 GB", "Rojo", Grado.A, 83, 5900, false, 0, "A15 Bionic", "4.7\"", "1173", 10, "cable y caja"),
        p(16, Categoria.CELULARES, "Samsung", "Galaxy A34", 2023, "128 GB", "6 GB", "Plata", Grado.B, 88, 4500, false, 2, "Dimensity 1080", "6.6\"", "6409", 12, "cargador"),
        // Tablets
        p(17, Categoria.TABLETS, "Apple", "iPad 9.ª gen", 2021, "64 GB", "3 GB", "Gris espacial", Grado.A, 90, 5400, true, 0, "A13 Bionic", "10.2\"", "2748", 3, "cargador y funda"),
        p(18, Categoria.TABLETS, "Samsung", "Galaxy Tab A8", 2022, "64 GB", "4 GB", "Gris", Grado.B, 85, 3200, false, 1, "Unisoc T618", "10.5\"", "8801", 6, "cargador"),
        p(19, Categoria.TABLETS, "Lenovo", "Tab M10", 2021, "32 GB", "3 GB", "Gris", Grado.C, 74, 1800, false, 3, "Helio P22T", "10.1\"", "3359", 9, "cargador"),
        p(20, Categoria.TABLETS, "Apple", "iPad Air 4", 2020, "64 GB", "4 GB", "Azul cielo", Grado.A, 86, 8700, true, 4, "A14 Bionic", "10.9\"", "6612", 2, "cargador y Apple Pencil"),
        // Audio
        p(21, Categoria.AUDIO, "Apple", "AirPods Pro", 2019, null, null, "Blanco", Grado.B, 82, 2400, false, 0, "In-ear con cancelación de ruido", null, null, 4, "estuche y cable"),
        p(22, Categoria.AUDIO, "Sony", "WH-CH520", 2023, null, null, "Negro", Grado.A, null, 800, false, 1, "On-ear inalámbricos", null, null, 7, "cable de carga"),
        p(23, Categoria.AUDIO, "JBL", "Tune 510BT", 2021, null, null, "Azul", Grado.B, null, 600, false, 2, "On-ear inalámbricos", null, null, 10, "cable de carga"),
        p(24, Categoria.AUDIO, "Samsung", "Galaxy Buds2", 2021, null, null, "Grafito", Grado.A, 90, 1300, true, 3, "In-ear con cancelación de ruido", null, null, 5, "estuche"),
        // Gaming
        p(25, Categoria.GAMING, "Nintendo", "Switch V2", 2019, "32 GB", null, "Neón", Grado.A, 88, 4800, true, 4, "Consola híbrida", "6.2\"", null, 2, "dock, cargador y 2 Joy-Con"),
        p(26, Categoria.GAMING, "Sony", "PlayStation 4 Slim", 2018, "500 GB", null, "Negro", Grado.B, null, 3600, false, 1, "Consola de sobremesa", null, null, 8, "control y cables"),
        p(27, Categoria.GAMING, "Microsoft", "Xbox Series S", 2021, "512 GB", null, "Blanco", Grado.A, null, 5200, false, 0, "Consola de sobremesa", null, null, 6, "control y cables"),
        p(28, Categoria.GAMING, "Sony", "DualSense PS5", 2022, null, null, "Blanco", Grado.B, null, 900, false, 2, "Control inalámbrico", null, null, 13, "cable USB-C"),
        // Calculadoras
        p(29, Categoria.CALCULADORAS, "Casio", "fx-991EX ClassWiz", 2020, null, null, "Negro", Grado.A, null, 450, false, 3, "Científica", "Alta resolución", null, 3, "tapa protectora"),
        p(30, Categoria.CALCULADORAS, "Texas Instruments", "TI-84 Plus", 2018, null, null, "Azul", Grado.B, null, 1400, false, 4, "Graficadora", "128 × 64 px", null, 9, "tapa y pilas")
    )
}
