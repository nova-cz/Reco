# Reco · Contexto del proyecto

App Android de compraventa de electrónica usada **entre estudiantes universitarios verificados**, con pago retenido, revisión de calidad y entrega en el campus. Proyecto final de Desarrollo de Aplicaciones Móviles, BUAP, Otoño 2026. Profesor: Abraham Sánchez López. Autor: Miguel Angel Cruz Reyes.

Este archivo resume todas las decisiones tomadas hasta el 6 de noviembre de 2026. La fuente completa es el documento ERS (IEEE 830).

## Enlaces

- Documento ERS IEEE 830: https://claude.ai/artifact/K8hGNr9fTTz7ATs7H8Una5
- Figma (pantallas, marca y mapas mentales): https://www.figma.com/design/3dFv6cBGuMqbBVZS0wD9YL
  - Página `ReCampus`: 14 pantallas diseñadas, en filas Acceso, Comprador y Vendedor
  - Página `Mapas mentales`: mapa general y mapa de navegación
  - Página `Marca`: logo, íconos de app y mascota

## Entregas

| Entrega | Fechas | Contenido | Estado |
| --- | --- | --- | --- |
| 1a | 11 y 13 nov | Auditoría de apps similares | Lista, Apéndice A del ERS |
| 2a | 18 y 20 nov | Mapas mentales, bocetos a mano y en Balsamiq | Mapas listos en Figma; bocetos los hace Miguel |
| 3a | 23 y 24 nov | Primer avance de la programación | En proceso: acceso, explorar y comprar funcionando con datos simulados |
| Final | Calendario DAE | ERS completa y app | En proceso |

## Concepto

- Nombre: **Reco**. Mascota: camaleón con lentes y copete de tres colores (azul, naranja, rosa).
- Diferenciador frente a Mercado Libre, Facebook Marketplace, Appetite, UniMarkt, rumie y otras: ninguna junta las cinco cosas a la vez.
  1. Solo estudiantes verificados.
  2. Pago retenido dentro de la app.
  3. Revisión de calidad del equipo.
  4. Entrega en un punto del campus.
  5. Solo electrónica.
- Categorías: Laptops, Celulares, Tablets, Audio, Gaming, Calculadoras y accesorios de cómputo.

## Usuarios y modos

- **Una sola cuenta por estudiante, con dos modos**: comprador y vendedor.
- La primera vez, la app pregunta "Quiero comprar" o "Quiero vender" y abre ese modo.
- El vendedor puede comprar desde el botón Explorar o con "Cambiar a modo comprador".
- El comprador activa el modo vendedor desde Perfil, en "¿Quieres vender algo?". Para eso se le pide teléfono, punto de entrega, horarios y CLABE simulada, y acepta las reglas.
- Nadie puede comprar sus propios anuncios.
- La app recuerda el último modo usado.
- **Moderador**: no tiene pantallas. Se simula desde el panel de Supabase: aprueba o rechaza anuncios y revisa reportes.

## Verificación de estudiante

- Se hace con el login web de la cuenta institucional, en una Chrome Custom Tab, y la app regresa por deep link (por ejemplo `reco://verificado`, declarado con un intent-filter en el AndroidManifest).
- Supabase hace la parte del login con `linkIdentity` y el proveedor de la universidad, Microsoft o Google.
- La app valida el dominio del correo contra la tabla `universidades`. El primer dominio es `alumno.buap.mx`.
- La app nunca ve la contraseña institucional.
- **Plan B**, si la universidad no permite apps externas: una página web de verificación propia y simulada, con el mismo ida y vuelta por deep link. *(Estado actual: el portal simulado es una Activity dentro de la app, `PortalSimuladoActivity`, que regresa por `reco://verificado?correo=...`; se cambia por Chrome Custom Tab al conectar el login real.)*
- Al verificarse, el perfil queda ligado a su universidad y gana la insignia "Estudiante verificado". Sin verificación no se puede comprar ni vender.

## Flujos

**Primer ingreso**
1. Bienvenida.
2. Crear cuenta o iniciar sesión, con correo o con Google.
3. Verificar estudiante.
4. Elegir carrera.
5. ¿Qué quieres hacer?
6. Abre Inicio (comprador) o Registro de vendedor y luego el Panel.

**Comprar**
1. Inicio, Resultados o Detalle.
2. Agregar al carrito.
3. Entrega y pago: punto y horario por cada vendedor.
4. Pago con Stripe en modo de prueba. Se crea un pedido por vendedor y el dinero queda retenido.
5. El día de la entrega, el comprador revisa el equipo y confirma la entrega.
6. Se libera el pago al vendedor y el comprador lo califica.

Cada equipo es pieza única: no hay cantidades y sale del catálogo cuando se paga.

**Publicar**
1. Panel, botón "+".
2. Cinco pasos: categoría y modelo, detalles, fotos, precio, resumen.
3. El anuncio queda En revisión.
4. El moderador lo aprueba o lo rechaza con motivo.
5. Si se aprueba, aparece en el catálogo y en "Mis productos en venta".

**Vender y cobrar**
1. Notificación de venta.
2. Detalle de venta.
3. Entrega en el campus.
4. Marcar entregado.
5. El comprador confirma y la ganancia aparece sin la comisión.

Estados del anuncio: Borrador, En revisión, Publicado, Rechazado con motivo, Reservado, Vendido, Pausado.

## Pantallas (26)

Diseñadas en Figma (14): Bienvenida, Iniciar sesión, Verificar estudiante, ¿Qué quieres hacer?, Inicio, Resultados, Detalle de producto, Carrito, Entrega y pago, Perfil, Registro de vendedor, Panel del vendedor, Publicar 1 y Publicar 2.

Faltan por diseñar (12): Crear cuenta, Compra confirmada, Mis compras, Detalle de pedido, Favoritos, Notificaciones, Perfil público del vendedor, Publicar 3, 4 y 5, Mis anuncios y Detalle de venta.

Barra inferior flotante con efecto glass:
- Comprador: Inicio, Favoritos, Carrito, Compras, Perfil.
- Vendedor: Panel, Anuncios, botón naranja "+" al centro, Ventas, Perfil.

## Estado actual del proyecto (código)

Última actualización: cierre de la sesión de trabajo del 6 de noviembre de 2026. Repo: `github.com/nova-cz/Reco` (rama `main`). Paquete `com.fcc.reco`, minSdk 26, targetSdk 37, AGP 9 con Kotlin integrado.

### Hecho y probado en el emulador

| Fase / flujo | Qué incluye |
| --- | --- |
| Fase 1 · Base | Tema siempre claro, colores del Figma, fuente Inter (4 pesos), íconos Lucide como vector drawables, fondo con manchas, ícono adaptativo y pantalla de arranque con el camaleón. Componentes: `GlassView` (blur real con RenderEffect en API 31+), `BarraInferiorFlotante`, `BotonPildora`, `ChipInsignia`, `ChipFiltro`, `CampoTexto`, `BarraPasos`, `AvatarIniciales`, `MosaicoCategoria`. Galería de componentes solo en debug ("Reco · Galería"). |
| Fase 2 · Acceso | Bienvenida → Iniciar sesión / Crear cuenta (misma pantalla con `modoRegistro`) → Verificar estudiante → hoja para elegir carrera → ¿Qué quieres hacer? Verificación con portal institucional **simulado dentro de la app** (`PortalSimuladoActivity`) que regresa por el deep link `reco://verificado?correo=...` y valida el dominio `alumno.buap.mx`. Recuerda sesión y último modo. |
| Fase 3 · Explorar | `CompradorActivity` con `nav_comprador` y barra inferior que navega (con contador del carrito). Inicio, Resultados (chips de filtro, búsqueda, orden) y Detalle (galería simulada, vendedor, especificaciones, IMEI con últimos 4 dígitos, barra de precio glass). 30 productos simulados. `VendedorActivity` como cascarón con su barra y el botón "+". |
| Flujo 3 · Comprar | Carrito (agrupado por vendedor) → Entrega y pago (punto y horario por vendedor + hoja de tarjeta de prueba validada con Luhn, vencimiento y CVC) → Compra confirmada. Un pedido por vendedor con el pago retenido, comisión 13.6 % + $8. Los equipos comprados salen del catálogo. |

Persistencia: carrito, favoritos y pedidos se guardan en JSON internos (`AlmacenJson`, en `filesDir`). La sesión va en SharedPreferences (`SesionLocal`). Pruebas unitarias en `src/test`: `ValidadorAcceso`, `Comision`, `Luhn` y `ValidadorPago`.

### Estado al 8 de noviembre de 2026 (sesión de trabajo)

Hecho, probado en el emulador y commiteado, flujo por flujo:

| Flujo | Qué incluye |
| --- | --- |
| Favoritos | Pantalla con filtros Todos / Disponibles / Vendidos; los vendidos salen atenuados. |
| Seguimiento | Mis compras (En curso / Completadas) → Detalle de pedido con línea de tiempo → Confirmar entrega (libera el pago) → hoja Calificar vendedor. `CalificacionRepository`. "Reportar un problema" solo muestra un aviso. |
| Registro de vendedor | Teléfono, punto, días y franjas, CLABE simulada (18 dígitos), reglas. Datos en `Sesion.vendedor`. Se llega desde Perfil o desde "Quiero vender". |
| Publicar | Asistente de 5 pasos con `AnuncioBuilder`, `ValidadorPublicacion` (IMEI Luhn o serie, bloqueados, duplicado, 2 a 8 fotos, rango 30–100 %, máximo 3 activos), `CatalogoModelos` (18 modelos), fotos reales con el selector del sistema, Borrador y envío a revisión. |
| Panel, Anuncios, Ventas | Panel con ganancias del mes, Mis anuncios con estados y acciones (Pausar, Reactivar, Corregir, Continuar, eliminar con mantener presionado), Ventas con "Marcar como entregado" y ganancia neta. Lo aprobado aparece en el catálogo del comprador. |
| Perfil | Un solo `PerfilFragment` que cambia si ya es vendedor, y Perfil público (A la venta / Reseñas, insignia "Entrega puntual" con ≥70 %). Se abre desde Detalle de producto, Detalle de pedido y "Ver mi perfil público". |

Decisiones de esta sesión:
- **Serie vs. IMEI**: celulares y tablets llevan IMEI de 15 dígitos con Luhn; el resto, número de serie (6 a 20 letras o números). Lista de bloqueados de prueba: `356918102345611`, `867501049283749`, `BLOQUEADO001`. IMEI válidos para probar: `490154203237518`, `353321087654326`.
- **Un solo dispositivo, una cuenta**: los anuncios propios sí salen en el catálogo, pero el botón de compra dice "Este anuncio es tuyo". Las compras de otros estudiantes sobre tus anuncios se simulan.
- **Botones solo en debug** (`FLAG_DEBUGGABLE`): en Mis anuncios, Aprobar / Rechazar (moderación) y Simular compra; en Ventas, "Simular: el comprador confirmó" (que además deja una reseña de 5 estrellas); en Detalle de pedido, "Simular: el vendedor marcó la entrega"; en Publicar 3, "Agregar foto de prueba".
- **Pedidos**: `Pedido.comprador` vacío = compra de este usuario; con nombre = venta de uno de sus anuncios (Mis compras filtra las primeras, Ventas las segundas).
- **Estado del pedido en la vista del vendedor**: Pago retenido → Por confirmar (el vendedor marcó entregado) → Completada (el comprador confirmó).
- La barra de progreso de Publicar es negra (componente existente), no naranja como en el Figma.

### Pendiente

1. **Supabase (obligatorio para la entrega final)**: `supabase/schema.sql` y `supabase/seed.sql` ya están escritos (sin probar en un proyecto real todavía). La app no tiene INSERT ni UPDATE directo sobre `pedidos`: los pedidos los crea la Edge Function `crear-pago` con `crear_pedido` (service_role) tras verificar el pago, y cambian de estado con `marcar_entregado` y `confirmar_entrega`; `verificar_estudiante` y `activar_vendedor` son funciones (la verificación exige que el token venga del tenant de Microsoft o del dominio `hd` de Google de la universidad: hay que cargar `azure_tenant_id` o `google_hd` en `universidades` cuando se sepa el proveedor de la BUAP), y los perfiles de otros se leen por la vista `perfiles_publicos` (sin teléfono ni CLABE). Falta crear el proyecto, correr los dos archivos, poner `SUPABASE_URL` y `SUPABASE_ANON_KEY` en `local.properties`, agregar supabase-kt y cambiar las implementaciones `...Simulado` por las de Supabase en `data/Repositorios.kt` (Auth con correo, productos con fotos en Storage, pedidos, calificaciones, favoritos).
2. Google Sign-In real (Credential Manager) solo si sobra tiempo tras Supabase.
3. Pendientes por definir de abajo (proveedor de cuentas BUAP, puntos reales de CU).
4. Menores: foto de perfil en el registro de vendedor, palomita verde en la CLABE, "Mis datos" y "Puntos de entrega" del Perfil muestran "Próximamente".

### Decisiones tomadas durante la implementación

- **Colores**: se respeta el Figma tal cual. Tablets es casi negro `#0F0F12` (no verde). El verde solo va en el logo, el chip "Estudiante verificado", la insignia "Certificado" y detalles que el Figma pinta de verde (escudo de "Pago protegido", "Gratis en tu campus"); nunca en botones, barras de progreso ni pestañas activas.
- **Glass**: translúcido con borde de luz como base; con API 31+ y `vincularFondo(vista)` desenfoca esa vista. Regla: el fondo debe ser **hermano** de la barra (no ancestro) y llevar él mismo `bg_fondo_app`; si no, el texto de atrás se ve nítido.
- **Cuadrículas**: no usar un `RecyclerView` anidado en un `ScrollView` (solo mostraba la primera fila). Se usa `CuadriculaProductos`, que arma filas en un `LinearLayout`.
- **Navegación**: los argumentos de texto con valor por defecto en los grafos van literales (Navigation no acepta `@string`); los argumentos viajan en `Bundle` con las claves `categoria`, `marca`, `consulta`, `productoId`, `pedidoIds`, `modoRegistro`, `titulo`. El helper `NavController.irA(...)` aplica las animaciones. La barra inferior del comprador se oculta en Detalle, Carrito, Entrega y pago y Compra confirmada, que traen su propia barra.
- **Verificación**: el Plan B simulado es una Activity propia, no una página web, porque no hay dónde alojarla. Con el login real pasa a Chrome Custom Tab.
- **Modelo de pedidos**: un pedido por vendedor con varios equipos (`productoIds`), como muestra el Figma del Carrito.
- **Datos por usuario**: carrito, favoritos y pedidos son por dispositivo y "Cerrar sesión" no los borra; con Supabase serán por usuario.
- **Botón de filtros** de Inicio y Resultados: solo limpia los filtros (la hoja avanzada está fuera de alcance).
- **Crear cuenta** reutiliza la pantalla de Iniciar sesión.

### Cómo trabajar en este repo

- Compilar y probar: `./gradlew assembleDebug testDebugUnitTest`. Correr siempre tras cada fase y corregir antes de seguir.
- Commits en español, a nombre de Miguel Angel Cruz Reyes con la configuración de git existente, **sin** líneas `Co-Authored-By`, "Generated with Claude" ni atribuciones. **No** hacer `git push` salvo que Miguel lo pida. La carpeta `.idea/` no se sube.
- Al cerrar cada flujo: probar en el emulador (AVD `Pixel_10a`), hacer commit, detenerse y decir qué probar.
- Antes de construir cada pantalla, consultar su nodo en Figma (archivo `3dFv6cBGuMqbBVZS0wD9YL`). Nodos: Bienvenida `1:16`, Iniciar sesión `1:101`, Verificar `1:152`, ¿Qué quieres hacer? `1:219`, Inicio `2:47`, Resultados `2:243`, Detalle `6:68`, Carrito `6:194`, Entrega y pago `6:319`, Perfil `7:101`, Registro de vendedor `7:219`, Panel `7:307`, Publicar 1 `8:134`, Publicar 2 `8:257`, Marca `16:240`. Las pantallas sin diseño se hacen con el mismo estilo.
- Los íconos son Lucide convertidos a vector drawable; los mosaicos y botones usan los degradados muestreados del Figma.

## Alcance final y orden de trabajo (acordado)

Unas 21 pantallas en 8 flujos. Cada flujo se construye completo, se prueba en el emulador y se commitea antes del siguiente.

1. **Acceso y verificación** (hecho): Bienvenida → Iniciar sesión → Verificar estudiante → ¿Qué quieres hacer?
2. **Explorar**: Inicio → Resultados → Detalle (hechos) + Favoritos.
3. **Comprar**: Carrito (agrupado por vendedor) → Entrega y pago (punto y horario por vendedor + pantalla de tarjeta simulada, sin Stripe) → Compra confirmada (dinero retenido).
4. **Seguimiento**: Mis compras → Detalle de pedido → Confirmar entrega → Calificar vendedor.
5. **Volverse vendedor**: Perfil "¿Quieres vender algo?" → Registro de vendedor.
6. **Publicar**: 5 pasos con validación (IMEI con Luhn, fotos, rango de precio) → En revisión → moderación con botón de debug (aprobar/rechazar) → Publicado.
7. **Gestionar ventas**: Panel → Mis anuncios (estados) → Ventas con detalle integrado, "Marcar entregado" y ganancia neta (13.6% + $8).
8. **Cuenta**: Perfil completo, cambiar de modo y cerrar sesión.

**Fuera de alcance** (recortado): Notificaciones, hoja de filtros avanzada, Detalle de venta como pantalla aparte, Crear cuenta como pantalla aparte (reutiliza Iniciar sesión) y `abrirReclamo` (en Detalle de pedido solo hay un botón "Reportar problema" que muestra un aviso). **Perfil público del vendedor sí entra** (22 pantallas en total).

## Decisiones de alcance definitivas (8 de noviembre de 2026)

Estas decisiones mandan sobre cualquier línea anterior que las contradiga.

- **Catálogo**: 15 a 20 modelos (2 a 3 por categoría) con precio de referencia.
- **Fotos al publicar**: mínimo 2, máximo 8. **Máximo 3 anuncios activos** por vendedor.
- **App bloqueada en vertical** (`screenOrientation="portrait"`); no se maneja rotación.
- **Verificación**: portal simulado (Custom Tab + `reco://verificado`). Google Sign-In real solo si sobra tiempo tras Supabase.
- **Certificación**: $99 MXN. **Comisión 13.6 % + $8**, calculada en un solo lugar (`domain/Comision.kt`).
- **Puntos de entrega**: Biblioteca Central, FCC, Explanada de Ingeniería y Comedor universitario. Horario 10:00 a 18:00.
- **Datos simulados persistentes**: JSON interno (`AlmacenJson`), sin Room.
- **Supabase** es obligatorio para la entrega final y va al último.
- **Perfil**: un solo Fragment que cambia si el usuario ya es vendedor. **Perfil público del vendedor** se abre desde Detalle de producto, Detalle de pedido y "Ver mi perfil público"; pestañas "A la venta" y "Reseñas"; insignia "Entrega puntual" si ≥70 % de las reseñas la tienen; sin datos de contacto.
- **Colores de estado** (texto / fondo): verde `#1E9E5A` / `#E8F7EF` (Publicado, Entregado); naranja `#E0650C` / `#FFF1E6` (En revisión, Listo para entregar); azul `#2F7BF6` / `#EEF4FF` (Pago retenido, Vendido); rojo `#E5484D` / `#FDECEC` (Rechazado); gris `#6E6E76` / `#EDEDF1` (Pausado).
- **Nodos Figma nuevos**: Favoritos `33:156`, Mis compras `33:445`, Detalle de pedido `33:713`, Calificar vendedor (bottom sheet) `33:914`, Perfil sin vender `7:101`, Perfil con vendedor activo `38:265`, Perfil público `38:456`, Publicar 3 `34:200`, Publicar 4 `34:387`, Publicar 5 `34:558`, Mis anuncios `35:233`, Ventas `35:439`. Los textos de los diseños son de ejemplo; se usan los datos simulados.
- **Orden**: 1 Favoritos, 2 Seguimiento, 3 Registro de vendedor, 4 Publicar + `ValidadorPublicacion` + moderación debug, 5 Panel / Mis anuncios / Ventas, 6 Perfil + Perfil público + estados vacíos y errores, 7 Supabase.

**Orden**: Comprar completo → Vender completo → Perfil y conexión → Supabase.

**Supabase es obligatorio para la entrega final** (Auth con correo; tablas de perfiles, productos, pedidos, calificaciones y favoritos; fotos en Storage; Google Sign-In real si alcanza). Va como última fase: como los repositorios son interfaces, solo se cambia la implementación.

**Datos simulados mientras tanto**: persisten al cerrar la app en archivos JSON internos (`filesDir`, sin Room ni DataStore), para que lo publicado y comprado no se pierda en la demo. Todo está conectado: lo publicado y aprobado aparece en el catálogo del comprador, y cada compra aparece en Ventas del vendedor con su estado.

**Decisión de modelo**: un pedido por vendedor puede incluir varios equipos (`productoIds`), como muestra el Figma del Carrito. En Supabase será `pedidos` + `pedido_items`.

## Reglas de publicación

1. Solo categorías de electrónica, y el equipo debe encender y funcionar.
2. Prohibido: bloqueo de iCloud o de cuenta Google, IMEI reportado, réplicas, baterías infladas y equipos para piezas.
3. IMEI de 15 dígitos que pase el algoritmo de Luhn, que no esté en `imei_bloqueados` y que no esté en otro anuncio activo. Al comprador solo se le muestran los últimos 4 dígitos.
4. De 4 a 8 fotos reales: frente encendido, reverso, costados y la pantalla de Ajustes con el modelo y la salud de batería.
5. Precio entre 30% y 100% del precio de referencia del modelo. Fuera de ese rango, va a revisión manual.
6. Grados: A Excelente, sin marcas; B Muy bueno, marcas leves; C Bueno, rayones visibles.
7. Un vendedor nuevo puede tener máximo 3 anuncios activos hasta completar 3 ventas con promedio de 4 estrellas o más.
8. Con 3 reportes, el anuncio se oculta y vuelve a revisión. Con 3 rechazos por fraude, la cuenta se suspende.

## Monetización

- Comisión igual a la de eBay para celulares y electrónica: **13.6% más $8 MXN por pedido**. Ejemplo: en una venta de $5,000 la comisión es $688 y el vendedor recibe $4,312.
- Certificación de pago, opcional: da la insignia "Certificado", 3 meses de garantía y prioridad en las búsquedas. Se simula: el moderador marca el anuncio.

## Stack y arquitectura

- Android nativo con **Kotlin** y **XML con Fragments**, como en las prácticas del curso.
- **MVVM con repositorios**:
  - Vista: Activities y Fragments.
  - ViewModel: StateFlow y corrutinas.
  - Repository: único punto de acceso a Supabase.
- Navigation Component. Hay una Activity de acceso que decide el modo, más `CompradorActivity` y `VendedorActivity`, cada una con su grafo de navegación y su barra inferior.
- Patrones: MVVM, Repository, Singleton (un solo cliente de Supabase), Observer (StateFlow), Adapter (RecyclerView) y Builder (asistente de publicar).
- Librerías:
  - supabase-kt (Auth, Postgrest, Storage) y Ktor.
  - Corrutinas, ViewModel, Navigation, RecyclerView.
  - Coil para imágenes.
  - Credential Manager para Google.
  - Chrome Custom Tabs.
  - Stripe Android SDK (PaymentSheet).
- Login con Google: Credential Manager entrega un ID token, que va a `supabase.auth.signInWith(IDToken)` con el proveedor Google.
- Pago: Stripe PaymentSheet en modo de prueba, con la tarjeta 4242 4242 4242 4242. Una Supabase Edge Function crea el PaymentIntent con la llave secreta de prueba; la llave nunca va en la app. Plan B: una pantalla de tarjeta propia que solo simula el pago.

Estructura sugerida:

```
app/src/main/java/.../reco/
  data/
    model/          # data classes: Perfil, Producto, Pedido...
    remote/         # SupabaseClient (singleton)
    repository/     # AuthRepository, PerfilRepository, VerificacionRepository, ProductoRepository,
                    # PagoRepository, PedidoRepository, CalificacionRepository, FavoritoRepository,
                    # ReporteRepository, CarritoRepository
  domain/
    ValidadorPublicacion.kt
  ui/
    acceso/         # Bienvenida, Login, Registro, Verificacion, ElegirModo
    comprador/      # Inicio, Resultados, Detalle, Carrito, EntregaPago, Compras, Favoritos, Perfil
    vendedor/       # RegistroVendedor, Panel, Publicar (5 pasos), Anuncios, Ventas
    comun/          # adapters, componentes reutilizables
supabase/
  schema.sql        # tablas y RLS
  seed.sql          # universidades, puntos, modelos, productos simulados
  functions/crear-pago/   # Edge Function de Stripe
```

## Modelo de datos (Supabase)

| Tabla | Campos principales |
| --- | --- |
| universidades | id, nombre, dominio |
| puntos_entrega | id, universidad_id, nombre, descripcion, horario |
| perfiles | id = auth.users.id, nombre, carrera, semestre, universidad_id, correo_institucional, verificado, es_vendedor, ultimo_modo, telefono, punto_preferido_id, disponibilidad, clabe_simulada, reputacion, ventas_completadas, rechazos, suspendido |
| modelos | id, categoria, marca, modelo, precio_referencia |
| productos | id, vendedor_id, modelo_id, almacenamiento, ram, color, grado, salud_bateria, imei, accesorios, descripcion, precio, carreras, estado, certificado, motivo_rechazo, creado |
| fotos | id, producto_id, url, orden |
| carrito | usuario_id, producto_id, agregado |
| pedidos | id, producto_id, comprador_id, vendedor_id, monto, comision, stripe_payment_id, punto_id, horario, estado |
| calificaciones | id, pedido_id, estrellas, comentario |
| favoritos | usuario_id, producto_id |
| reportes | id, producto_id, usuario_id, motivo |
| imei_bloqueados | imei |

La tabla `carrito` no está en el ERS; se agrega aquí. Estados del pedido: pagado_retenido, entregado, liberado, en_reclamo, cancelado.

RLS:
- El comprador solo lee productos publicados y sus propios pedidos.
- El vendedor solo edita sus productos.
- Solo el moderador cambia un anuncio a Publicado o Rechazado.
- Nadie puede crear un pedido sobre un producto propio.

## Métodos principales

| Clase | Métodos |
| --- | --- |
| AuthRepository | registrarConCorreo, iniciarConCorreo, iniciarConGoogle, cerrarSesion |
| PerfilRepository | obtenerPerfil, activarVendedor, cambiarModo, obtenerUltimoModo |
| VerificacionRepository | abrirLoginInstitucional, procesarDeepLink, dominioValido, ligarUniversidad |
| ProductoRepository | obtenerPublicados (con filtros y universidad), obtenerPorId, obtenerMisAnuncios, guardarBorrador, subirFotos, enviarARevision, actualizar, pausar, eliminar |
| ValidadorPublicacion | validarImei (Luhn), imeiBloqueado, imeiDuplicado, validarFotos, validarPrecio |
| CarritoRepository | agregar, quitar, listarAgrupadoPorVendedor |
| PagoRepository | crearIntentoDePago, procesarResultado, calcularComision |
| PedidoRepository | crearPedido, esAnuncioPropio, elegirPuntoEntrega, marcarEntregado, confirmarEntrega, abrirReclamo |
| CalificacionRepository | calificarVendedor, obtenerReputacion |
| FavoritoRepository | agregar, quitar, listar |
| ReporteRepository | reportarProducto |

## Guía visual

- Estilo tipo iOS: fondo gris claro `#F2F2F5` con manchas de color difuminadas, y tarjetas blancas con radio 24 y sombra suave.
- Chips y botones en forma de píldora. El botón principal es **negro** `#111114`, con un degradado sutil y texto blanco. Probamos el verde y se descartó.
- Efecto glass: blanco al 55-72%, desenfoque de 24-36 y borde de luz. Se usa en la barra inferior, la barra de precio del detalle y los botones sobre fotos.
- Texto principal `#111114` y secundario `#6E6E76`.
- Tipografía Inter: títulos 28 semibold o bold, secciones 20 semibold, cuerpo 15, etiquetas 12 medium.
- Íconos de línea de 1.5 a 2 px, estilo Lucide o SF Symbols.
- Colores por categoría, con degradado diagonal en los mosaicos:

| Categoría | Color |
| --- | --- |
| Laptops | `#1C1C1E` |
| Celulares | `#2F7BF6` |
| Tablets | `#0F0F12` (casi negro, según el Figma) |
| Audio | `#FF8DB3` |
| Gaming | `#FF7A1A` |
| Calculadoras | `#D29A5C` |

- Insignias: Estudiante verificado en azul, Certificado en verde con escudo, y el grado en un chip negro.
- Marca: verde Reco `#34C27A`. Logo del camaleón hecho por Miguel, con el copete azul, naranja y rosa. La mascota es un juguete 3D minimalista.

## Datos simulados

- **Hoy** (antes de Supabase): 30 productos en memoria en `DatosSimulados` con un ícono de categoría sobre gris como foto; 5 vendedores de prueba; universidad BUAP con dominio `alumno.buap.mx`; puntos de entrega Biblioteca Central (Ciudad Universitaria, planta baja) y Cafetería FCC (Edificio CCO1); horarios Lun 11:00, Mar 13:00, Mié 16:00 y Jue 12:00. Pagos simulados (tarjeta de prueba 4242 4242 4242 4242, sin dinero real).
- **Con Supabase**: unos 30 productos de prueba en la base, con fotos en Storage; tabla `imei_bloqueados` con algunos IMEI de prueba; Stripe en modo de prueba solo si se decide usarlo.

## Fuera de alcance (requisitos futuros)

Chat en tiempo real, ofertas, pasarela de pago real, consulta real de IMEI, meses sin intereses y recompra al terminar la carrera.

## Pendientes por definir

1. **Proveedor de cuentas de la BUAP**: averiguar si es Microsoft o Google. Eso decide si va el login institucional real o se queda el plan B simulado.
2. ~~Package name~~ **Resuelto**: `com.fcc.reco`, minSdk 26, targetSdk 37.
3. **Catálogo de modelos** con su precio de referencia (30 a 50 modelos), que se usa para validar el precio al publicar.
4. **Puntos de entrega y horarios reales** de CU (hoy son los dos de ejemplo).
5. **Costo de la certificación de pago**, por ejemplo $99 MXN.
6. ~~Stripe~~ **Resuelto por ahora**: pago simulado con pantalla de tarjeta propia (plan B). Stripe PaymentSheet con Edge Function solo si sobra tiempo.
7. **Fotos de los productos simulados**: fotos propias o de prueba para Storage.
8. **Diseño de las pantallas sin Figma** que sí están en el alcance: Compra confirmada y Favoritos (ya construida o por construir con el mismo estilo), Mis compras, Detalle de pedido, Publicar 3 a 5, Mis anuncios y Ventas. Quedaron fuera de alcance: Notificaciones, Perfil público del vendedor, hoja de filtros avanzada, Detalle de venta aparte y Crear cuenta aparte.

## Avance respecto a la 3a entrega (23 y 24 nov)

1. ~~Proyecto base: Gradle, dependencias, tema, colores y tipografía Inter~~ (hecho).
2. Cliente de Supabase, `schema.sql` y `seed.sql`: **pendiente** (va al final; hoy los repositorios son simulados).
3. ~~Acceso: login con correo y Google, verificación (plan B) y elegir modo~~ (hecho, simulado).
4. ~~Inicio, Resultados y Detalle de producto~~ (hecho con datos simulados; falta conectarlos a Supabase).
5. ~~Navegación de comprador y vendedor con las barras inferiores~~ (hecho; el vendedor es un cascarón).
6. Extra ya hecho: flujo de compra completo (Carrito, Entrega y pago, Compra confirmada).

## Convenciones

- Interfaz y textos de la app en español de México.
- Nombres de clases y métodos en español, como en el ERS.
- Código completo y listo para pegar cuando se pida un archivo.
- Ningún secreto en el repositorio. Las llaves de Supabase y Stripe van en `local.properties` o en variables de la Edge Function.
- Los textos de la interfaz van en `strings.xml` (excepto los valores por defecto de argumentos de navegación).
- Un ViewModel por pantalla o flujo, con `StateFlow` y eventos de una sola vez por `Channel`; los fragments recolectan con `repeatOnLifecycle(STARTED)`.
- Los repositorios son interfaces con implementación `...Simulado`; se obtienen desde `Repositorios` (inicializado en `RecoApp`).
