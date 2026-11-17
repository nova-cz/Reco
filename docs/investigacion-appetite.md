# Investigación: Appetite (marketplace universitario, México)

Fecha de la investigación: 17 de noviembre de 2026. Versión revisada de Appetite: 2.1.0 (8 de noviembre de 2026).
Reco comparado: rama `main`, commit `c9c81e6` (8 de noviembre de 2026) y `CLAUDE.md`.

**Convención:** todo dato lleva su fuente. "No confirmado" significa que no apareció en ninguna fuente que pude abrir. Las cifras que publica la propia empresa se marcan como "según Appetite".

## Fuentes usadas

| Clave | Fuente | Qué se pudo leer |
| --- | --- | --- |
| [AS] | Ficha App Store: https://apps.apple.com/mx/app/appetite-campus-marketplace/id6741954481 (datos vía https://itunes.apple.com/lookup?id=6741954481&country=mx) | Descripción, novedades de la 2.1.0, calificación, categoría, desarrollador |
| [GP] | Ficha Google Play: https://play.google.com/store/apps/details?id=com.heyappetite.appetite | Descripción parcial (Inicio, Búsqueda, Selección de campus, Publicar producto), calificación, descargas. La sección de novedades solo dice "Rediseño completo de la app" |
| [WEB] | Sitio: https://hey-appetite.com/ (secciones Cómo vender, Reglas, FAQ y Equipo) | Texto completo de la página |
| [TOS] | Términos de servicio: https://hey-appetite.com/tos | Texto leído (sin fecha de vigencia) |
| [PRIV] | Aviso de privacidad: https://hey-appetite.com/privacy | Texto leído (última actualización 2 de febrero de 2025) |
| [B201] | https://hey-appetite.com/blog/appetite-2-0-1 | Notas de la 2.0.1 (29 de agosto de 2026) |
| [B202] | https://hey-appetite.com/blog/appetite-2-0-2 | Notas de la 2.0.2 (9 de septiembre de 2026) |
| [B210] | https://hey-appetite.com/blog/appetite-2-1-0 | Notas de la 2.1.0 (8 de noviembre de 2026) |
| [ACA] | https://hey-appetite.com/academy | Solo temario (formalizar, alimentos, crecer); sin reglas de venta ni pagos |
| [IG] | https://www.instagram.com/hey_appetite | Bio y publicaciones recientes; sin información de pagos ni verificación |
| [RSS] | Reseñas App Store México: https://itunes.apple.com/mx/rss/customerreviews/id=6741954481/sortby=mostrecent/json | 6 reseñas |

**No se pudo abrir o no hay contenido útil:**
- TikTok (@hey.appetite.mx) y Facebook (HeyAppetiteMx): la página no cargó o no se revisó. Solo conozco sus enlaces desde [WEB].
- LinkedIn de los fundadores: solo se vieron los enlaces en [WEB]. No se leyeron los perfiles.
- Prensa y entrevistas: tres búsquedas no encontraron notas de prensa ni entrevistas sobre Appetite México. Aparece una startup británica con el mismo nombre (maddyness.com), sin relación con esta. **No confirmado** que existan.
- **Capturas `docs/capturas/appetite/`:** esa carpeta **no existe** en el repo `nova-cz/Reco` (rama `main`, ni en ninguna otra rama remota) y tampoco la encontré en esta máquina. Por eso no puedo describir las capturas pantalla por pantalla. Las pantallas se reconstruyen solo desde el blog y las fichas, y todo lo que dependa de ver la app queda como **no confirmado**. Si me pasas las capturas, completo la sección.

---

## Resumen

1. Appetite es un marketplace gratuito para estudiantes de México, organizado por campus. Lo desarrolla E-MEXICO COMERCIO INTELIGENTE SAPI DE CV ([AS], [PRIV]). Tiene 4.59 de 56 calificaciones y más de 5 mil descargas en Google Play ([GP]), y 4.54 de 24 en App Store México ([AS]).
2. **No procesa pagos.** Comprador y vendedor acuerdan precio, forma de pago y entrega por el chat. Tampoco cobra comisión ni suscripción "por el momento" ([WEB] FAQ, [TOS]). No encontré modelo de ingresos publicado: **no confirmado**.
3. La protección es social, no financiera: reseñas con estrellas, reportes, moderación con IA y strikes. Los términos descargan toda responsabilidad por disputas, productos y entregas ([TOS] §6, §12).
4. Vender exige verificar al estudiante: apodo, documento académico y foto sosteniéndolo. Revisión humana en unas 24 horas hábiles, y los documentos se borran al validar ([WEB]).
5. Su diferenciador es la inmediatez: "Para ¡YA!" (productos con entrega inmediata en el campus) y el chat con "Lo quiero" y respuestas rápidas ([WEB], [B201], [B210]). Reco compite en confianza (pago retenido, electrónica, revisión), no en velocidad.

---

## 1. Pagos

**Se paga fuera de la app.** El pago se pacta por chat.
- FAQ: "Comprador y vendedor acuerdan todo directamente por el chat de la app: precio, forma de pago y punto de entrega dentro del campus" ([WEB], FAQ "¿Cómo se hacen los pagos y las entregas?").
- Términos §1: "Todas las transacciones se realizan directamente entre compradores y vendedores". §4 menciona WhatsApp u otros medios acordados por las partes ([TOS]).
- La ficha de App Store dice "Conecta directamente con vendedores a través de WhatsApp" ([AS]). Nota: la web y el blog 2.0.1 describen un chat interno en tiempo real. La ficha de App Store parece anterior a ese chat; la versión actual usa el chat interno ([WEB], [B210]).

**Métodos aceptados:** no hay una lista. Los define cada pareja. El único ejemplo visible es un chat de demostración con entrega en la explanada ([WEB], sección "Chat en tiempo real"). Efectivo, transferencia o tarjeta: **no confirmado** como opciones oficiales.

**Comisión o suscripción:** ninguna por ahora. "Descargar la app y comprar es completamente gratis. Por el momento tampoco cobramos por vender [...] Si en el futuro agregamos planes para vendedores, te avisaremos con anticipación" ([WEB], FAQ). Los términos no mencionan comisiones ([TOS]). En App Store aparece sin compras dentro de la app ([AS]).

**Cómo ganan dinero:** **no confirmado.** Hoy no hay ingreso visible. Lo único que insinúa algo es la frase sobre "planes para vendedores" ([WEB]). Cualquier otra hipótesis (publicidad, destacados) sería inferencia mía y no la afirmo.

## 2. Protección del comprador y confianza

**Si el producto no llega o sale mal:** no hay protección formal.
- Las disputas "deberán resolverse únicamente entre las partes involucradas" ([TOS] §6).
- Appetite no responde por pérdida de ventas, disputas entre usuarios ni problemas con productos o entregas ([TOS] §12), y no garantiza la autenticidad de los anuncios ni verifica la información de los usuarios ([TOS] §6).
- Recomienda entregas en lugares públicos y concurridos ([TOS] §8; [WEB] FAQ).
- Reembolso, garantía o retención de pago: **no confirmado**, no aparece en ninguna fuente.

**Mecanismos de confianza que sí existen:**

| Mecanismo | Qué dice la fuente |
| --- | --- |
| Verificación de vendedor | Documento académico y foto sosteniéndolo; revisión humana ([WEB]) |
| Reseñas con estrellas | El vendedor marca "¿Se concretó la venta?" y el comprador recibe la invitación a calificar con estrellas y comentario. Con 3 estrellas o menos el comentario es obligatorio. Las estrellas salen en tarjetas, búsqueda y perfil ([B201]) |
| Reportes | "Sistema de reportes" y "motivos para reportar un producto" ([WEB], [B201]). En los términos no se describe ningún mecanismo de reporte ([TOS] §9) |
| Moderación | IA más reportes de la comunidad más strikes revisados por humanos ([WEB], sección Reglas) |
| Sanciones | Strikes; alcohol = suspensión permanente inmediata ([WEB] Reglas); suspensión o eliminación por fraude o abuso ([TOS] §3, §5, §10) |
| Privacidad | Los vendedores usan apodo y no se muestra su nombre real ([WEB]) |
| Bloqueo de usuarios | **No confirmado.** No se menciona en términos, web ni blog |

**Calidad de la confianza real:** una reseña de App Store 1★ (versión 2.0.1) dice que le eliminaron la cuenta sin enviar ningún mensaje al correo y que no le llega el código de recuperación ([RSS]). Es un caso aislado, pero muestra que el soporte puede ser débil.

## 3. Vender: volverse emprendedor

Fuente principal: [WEB], sección "Cómo vender" y FAQ. El texto "Unirme como emprendedor" no aparece literal en las fuentes públicas; la web dice "Únete como emprendedor". El nombre exacto del botón en la app: **no confirmado** (requiere las capturas).

**Flujo, en 4 pasos según [WEB]:**
1. **Únete como emprendedor:** elige un apodo ("jamás mostramos tu nombre real"), cuenta de tu negocio, y selecciona institución y campus.
2. **Valida que eres estudiante:** sube credencial escolar, historial académico o comprobante de inscripción. La FAQ añade una **foto tuya sosteniendo el documento**.
3. **Revisión humana:** "~24 horas" ("aproximadamente 24 horas hábiles" en la FAQ).
4. **Publica y vende.**

**Datos que piden** (de fuentes distintas): apodo, descripción del negocio, institución, campus(es), documento académico y selfie con el documento ([WEB]); correo, nombre y apellido y teléfono ([PRIV]); el blog menciona "géneros del registro" ([B201]), así que el registro incluye un campo de género. Inicio de sesión con Google y códigos de verificación (por correo o SMS: **no confirmado**) ([AS], [B210]).

**Verificación:** quién puede vender: "estudiantes en activo, registrados en un semestre de alguna institución educativa" ([TOS] §1). Los documentos se eliminan al validar la cuenta ([WEB]). El aviso de privacidad **no menciona** documentos de verificación ([PRIV]): inconsistencia entre fuentes.

**Reglas:**
- Prohibido: armas, drogas, vapeadores, productos robados, alcohol (suspensión permanente), acoso, descripciones engañosas, spam (publicar el mismo producto repetidamente) ([WEB] Reglas).
- Términos: también medicamentos con receta y productos peligrosos ([TOS] §5).

**Reglas de la app para vender:** el vendedor puede compartir productos y perfil por enlace, QR con logo, PDF imprimible e imagen, con atribución de canal ([B201]). Appetite Academy ofrece guías de formalización y de venta de alimentos ([ACA]).

## 4. Publicar

**Campos de un producto:** nombre, descripción detallada, precio en MXN, categorías y fotografías ([GP] "Publicar Producto"). Otros campos (disponibilidad, estado del artículo, ubicación): **no confirmado**.

**Fotos:** hasta 5 ([WEB], pasos 2/4 de Cómo funciona/vender). Mínimo: **no confirmado**. Desde la 2.0.1 hay cámara integrada para tomar las fotos ([B201]).

**Categorías:** 23 ([WEB]): Comida, Postres, Dulces, Bebidas, Ropa y calzado, Accesorios y joyería, Bolsos y mochilas, Perfumes, Belleza y salud, Gadgets y electrónica, Libros y revistas, Papelería, Arte, Música y entretenimiento, Deportes, Juguetes, Flores y plantas, Regalos, Para mascotas, Rutina y hogar, Cursos y talleres, Servicios.

**"Para ¡YA!":** "¿Llevas producto contigo en el campus? Activa Para ¡YA!, elige campus y hora de salida, y tus productos se listan hasta arriba del feed para entrega inmediata" ([WEB]). El ejemplo de la web muestra "En FCA, hasta las 2:30 pm" con la etiqueta "AHORA". Hay una sección "Para ¡YA!" en el inicio y un filtro "Para ¡YA!" en la búsqueda ([B210]). Es una hora límite ("hasta las…"), no una agenda por franjas. Si se puede programar a futuro: **no confirmado**.

**"Vendedor global" vs. por campus:** lo único confirmado es que el perfil del vendedor muestra "chips de campus o de vendedor global" ([B201]), y que un vendedor puede vender en uno o varios campus a la vez ([WEB], "Multi-campus"). Qué significa exactamente "global" (¿todos los campus?) y cómo se activa: **no confirmado**. Inferencia mía, sin verificar: "global" sería un vendedor sin campus fijo.

**Otros:** cada producto y cada vendedor tiene página pública en hey-appetite.com ([B201]).

## 5. Chat

- **Chat en tiempo real** dentro de la app, con notificaciones push ([WEB]). El chat arranca con una tarjeta del producto sobre el que se pregunta; arriba se ve con quién hablas y la foto del producto; hay palomitas de leído ([B210]).
- **"Lo quiero" y respuestas rápidas:** precargan el mensaje desde el detalle del producto, con una variante especial para productos en ¡YA! ([B201]). Desde la 2.1.0 **ya no envían el mensaje solos**: lo dejan escrito para que el usuario lo revise o cambie antes de enviarlo ([B210]). Los botones están al final de la ficha de producto ([B210]).
- **Textos de las preguntas rápidas** ("¿Dónde entregas?", "¿Estás en campus?", "¿Aceptas…?"): vienen de tu descripción de la app; ninguna fuente pública los cita literalmente. **No confirmado.**
- **Después del chat:** el vendedor ve la tarjeta "¿Se concretó la venta?" en la conversación. Su "sí" cuenta de inmediato y habilita la calificación del comprador. Contadores verdes avisan lo pendiente ([B201]). La venta es solo una marca de reputación: no mueve dinero.
- **Ofertas, contraofertas, estados de pedido:** no existen en las fuentes.

## 6. Perfil de vendedor y panel

- **Perfil rediseñado:** foto flotante, calificaciones a la vista, chips de campus o de vendedor global ([B201]). Las listas de productos de cada emprendedor llevan el mismo estilo nuevo ([B210]). Perfil público también en la web ([B201]).
- **Analytics:** "Mira cuántas personas ven tus productos y qué es lo que más jala" (ejemplo: "+124 vistas esta semana") ([WEB]). Qué más muestra el panel: **no confirmado**.
- **Mis Productos:** la pestaña que existía se convirtió en el botón coral Vender; ahí siguen los productos del emprendedor ([B210]). Descripción del negocio y apodo: ver §3. **Fecha de alta** en el perfil: **no confirmado.**
- Estados de publicación (borrador, pausado): **no confirmado**; la ficha dice "Organiza tus publicaciones activas" y "Actualiza precios y disponibilidad en tiempo real" ([AS]).

## 7. Navegación

Según [B210] (la fuente más confiable para el rediseño 2.1.0):
- **Barra inferior flotante con efecto de vidrio:** Inicio, Mensajes, Cuenta, y un botón coral **Vender** (antes pestaña "Mis Productos").
- **Inicio:** buscador y selector de campus juntos arriba. Secciones: Para ti ("Según lo que has visto"), Lo + nuevo ("Publicado hace poco"), Lo + popular ("Lo que más están viendo") y Para ¡YA!.
- **Búsqueda:** filtros por precio, categoría y Para ¡YA! en una hoja inferior; sugerencias cuando no hay resultados.
- **Ficha de producto:** foto a todo el ancho; categorías, nombre, precio grande, vendedor y campus; compartir y favoritos sobre la foto; preguntas rápidas y "Lo quiero" al final.
- **Chat, perfil de vendedor, acceso:** bienvenida, registro e inicio de sesión rediseñados.
- Selector de campus con "¿No encuentras tu escuela?" que abre WhatsApp con el mensaje precargado ([B201]).

Descripción pantalla por pantalla a partir de las capturas: **pendiente** (la carpeta no existe).

## Datos de contexto

- Desarrollador legal: E-MEXICO COMERCIO INTELIGENTE SAPI DE CV, Quintana Roo 134, 502, Col. Hipódromo Condesa, CDMX ([PRIV]). Equipo: Raúl Teohua Salinas Banegas (full-stack), Carlos Alfredo Pérez Espinosa (estrategia), Diego Rubén Salazar Jiménez (marketing), Arnold Cortes Cabrales (operaciones), Roni Hernández (backend) ([WEB]).
- Categoría: Shopping y Educación; gratis; clasificación de edad 17+ según el servicio de consulta de Apple ([AS]).
- **"24,108 campus"** es una cifra de la propia empresa ([WEB], [B210]). Los contadores de comunidad (emprendedores, productos, campus) aparecen en 0 en la página, así que **no hay cifra verificable de usuarios**. Dos reseñas piden más escuelas (EBC, UNITEC) y campus con pocos productos ([RSS]); eso sugiere que la cobertura "real" es mucho menor que la anunciada. Es una inferencia mía.
- Historial: 2.0.0 trajo contacto directo, Google Sign-In y favoritos; 2.0.1 (29 ago 2026) cerró el ciclo de venta y reseñas; 2.0.2 (9 sep 2026) migró el backend y obligó a actualizar; 2.1.0 (8 nov 2026) rediseñó la app ([AS], [B201], [B202], [B210]). Primera publicación en App Store: 5 de mayo de 2025 ([AS], vía API).
- Instagram: @hey_appetite, 689 seguidores; se presentan como "La primera App para estudiantes emprendedores" ([IG]).

---

## Appetite vs. Reco

Reco = lo que hay en `CLAUDE.md` y en el código del repo en `c9c81e6`. Esfuerzo: **B** bajo, **M** medio, **A** alto.

| Función | Qué hace Appetite | Qué hace Reco hoy | Recomendación | Esfuerzo |
| --- | --- | --- | --- | --- |
| Pago | Fuera de la app; se acuerda por chat. Sin comisión | Pago dentro de la app (simulado, tarjeta de prueba), retenido hasta que el comprador confirma. Comisión 13.6 % + $8 (`domain/Comision.kt`) | **Mantener** el pago retenido (ver sección final) | — |
| Monetización | Ninguna visible hoy; "planes para vendedores" a futuro | Comisión por pedido y certificación opcional de $99 | **Mantener** | — |
| Protección al comprador | Ninguna formal; la responsabilidad es de las partes | Pago retenido, revisión del equipo, confirmar entrega, "Reportar un problema" (hoy solo muestra un aviso); estado `en_reclamo` en el modelo, sin flujo | **Mantener y completar** el reclamo (hoy es solo un aviso) | M |
| Reseñas | Estrellas y comentario; 3★ o menos exige comentario; salen en tarjetas, búsqueda y perfil | Calificar vendedor al confirmar entrega; reseñas en Perfil público; insignia "Entrega puntual" | **Copiar** el comentario obligatorio con ≤3★ y mostrar estrellas en tarjetas y búsqueda | B |
| Reportes | Reportar producto con motivos; IA y strikes | Tabla `reportes`, 3 reportes ocultan el anuncio; en la app solo existe el texto "Reportar perfil" y un aviso | **Adaptar:** conectar el reporte de producto y perfil a Supabase, con motivos fijos | M |
| Bloqueo de usuarios | No confirmado | No existe | **Descartar** por ahora (poco valor en un modelo de pedidos, no de chat) | — |
| Verificación de estudiante | Documento + selfie con documento, revisión humana de ~24 h, borrado al validar | Login institucional (Microsoft/Google) y dominio de correo; sin documentos | **Mantener** (más rápida y automática). **Adaptar** como plan B: documento + revisión manual para quienes no tengan correo institucional | M |
| Alta de vendedor | Apodo, negocio, institución, campus y verificación de documento | Registro con teléfono, punto de entrega, horarios, CLABE simulada y reglas; requiere estar verificado | **Adaptar:** ofrecer apodo opcional en el perfil público (Reco ya oculta teléfono y CLABE) | B |
| Publicar | Nombre, descripción, precio, categorías, hasta 5 fotos, cámara integrada | Asistente de 5 pasos, 2 a 8 fotos, IMEI/serie, rango de precio 30–100 %, máximo 3 activos, moderación | **Mantener.** **Copiar** cámara integrada en el paso de fotos | B |
| Categorías | 23, todo tipo de producto y servicio | 6 (laptops, celulares, tablets, audio, gaming, calculadoras) | **Descartar:** el enfoque en electrónica es nuestro diferenciador | — |
| Para ¡YA! | Producto disponible hasta una hora, arriba del feed | Entrega en punto y horario fijos por vendedor al comprar; sin "disponible ahora" | **Adaptar** como insignia o filtro "Disponible hoy en [punto]" sobre los horarios del vendedor; no hacerlo en tiempo real | M |
| Vendedor global vs. campus | Vendedor en uno o varios campus; chip "global" (significado no confirmado) | Una universidad por perfil (`universidad_id`), BUAP | **Descartar** hasta tener más de una universidad | — |
| Chat "Lo quiero" y preguntas rápidas | Chat en tiempo real; "Lo quiero" deja el mensaje escrito | Sin chat (fuera de alcance); se compra con carrito | **Adaptar** versión ligera: botón "Preguntar" con mensajes rápidos (¿Dónde entregas?, ¿Sigue disponible?) en Detalle de producto. Chat en tiempo real: **descartar** hasta después de la entrega | A (chat completo) / M (preguntas rápidas) |
| "¿Se concretó la venta?" | Tarjeta en el chat; la marca el vendedor | El comprador confirma la entrega y libera el pago; el vendedor marca "entregado" | **Mantener** el nuestro (el comprador confirma, que protege más) | — |
| Perfil público | Foto, estrellas, chips de campus, página web pública | Perfil público con pestañas A la venta / Reseñas e insignia; sin datos de contacto | **Mantener.** **Copiar** compartir perfil o producto por enlace y QR | M |
| Panel del vendedor | Analytics de vistas y "lo que más jala" | Ganancias del mes, anuncios con estados, ventas con ganancia neta | **Copiar** contador de vistas por anuncio | M |
| Navegación | Inicio, Mensajes, Cuenta y botón Vender (coral) | Comprador: Inicio, Favoritos, Carrito, Compras, Perfil. Vendedor: Panel, Anuncios, "+", Ventas, Perfil | **Mantener.** El botón central "+" ya cubre lo equivalente a Vender | — |
| Reglas y sanciones | Strikes, suspensión por alcohol y fraude | 3 reportes ocultan; 3 rechazos por fraude suspenden la cuenta; reglas de publicación detalladas | **Mantener** | — |
| Educación al vendedor | Appetite Academy (RSS/RESICO, higiene) | No existe | **Descartar** (fuera de alcance) | — |

---

## Pago: retenido (Reco) frente a "acuerdan por chat" (Appetite)

Contexto: la comisión de Reco (13.6 % + $8, con certificación opcional de $99) es nuestra monetización ([CLAUDE.md](../CLAUDE.md), sección Monetización). Appetite hoy no cobra; esto lo hace barato de adoptar y sin ingresos.

### Pago retenido dentro de la app (Reco)

**Pros**
- **Es el único lugar donde podemos cobrar.** Sin pago en la app no hay comisión.
- Resuelve el miedo principal de comprar electrónica usada entre desconocidos: que el dinero se pierda. Es la base del diferenciador (junto a la revisión y la verificación).
- Hay un mecanismo para disputas: mientras el dinero está retenido, el reclamo es posible.
- Hace posible la reputación con datos verificados: solo califica quien compró de verdad.
- Los tickets son altos (celulares, laptops), donde la retención se justifica; en comida de $80 no tendría sentido.

**Contras**
- Fricción: tarjeta, alta bancaria de vendedores (CLABE) y espera de la liberación. Appetite vende "un chat y ya".
- **Riesgo de fuga:** compradores y vendedores pueden llevarse la transacción a WhatsApp para evitar la comisión de 13.6 %. Es el riesgo clásico de todo marketplace con comisión.
- Obligaciones fiscales, regulatorias y de soporte (reembolsos, contracargos, KYC). Hoy el pago es simulado y esto aún no se ha enfrentado.
- Costos de la pasarela (Stripe cobra por transacción; **no confirmado** el monto exacto para México y no lo verifiqué).
- Si la verificación del vendedor o el reclamo funcionan mal, la promesa de protección se rompe y el daño es mayor.

### "Acuerdan por chat" (Appetite)

**Pros**
- Cero fricción y cero soporte financiero; crecimiento rápido y barato.
- Sin responsabilidad legal sobre el dinero (los términos la descargan por completo).
- Funciona bien con artículos baratos, comida y servicios entre conocidos del campus.

**Contras**
- **Sin ingresos propios:** la monetización queda en "planes para vendedores" futuros ([WEB]).
- Sin protección real; la confianza depende solo de reseñas y moderación.
- Se vuelve difícil de medir: Appetite sabe cuántos chats hay, pero no si se concretó una venta ni por cuánto, salvo la tarjeta "¿Se concretó la venta?" que depende de que el vendedor la marque ([B201]).
- Para electrónica de miles de pesos, el riesgo de fraude es mucho mayor.

### Recomendación

1. **Mantener el pago retenido.** Es la monetización y el diferenciador; "acuerdan por chat" convierte a Reco en un clon sin ingresos de Appetite.
2. **Reducir la fricción donde sea barato:** mantener la entrega en un punto del campus, con la confirmación en el momento de la entrega, y mostrar el costo de la comisión al vendedor antes de publicar (ya se muestra la ganancia neta).
3. **Contrarrestar la fuga** con lo que el chat no puede dar: garantía de 3 meses y revisión (certificación), reseñas solo de compras reales y prioridad en búsquedas. Evitar mostrar teléfono antes de comprar (ya es así: el perfil público no muestra contacto).
4. **Copiar de Appetite lo que no toca el dinero:** preguntas rápidas previas a la compra, "Disponible hoy" y compartir por enlace o QR.
5. **Pendiente de decidir (no investigado):** si la comisión de 13.6 % es competitiva para estudiantes frente a un modelo gratuito. Una opción a evaluar es una tarifa menor o fija en artículos baratos; no tengo datos para recomendar una cifra.
