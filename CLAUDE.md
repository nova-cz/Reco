# Reco · Contexto del proyecto (diseño v2)

App Android de compraventa de electrónica usada **entre estudiantes universitarios verificados**, con pago retenido, revisión de calidad y entrega en el campus. Proyecto final de Desarrollo de Aplicaciones Móviles, BUAP, Otoño 2026. Profesor: Abraham Sánchez López. Autor: Miguel Angel Cruz Reyes.

La fuente completa es el documento ERS (IEEE 830). Este archivo resume el estado real del proyecto al 22 de noviembre de 2026.

## Enlaces

- ERS IEEE 830: https://claude.ai/artifact/K8hGNr9fTTz7ATs7H8Una5
- Figma: https://www.figma.com/design/3dFv6cBGuMqbBVZS0wD9YL, sección **"Reco v2 (simplificada)"** (nodo `48:277`). Es la referencia vigente; la página `ReCampus` es el diseño v1 (descartado).
- Repo: `github.com/nova-cz/Reco` (rama `main`). Paquete `com.fcc.reco`, minSdk 26, targetSdk 37.
- Supabase: proyecto `reco`, ref `otkbruduudshhnoagadi`, https://otkbruduudshhnoagadi.supabase.co

## Entregas

| Entrega | Fechas | Estado |
| --- | --- | --- |
| 1a · Auditoría de apps similares | 11 y 13 nov | Lista (Apéndice A del ERS) |
| 2a · Mapas mentales y bocetos | 18 y 20 nov | Mapas en Figma |
| 3a · Primer avance de programación | 23 y 24 nov | Acceso, compra y cuenta funcionando; con Supabase real o con datos simulados |
| Final | Calendario DAE | ERS completa y app |

## Concepto

- **Reco**. Mascota: camaleón con lentes. Logo verde azulado.
- Ninguna app similar junta las cinco cosas: solo estudiantes verificados, pago retenido, revisión de calidad, entrega en un punto del campus y solo electrónica.
- Categorías: Laptops, Celulares, Tablets, Audio, Gaming, Calculadoras. Los accesorios se muestran dentro de Audio.

## Diseño v2: reglas

- **Sin modos**: una sola cuenta y una sola `MainActivity` con `nav_main`.
- **Compra directa** de un equipo, sin carrito.
- Barra inferior **Inicio · Favoritos · Compras · Cuenta** más el botón **Vender** aparte (abre "Vende en Reco" si aún no eres vendedor, o "Mis ventas" si ya lo eres).
- **Preguntas rápidas** en Detalle (¿dónde entregas?, ¿horario?, ¿aceptas tarjeta?). Sin chat.
- **Comentario obligatorio** al calificar con 3 estrellas o menos (en la app y en la base de datos).
- **Publicar en 3 pasos** (Tu equipo, Fotos, Precio) con "Enviar a revisión".
- Botones principales con degradado `#0E9AA7 → #1FA463`.

Las 18 pantallas en 5 flujos: **Entrar** (Bienvenida, Iniciar sesión, Verificar estudiante), **Comprar** (Inicio, Resultados, Detalle, Comprar, Favoritos), **Mis compras** (Mis compras, Detalle de pedido, Calificar), **Vender** (Vende en Reco, Mis ventas, Publicar 1, 2 y 3) y **Cuenta** (Cuenta, Perfil del vendedor).

## Verificación de estudiante

- Portal institucional **simulado dentro de la app** (`PortalSimuladoActivity`), que regresa por el deep link `reco://verificado?correo=...`.
- Con Supabase, la app llama a `verificar_estudiante(p_correo)`, que **solo comprueba que el correo termine en `@alumno.buap.mx`** (tabla `universidades`). No hay Azure ni Google. Al ser simulada, el servidor confía en el correo que manda la app: sirve para el proyecto, no es seguridad real.
- Sin verificación no se puede comprar ni vender (`crear_pedido` y `activar_vendedor` lo exigen).

## Datos: simulados o Supabase (switch)

`data/Repositorios.kt` elige las implementaciones al iniciar la app:

- **Supabase** (por defecto si hay llaves en `local.properties`): cuenta y perfil, verificación, catálogo con fotos, favoritos, pedidos y reseñas.
- **Simulados** (JSON internos en `filesDir`): sin red ni llaves.
- **Switch de debug**: en la Bienvenida, solo en builds debug, el texto "DEBUG · Datos: ..." alterna entre ambos y cierra la sesión.
- **Anuncios del vendedor** (Mis ventas, Publicar): siguen **locales** con ambos modos. Falta conectarlos a la tabla `productos` y a Storage.

Las pantallas leen los repositorios de forma síncrona, así que en modo Supabase se **precargan** antes de abrir la app (`Repositorios.precargar()`, desde `AccesoViewModel`/`AccesoActivity`). Los ids son `Int` en la app y UUID en Supabase: `data/remote/IdMapa.kt` los traduce. Favoritos, confirmar entrega y calificar se actualizan en pantalla al instante y se guardan en segundo plano.

### Archivos clave

```
app/src/main/java/com/fcc/reco/data/
  Repositorios.kt            # elige simulado o Supabase; precargar()
  remote/                    # Nube (cliente, switch), NubeDatos, IdMapa, Dto
  repository/*Supabase.kt    # implementaciones reales (Producto, Pedido, Favorito, Calificacion, Cuenta)
  repository/*Simulado       # implementaciones locales
supabase/
  config.toml                # solo declara lo que se empuja (confirmación de correo desactivada)
  migrations/                # esquema (init), seguridad del IMEI y vista resenas_publicas
  seed.sql                   # generado por scripts/seed_dummyjson.py
scripts/
  seed_dummyjson.py          # baja productos de DummyJSON, sube fotos a Storage y genera seed.sql
  seed_base.sql              # catálogos base (universidad, puntos, modelos, IMEI bloqueados)
```

### Llaves (`local.properties`, no se sube a git)

`SUPABASE_URL`, `SUPABASE_ANON_KEY` (llegan a la app por `buildConfigField`), `SUPABASE_SERVICE_ROLE_KEY` y `SUPABASE_DB_PASSWORD` (solo para scripts y la CLI; **nunca** van en la app).

## Backend (Supabase)

- Se cambia con migraciones: `supabase migration new <nombre>` y `supabase db push --remote`. La CLI ya está enlazada (`supabase link`).
- **Datos de prueba**: `python3 scripts/seed_dummyjson.py` (25 productos de DummyJSON, 91 fotos en el bucket público `fotos`, 4 vendedores ficticios de la BUAP con correos `@example.invalid` y bloqueados) y luego `supabase db push --include-seed --remote`. Se puede repetir sin duplicar.
- **Precios** de prueba: USD × 18 × 0.65, redondeado a centenas.
- **Tablas**: universidades, puntos_entrega, modelos, imei_bloqueados, perfiles, productos, fotos, favoritos, reportes, pedidos (un pedido = un equipo), calificaciones. No hay carrito.
- **Perfiles de vendedor**: `apodo`, `acerca_de` y las respuestas rápidas `donde_entregas`, `horario`, `acepta_tarjeta`. Los demás ven solo la vista `perfiles_publicos` (sin teléfono ni CLABE).
- **Funciones** (las llama la app con la sesión del usuario): `verificar_estudiante(p_correo)`, `activar_vendedor(...)`, `crear_pedido(p_producto, p_punto, p_horario)`, `marcar_entregado(p_pedido)`, `confirmar_entrega(p_pedido)`, `obtener_mi_imei(p_producto)`. El monto y la comisión (13.6 % + $8) los calcula el servidor.
- **IMEI**: la columna `imei` solo la lee su dueño (con `obtener_mi_imei`); los demás ven `imei_ultimos4`. Por eso las consultas a `productos` deben listar columnas, no usar `select=*`.
- **Anónimo (anon key)**: puede leer solo anuncios publicados, sus fotos, modelos y puntos de entrega. Todo lo demás exige sesión.
- **Reseñas**: la vista `resenas_publicas` expone quién calificó, a quién y qué equipo.
- **Moderador**: sin pantallas. Aprueba o rechaza anuncios desde el panel de Supabase (`update productos set estado = 'publicado' where id = ...`).
- **Auth**: correo y contraseña, sin confirmación por correo (para la demo). Google Sign-In real no está hecho.

## Reglas de publicación

1. Solo electrónica que encienda y funcione. Prohibido: bloqueo de iCloud o Google, IMEI reportado, réplicas, baterías infladas, equipos para piezas.
2. Celulares y tablets llevan IMEI de 15 dígitos con Luhn; el resto, número de serie. No puede estar bloqueado ni repetido en otro anuncio vivo. Al comprador solo se le muestran los últimos 4 dígitos.
3. De 2 a 8 fotos reales.
4. Precio entre 30 % y 100 % del precio de referencia del modelo; fuera de ese rango va a revisión manual.
5. Grados: A Excelente, B Muy bueno, C Bueno.
6. Máximo 3 anuncios activos por vendedor (la carga inicial de prueba lo salta).
7. Con 3 reportes el anuncio vuelve a revisión; con 3 rechazos la cuenta se suspende.

Estados del anuncio: Borrador, En revisión, Publicado, Rechazado, Reservado, Vendido, Pausado.

## Pedidos y pago

- **Pago simulado** (pantalla de tarjeta propia con Luhn; tarjeta de prueba 4242 4242 4242 4242). No hay Stripe ni Edge Function.
- Estados: pagado_retenido → entregado (lo marca el vendedor) → liberado (lo confirma el comprador, que luego califica). También en_reclamo y cancelado.
- Cada equipo es pieza única: sale del catálogo cuando se compra.
- Comisión **13.6 % + $8 MXN** por pedido (`domain/Comision.kt` y `calcular_comision` en la base).
- Puntos de entrega: Biblioteca Central, Cafetería FCC, Explanada de Ingeniería y Comedor universitario. Horario 10:00 a 18:00.

## Stack y convenciones

- Kotlin, XML con Fragments, MVVM con repositorios (interfaces con implementación `...Simulado` y `...Supabase`), Navigation Component, StateFlow y corrutinas.
- supabase-kt (Auth, Postgrest, Storage) con Ktor OkHttp, kotlinx.serialization y Coil para las fotos por URL.
- Interfaz y textos en español de México; nombres de clases y métodos en español. Textos en `strings.xml`.
- Un ViewModel por pantalla o flujo; eventos de una sola vez por `Channel`; los fragments recolectan con `repeatOnLifecycle(STARTED)`.
- Tema siempre claro, app bloqueada en vertical, fuente Inter, íconos Lucide como vector drawables.
- Compilar y probar: `./gradlew assembleDebug testDebugUnitTest`.
- Commits en español, a nombre de Miguel Angel Cruz Reyes, **sin** `Co-Authored-By` ni menciones a Claude. No hacer `git push` salvo que Miguel lo pida. `.idea/` y `local.properties` no se suben.
- Ningún secreto en el repositorio.

## Pendientes

1. **Anuncios del vendedor en Supabase**: guardar borradores y enviar a revisión en `productos`, subir las fotos a `fotos/<uid>/...` y guardar la URL pública en `fotos.url`, y leer "Mis ventas" desde `pedidos`.
2. **Preguntas rápidas** del Detalle con las respuestas reales del vendedor (`donde_entregas`, `horario`, `acepta_tarjeta`); hoy usan textos de plantilla.
3. Google Sign-In real (Credential Manager), solo si sobra tiempo.
4. Login institucional real (Microsoft o Google de la BUAP) en lugar del portal simulado.
5. Puntos de entrega reales de CU.
6. Menores: "Mis datos" y "Puntos de entrega" de Cuenta, foto de perfil, reportar un problema en un pedido.

## Fuera de alcance

Chat en tiempo real, ofertas, pasarela de pago real, consulta real de IMEI, notificaciones, meses sin intereses y recompra al terminar la carrera.
