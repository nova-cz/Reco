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
| 3a | 23 y 24 nov | Primer avance de la programación | Por empezar |
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
- **Plan B**, si la universidad no permite apps externas: una página web de verificación propia y simulada, con el mismo ida y vuelta por deep link.
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
| Tablets | `#34C27A` |
| Audio | `#FF8DB3` |
| Gaming | `#FF7A1A` |
| Calculadoras | `#D29A5C` |

- Insignias: Estudiante verificado en azul, Certificado en verde con escudo, y el grado en un chip negro.
- Marca: verde Reco `#34C27A`. Logo del camaleón hecho por Miguel, con el copete azul, naranja y rosa. La mascota es un juguete 3D minimalista.

## Datos simulados

- Unos 30 productos de prueba en Supabase, con fotos en Storage.
- Universidad BUAP con dominio `alumno.buap.mx`. Puntos de entrega de ejemplo: Biblioteca Central de CU y Cafetería FCC.
- Tabla `imei_bloqueados` con algunos IMEI de prueba.
- Pagos con Stripe en modo de prueba. No hay dinero real.

## Fuera de alcance (requisitos futuros)

Chat en tiempo real, ofertas, pasarela de pago real, consulta real de IMEI, meses sin intereses y recompra al terminar la carrera.

## Pendientes por definir

1. **Proveedor de cuentas de la BUAP**: averiguar si es Microsoft o Google. Eso decide si va el login institucional real o el plan B simulado.
2. ~~Package name~~ **Resuelto**: el paquete y `applicationId` es `com.fcc.reco`, con minSdk 26 y targetSdk 37.
3. **Catálogo de modelos** con su precio de referencia, que se usa para validar el precio. Se necesitan unos 30 a 50 modelos.
4. **Lista de puntos de entrega y horarios** reales de CU.
5. **Costo de la certificación de pago**, por ejemplo $99 MXN.
6. **Stripe**: confirmar si se hace con PaymentSheet y Edge Function o con el plan B simulado.
7. **Fotos de los productos simulados**: fotos propias o de prueba.
8. Diseño de las 12 pantallas restantes.

## Orden sugerido para la 3a entrega (23 y 24 nov)

1. Proyecto base: Gradle, dependencias, tema, colores y tipografía Inter.
2. Cliente de Supabase, `schema.sql` y `seed.sql`.
3. Acceso: login con correo y Google, verificación (plan B si hace falta) y elegir modo.
4. Inicio y Resultados con datos reales de Supabase, más Detalle de producto.
5. Navegación de comprador y vendedor con las barras inferiores.

## Convenciones

- Interfaz y textos de la app en español de México.
- Nombres de clases y métodos en español, como en el ERS.
- Código completo y listo para pegar cuando se pida un archivo.
- Ningún secreto en el repositorio. Las llaves de Supabase y Stripe van en `local.properties` o en variables de la Edge Function.
