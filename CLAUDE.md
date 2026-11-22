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

Sin verificación no se puede comprar ni vender (`crear_pedido` y `activar_vendedor` lo exigen). Quien decide es el servidor, con `verificar_estudiante()` (sin parámetros): **la app ya no manda ningún correo**.

- **Plan A, Microsoft** (la BUAP usa Microsoft 365): botón "Verificar con mi correo BUAP". La app usa `auth.linkIdentity(Azure)` (o `signInWith(Azure)` si no hay sesión) con scope `email` y regresa por el deep link `reco://auth`. El servidor exige una identidad `azure` en `auth.identities` cuyo correo termine en un dominio de `dominios_permitidos` y, si la fila tiene `azure_tenant_id`, que el `iss` sea de ese tenant.
- **Plan B, código por correo**: "Recibir un código en mi correo" (`signInWith(OTP)` y `verifyEmailOtp`). El servidor exige que la sesión se haya abierto con un código OTP (`amr` del JWT) y que el correo de la cuenta, ya confirmado, sea de un dominio permitido. Un registro con contraseña (que entra sin confirmar) **no** basta.
- **Dominios**: tabla `dominios_permitidos` (hoy `alumno.buap.mx`). Para otra universidad se inserta una fila. Sin políticas RLS: solo la leen las funciones.
- **Tenant de la BUAP**: la columna `azure_tenant_id` está vacía; en cuanto se conozca (viene en `identity_data ->> 'iss'` tras el primer login) hay que cargarla: `update dominios_permitidos set azure_tenant_id = '<tenant>' where dominio = 'alumno.buap.mx';`. Mientras esté vacía, basta con que el correo de Microsoft sea del dominio, y como el correo de Microsoft no está verificado por Microsoft, otra organización podría poner el mismo.
- **Portal simulado** (`PortalSimuladoActivity`, `reco://verificado`): es lo que se muestra con **datos simulados** (la demo). Valida el formato `^[a-z0-9]+(\.[a-z0-9]+)*@alumno\.buap\.mx$` (se compara en minúsculas) y, si no cumple, dice "Usa tu correo institucional, ej. nombre.apellido@alumno.buap.mx"; el error se quita al editar el correo.
- **Botones de Microsoft y de código** (solo con Supabase): se muestran únicamente cuando `VERIFICACION_REAL=true` en `local.properties` (hazlo cuando Azure esté activo). Con Supabase y sin esa bandera, Verificar muestra solo "La verificación con tu correo de Microsoft estará disponible pronto", para que nada falle en una presentación. Con Supabase no hay otra forma de verificar sin Microsoft ni código.

## Datos: simulados o Supabase (switch)

`data/Repositorios.kt` elige las implementaciones al iniciar la app:

- **Simulados** (JSON internos en `filesDir`): **es el modo por defecto** (la demo). No necesita red ni llaves.
- **Supabase**: cuenta y perfil, verificación, catálogo con fotos, favoritos, pedidos, reseñas y los anuncios del vendedor. Se activa con el switch de debug y requiere las llaves en `local.properties`.
- **Switch de debug**: en la Bienvenida, solo en builds debug, el texto "DEBUG · Datos: ..." alterna entre ambos y cierra la sesión. La elección se recuerda; una instalación limpia arranca en simulados.
- **Anuncios del vendedor** (Mis ventas, Publicar): con Supabase se leen y escriben en `productos` y `fotos`. Los **borradores** se quedan en el teléfono (JSON local, con rutas de archivo); al **enviar a revisión** se suben las fotos a Storage en `fotos/<vendedor_id>/<anuncio_id>/<n>.jpg` y se crea el anuncio (`AnuncioRepositorySupabase`). Corregir un rechazado actualiza el mismo anuncio y reemplaza sus fotos. Mis ventas muestra los estados En revisión, Publicado, Rechazado y Vendido. Con datos simulados todo sigue como antes (JSON local, botones de moderación en debug).
- **Publicar 2 · Fotos**: de 2 a 8 fotos desde la galería (Photo Picker) o la cámara; ◀ ▶ reordenan, ★ marca la portada (la primera) y cada foto se reduce a máx. 1280 px en JPG antes de subirla. La cámara usa un `FileProvider` y no pide permisos.
- **Preguntas rápidas** del Detalle: con Supabase usan las respuestas del vendedor (`donde_entregas`, `horario`, `acepta_tarjeta`, que se llenan al unirse como vendedor); sin esos datos, o con datos simulados, usan la plantilla.

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
- **Datos de prueba**: `python3 scripts/seed_dummyjson.py` (25 productos de DummyJSON, 91 fotos en el bucket público `fotos`, 4 vendedores ficticios de la BUAP con correos `@example.invalid` y bloqueados) y luego `supabase db push --include-seed --remote`. Se puede repetir sin duplicar. **Fotos propias**: pon carpetas en `scripts/fotos_extra/<categoria>/<Marca Modelo>/` (ver su README); el script las comprime, las sube y las agrega como anuncios publicados.
- **Ojo con la CLI**: `supabase migration new` se cuelga si se encadena con tuberías; córrelo solo, con `</dev/null`, y escribe el archivo aparte. `supabase db query` y `db push` se corren desde la carpeta del repo.
- **Precios** de prueba: USD × 18 × 0.65, redondeado a centenas.
- **Tablas**: universidades, dominios_permitidos, puntos_entrega, modelos, imei_bloqueados, perfiles, productos, fotos (con `es_portada`), favoritos, reportes, pedidos (un pedido = un equipo), calificaciones. No hay carrito. La vista `anuncio_fotos` (anuncio_id, url, orden, es_portada) es otro nombre para `fotos`, con las mismas reglas RLS.
- **Perfiles de vendedor**: `apodo`, `acerca_de` y las respuestas rápidas `donde_entregas`, `horario`, `acepta_tarjeta`. Los demás ven solo la vista `perfiles_publicos` (sin teléfono ni CLABE).
- **Funciones** (las llama la app con la sesión del usuario): `verificar_estudiante()`, `activar_vendedor(...)`, `crear_pedido(p_producto, p_punto, p_horario)`, `marcar_entregado(p_pedido)`, `confirmar_entrega(p_pedido)`, `obtener_mi_imei(p_producto)`. El monto y la comisión (13.6 % + $8) los calcula el servidor.
- **IMEI**: la columna `imei` solo la lee su dueño (con `obtener_mi_imei`); los demás ven `imei_ultimos4`. Por eso las consultas a `productos` deben listar columnas, no usar `select=*`.
- **Storage (bucket público `fotos`)**: las fotos se ven por URL pública, pero el bucket **no se puede listar**; solo el dueño de la carpeta `<uid>/` puede leer (lo que pide un upsert), subir, reemplazar y borrar.
- **Fotos de un anuncio**: la URL debe empezar con el origen de Storage de este proyecto (fijo en la migración `url_fotos_anclada`; hay que actualizarlo si el proyecto cambia) y la carpeta de su vendedor, sin `..`, `?`, `#` ni `%`; una foto no se puede mover a otro anuncio (la app las borra y las vuelve a insertar); y cambiar las fotos de un anuncio publicado, pausado o reservado lo regresa a `en_revision` (triggers `fotos_validar_url` y `fotos_revision`, que miran el rol del JWT con `es_cliente_jwt()`).
- **IMEI bloqueado**: además de la app, lo rechaza un trigger del servidor (`imei_bloqueados`).
- **Anónimo (anon key)**: puede leer solo anuncios publicados, sus fotos, modelos y puntos de entrega. Todo lo demás exige sesión.
- **Reseñas**: la vista `resenas_publicas` expone quién calificó, a quién y qué equipo.
- **Moderador**: sin pantallas. Aprueba o rechaza anuncios desde el panel de Supabase (`update productos set estado = 'publicado' where id = ...`).
- **Auth**: correo y contraseña (sin confirmación por correo, para la demo), Microsoft (Azure) y código OTP. `enable_manual_linking = true` y `reco://auth` están en `config.toml`. Google Sign-In no está hecho.
- **Correo OTP**: para que el correo lleve el **código** (y no solo el enlace) hay que cambiar la plantilla (`supabase/templates/codigo.html`), y el plan gratis con el SMTP por defecto no lo permite; hace falta SMTP propio. Ese SMTP también quita el límite de correos por hora y que solo llegue a miembros de la organización.
- **Secretos de Microsoft**: `AZURE_CLIENT_ID` y `AZURE_SECRET` van en `supabase/.env.local` (ignorado por git); `config.toml` los lee con `env(...)`.

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

1. **Borradores en Supabase**: hoy solo viven en el teléfono (con rutas de archivo) hasta enviar a revisión.
2. **Moderación**: sin pantallas; aprobar o rechazar se hace en el panel de Supabase. Al reenviar un rechazado, el `motivo_rechazo` se queda hasta que el moderador lo limpie.
3. Activar el proveedor Azure en Supabase con el client ID y el secret, probar con una cuenta `@alumno.buap.mx` y cargar el `azure_tenant_id`. Si la BUAP exige aprobación del administrador, el plan B (código) pasa a principal y necesita SMTP propio.
4. Google Sign-In real (Credential Manager), solo si sobra tiempo.
5. Puntos de entrega reales de CU.
6. Menores: "Mis datos" y "Puntos de entrega" de Cuenta, foto de perfil, reportar un problema en un pedido.

## Fuera de alcance

Chat en tiempo real, ofertas, pasarela de pago real, consulta real de IMEI, notificaciones, meses sin intereses y recompra al terminar la carrera.
