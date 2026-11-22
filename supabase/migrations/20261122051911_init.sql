-- Reco v2 · esquema de Supabase (tablas, funciones, triggers y RLS)
-- Migración inicial (supabase db push). Los datos de prueba van aparte en seed.sql (scripts/seed_dummyjson.py).
-- El moderador no tiene pantallas: aprueba o rechaza anuncios desde el panel de Supabase
-- (SQL Editor o Table Editor), donde auth.uid() es nulo y las restricciones de abajo no aplican.

create extension if not exists "pgcrypto";

-- ---------------------------------------------------------------- Catálogo y datos de apoyo
create table universidades (
  id        uuid primary key default gen_random_uuid(),
  nombre    text not null,
  dominio   text not null unique   -- ej. alumno.buap.mx; la verificación simulada solo compara el final del correo
);

create table puntos_entrega (
  id              uuid primary key default gen_random_uuid(),
  universidad_id  uuid not null references universidades(id),
  nombre          text not null,
  descripcion     text,
  horario         text not null default 'Lun a Vie · 10:00 – 18:00'
);

create type categoria as enum ('laptops', 'celulares', 'tablets', 'audio', 'gaming', 'calculadoras', 'accesorios');

create table modelos (
  id                uuid primary key default gen_random_uuid(),
  categoria         categoria not null,
  marca             text not null,
  modelo            text not null,
  anio              int  not null,
  precio_referencia int  not null check (precio_referencia > 0),
  almacenamientos   text[] not null default '{}',
  rams              text[] not null default '{}',
  colores           text[] not null default '{}',
  procesador        text,
  pantalla          text,
  unique (marca, modelo)
);

-- IMEI o números de serie reportados.
create table imei_bloqueados (
  imei text primary key
);

-- ---------------------------------------------------------------- Perfiles
create table perfiles (
  id                  uuid primary key references auth.users(id) on delete cascade,
  nombre              text not null,
  carrera             text,
  semestre            int,
  universidad_id      uuid references universidades(id),
  correo_institucional text,
  verificado          boolean not null default false,
  es_vendedor         boolean not null default false,
  ultimo_modo         text check (ultimo_modo in ('comprador', 'vendedor')),
  telefono            text,
  punto_preferido_id  uuid references puntos_entrega(id),
  disponibilidad      jsonb,                 -- {"dias": ["Lun","Jue"], "franjas": ["Tarde"]}
  clabe_simulada      text check (clabe_simulada is null or clabe_simulada ~ '^[0-9]{18}$'),
  -- Perfil del vendedor y preguntas rápidas del Detalle (v2)
  apodo               text,
  acerca_de           text,
  donde_entregas      text,                  -- respuesta rápida: ¿dónde entregas?
  horario             text,                  -- respuesta rápida: ¿en qué horario?
  acepta_tarjeta      boolean not null default false,  -- respuesta rápida: ¿aceptas tarjeta?
  reputacion          numeric(2,1) not null default 0,
  ventas_completadas  int not null default 0,
  rechazos            int not null default 0,
  suspendido          boolean not null default false,
  creado              timestamptz not null default now()
);

-- ---------------------------------------------------------------- Anuncios
create type estado_anuncio as enum
  ('borrador', 'en_revision', 'publicado', 'rechazado', 'reservado', 'vendido', 'pausado');

create table productos (
  id              uuid primary key default gen_random_uuid(),
  vendedor_id     uuid not null references perfiles(id),
  modelo_id       uuid not null references modelos(id),
  almacenamiento  text,
  ram             text,
  color           text not null,
  grado           text not null check (grado in ('A', 'B', 'C')),
  salud_bateria   int check (salud_bateria between 1 and 100),
  imei            text,                      -- IMEI (celulares y tablets) o número de serie
  accesorios      text[] not null default '{}',
  descripcion     text,
  precio          int not null default 0 check (precio >= 0),
  carreras        text[] not null default '{}',
  estado          estado_anuncio not null default 'borrador',
  certificar      boolean not null default false,   -- lo pidió el vendedor
  certificado     boolean not null default false,   -- lo marcó el moderador
  revision_manual boolean not null default false,   -- precio fuera del 30 %–100 % de la referencia
  motivo_rechazo  text,
  creado          timestamptz not null default now()
);
create index on productos (estado);
create index on productos (vendedor_id);
-- Un IMEI/serie no puede estar en otro anuncio vivo.
create unique index productos_imei_activo on productos (imei)
  where imei is not null and estado in ('en_revision', 'publicado', 'reservado', 'pausado', 'vendido');

create table fotos (
  id           uuid primary key default gen_random_uuid(),
  producto_id  uuid not null references productos(id) on delete cascade,
  url          text not null,                -- URL pública en Storage (bucket "fotos")
  orden        int not null default 0
);
create index on fotos (producto_id, orden);

-- ---------------------------------------------------------------- Favoritos y reportes
create table favoritos (
  usuario_id   uuid not null references perfiles(id) on delete cascade,
  producto_id  uuid not null references productos(id) on delete cascade,
  primary key (usuario_id, producto_id)
);

create table reportes (
  id           uuid primary key default gen_random_uuid(),
  producto_id  uuid not null references productos(id) on delete cascade,
  usuario_id   uuid not null references perfiles(id),
  motivo       text not null,
  creado       timestamptz not null default now(),
  unique (producto_id, usuario_id)
);

-- ---------------------------------------------------------------- Pedidos (compra directa: un pedido = un equipo)
create type estado_pedido as enum ('pagado_retenido', 'entregado', 'liberado', 'en_reclamo', 'cancelado');

create table pedidos (
  id                uuid primary key default gen_random_uuid(),
  producto_id       uuid not null unique references productos(id),   -- cada equipo es pieza única
  comprador_id      uuid not null references perfiles(id),
  vendedor_id       uuid not null references perfiles(id),
  monto             int not null check (monto > 0),
  comision          int not null check (comision >= 0),
  punto_id          uuid references puntos_entrega(id),
  horario           text,
  estado            estado_pedido not null default 'pagado_retenido',
  creado            timestamptz not null default now(),
  check (comprador_id <> vendedor_id)
);

create table calificaciones (
  id           uuid primary key default gen_random_uuid(),
  pedido_id    uuid not null unique references pedidos(id),
  estrellas    int not null check (estrellas between 1 and 5),
  etiquetas    text[] not null default '{}', -- Puntual, Como se describió, Amable, Buen precio
  comentario   text,
  creado       timestamptz not null default now(),
  -- Con 3 estrellas o menos el comentario es obligatorio.
  check (estrellas > 3 or length(btrim(coalesce(comentario, ''))) > 0)
);

-- ---------------------------------------------------------------- Comisión (un solo lugar): 13.6 % + $8 MXN por pedido
create function calcular_comision(monto int) returns int
language sql immutable as $$ select round(monto * 0.136)::int + 8 $$;

create function pedido_calcula_comision() returns trigger language plpgsql as $$
begin
  new.comision := calcular_comision(new.monto);
  return new;
end $$;
create trigger pedidos_comision before insert on pedidos
  for each row execute function pedido_calcula_comision();

-- ---------------------------------------------------------------- Reglas que no se pueden saltar desde la app
-- "Cliente" = una petición hecha por la app con la sesión del usuario (rol authenticated).
-- El moderador trabaja desde el panel de Supabase (rol postgres) y no pasa por estas restricciones.
create function es_cliente() returns boolean language sql stable as $$ select current_user = 'authenticated' $$;

-- Precio fuera del 30 %–100 % de la referencia: lo calcula el servidor, no el cliente.
create function calcular_revision_manual() returns trigger language plpgsql as $$
declare ref int;
begin
  select precio_referencia into ref from modelos where id = new.modelo_id;
  new.revision_manual := new.precio > 0 and (new.precio < ref * 0.30 or new.precio > ref);
  return new;
end $$;
create trigger productos_revision before insert or update on productos
  for each row execute function calcular_revision_manual();

-- Altas: un anuncio nuevo solo puede ser borrador o estar en revisión; certificado y motivo no los escribe el cliente.
create function proteger_alta_anuncio() returns trigger language plpgsql as $$
begin
  if es_cliente() then
    if new.estado not in ('borrador', 'en_revision') then
      raise exception 'Un anuncio nuevo solo puede ser borrador o estar en revisión';
    end if;
    new.certificado := false;
    new.motivo_rechazo := null;
  end if;
  return new;
end $$;
create trigger productos_alta before insert on productos
  for each row execute function proteger_alta_anuncio();

-- Cambios: el cliente solo hace estas transiciones; Publicado, Rechazado, Reservado y Vendido son del moderador y del servidor.
-- Si cambia el contenido de un anuncio ya publicado, vuelve a revisión.
create function proteger_estado_anuncio() returns trigger language plpgsql as $$
declare contenido_cambio boolean;
begin
  if es_cliente() then
    if new.vendedor_id is distinct from old.vendedor_id then
      raise exception 'No se puede cambiar el vendedor de un anuncio';
    end if;
    new.certificado := old.certificado;
    new.motivo_rechazo := old.motivo_rechazo;

    if new.estado is distinct from old.estado and not (
         (old.estado = 'borrador'  and new.estado = 'en_revision') or
         (old.estado = 'rechazado' and new.estado = 'en_revision') or
         (old.estado = 'publicado' and new.estado = 'pausado') or
         (old.estado = 'pausado'   and new.estado = 'publicado')) then
      raise exception 'Cambio de estado no permitido: % a %', old.estado, new.estado;
    end if;

    contenido_cambio := (new.modelo_id, new.almacenamiento, new.ram, new.color, new.grado, new.salud_bateria,
                         new.imei, new.accesorios, new.descripcion, new.precio)
                        is distinct from
                        (old.modelo_id, old.almacenamiento, old.ram, old.color, old.grado, old.salud_bateria,
                         old.imei, old.accesorios, old.descripcion, old.precio);
    if contenido_cambio and old.estado in ('publicado', 'pausado', 'reservado') then
      new.estado := 'en_revision';
    end if;
  end if;
  return new;
end $$;
create trigger productos_estado before update on productos
  for each row execute function proteger_estado_anuncio();

-- Máximo 3 anuncios activos por vendedor.
create function limitar_anuncios_activos() returns trigger language plpgsql as $$
declare activos int;
begin
  if new.estado in ('en_revision', 'publicado', 'reservado') then
    select count(*) into activos from productos
      where vendedor_id = new.vendedor_id and id <> new.id and estado in ('en_revision', 'publicado', 'reservado');
    if activos >= 3 then raise exception 'Máximo 3 anuncios activos'; end if;
  end if;
  return new;
end $$;
-- Dispara en todo UPDATE (no solo "of estado"): productos_estado puede cambiar new.estado y corre antes (orden alfabético).
create trigger productos_limite before insert or update on productos
  for each row execute function limitar_anuncios_activos();

-- Nadie compra sus propios anuncios ni equipos que no están publicados; al pagarse, el equipo sale del catálogo.
create function validar_pedido() returns trigger language plpgsql security definer set search_path = public as $$
declare p productos;
begin
  select * into p from productos where id = new.producto_id for update;
  if p.vendedor_id = new.comprador_id then raise exception 'No puedes comprar tu propio anuncio'; end if;
  if p.vendedor_id <> new.vendedor_id then raise exception 'El equipo no es de este vendedor'; end if;
  if p.estado <> 'publicado' then raise exception 'El equipo ya no está disponible'; end if;
  update productos set estado = 'vendido' where id = p.id;
  return new;
end $$;
create trigger pedidos_validar before insert on pedidos
  for each row execute function validar_pedido();

-- Con 3 reportes el anuncio se oculta y vuelve a revisión.
create function ocultar_por_reportes() returns trigger language plpgsql security definer set search_path = public as $$
begin
  if (select count(*) from reportes where producto_id = new.producto_id) >= 3 then
    update productos set estado = 'en_revision' where id = new.producto_id and estado = 'publicado';
  end if;
  return new;
end $$;
create trigger reportes_ocultar after insert on reportes
  for each row execute function ocultar_por_reportes();

-- Con 3 rechazos por fraude la cuenta se suspende.
create function contar_rechazos() returns trigger language plpgsql security definer set search_path = public as $$
begin
  if new.estado = 'rechazado' and old.estado is distinct from 'rechazado' then
    update perfiles set rechazos = rechazos + 1, suspendido = (rechazos + 1 >= 3) where id = new.vendedor_id;
  end if;
  return new;
end $$;
create trigger productos_rechazos after update of estado on productos
  for each row execute function contar_rechazos();

-- Al liberar un pago sube el contador de ventas; al calificar se recalcula la reputación.
create function sumar_venta() returns trigger language plpgsql security definer set search_path = public as $$
begin
  if new.estado = 'liberado' and old.estado is distinct from 'liberado' then
    update perfiles set ventas_completadas = ventas_completadas + 1 where id = new.vendedor_id;
  end if;
  return new;
end $$;
create trigger pedidos_venta after update of estado on pedidos
  for each row execute function sumar_venta();

create function recalcular_reputacion() returns trigger language plpgsql security definer set search_path = public as $$
declare v uuid;
begin
  select vendedor_id into v from pedidos where id = new.pedido_id;
  update perfiles set reputacion = (
    select round(avg(c.estrellas)::numeric, 1) from calificaciones c join pedidos p on p.id = c.pedido_id where p.vendedor_id = v
  ) where id = v;
  return new;
end $$;
create trigger calificaciones_reputacion after insert on calificaciones
  for each row execute function recalcular_reputacion();

-- ---------------------------------------------------------------- Funciones que la app puede llamar (SECURITY DEFINER)
-- La verificación, el modo vendedor y los cambios de estado del pedido NO se escriben con UPDATE desde la app.

-- Verificación SIMULADA (portal dentro de la app): solo comprueba que el correo termine en @<dominio de una universidad>
-- (hoy @alumno.buap.mx). Sin Azure ni Google. p_correo es el que devuelve el portal; si no se manda, se usa el de la cuenta.
-- OJO: al ser simulada confía en el correo que manda la app; con login institucional real habría que atarlo al proveedor.
create function verificar_estudiante(p_correo text default null) returns void
language plpgsql security definer set search_path = public, auth as $$
declare uni uuid; correo text;
begin
  if auth.uid() is null then raise exception 'Inicia sesión primero'; end if;
  correo := lower(btrim(coalesce(p_correo, (select email from auth.users where id = auth.uid()))));
  select u.id into uni from universidades u
   where correo ~ '^[^@[:space:]]+@[^@[:space:]]+$' and split_part(correo, '@', 2) = lower(u.dominio);
  if uni is null then raise exception 'El correo debe terminar en @alumno.buap.mx'; end if;
  update perfiles set verificado = true, universidad_id = uni, correo_institucional = correo where id = auth.uid();
  if not found then raise exception 'No existe el perfil'; end if;
end $$;

create function activar_vendedor(p_telefono text, p_punto uuid, p_disponibilidad jsonb, p_clabe text)
returns void language plpgsql security definer set search_path = public as $$
begin
  if not exists (select 1 from perfiles where id = auth.uid() and verificado and not suspendido) then
    raise exception 'Verifica tu cuenta de estudiante primero';
  end if;
  update perfiles set es_vendedor = true, telefono = p_telefono, punto_preferido_id = p_punto,
    disponibilidad = p_disponibilidad, clabe_simulada = p_clabe where id = auth.uid();
end $$;

-- Pedido: el vendedor marca entregado; el comprador confirma y se libera el pago.
create function marcar_entregado(p_pedido uuid) returns void language plpgsql security definer set search_path = public as $$
begin
  update pedidos set estado = 'entregado' where id = p_pedido and vendedor_id = auth.uid() and estado = 'pagado_retenido';
  if not found then raise exception 'No se puede marcar como entregado'; end if;
end $$;

create function confirmar_entrega(p_pedido uuid) returns void language plpgsql security definer set search_path = public as $$
begin
  update pedidos set estado = 'liberado' where id = p_pedido and comprador_id = auth.uid() and estado = 'entregado';
  if not found then raise exception 'No se puede confirmar la entrega'; end if;
end $$;

-- Crear el pedido (compra directa, un solo equipo): lo llama cualquier usuario verificado y autenticado.
-- El comprador es siempre auth.uid() y el monto sale del precio real del anuncio; el cliente nunca los manda.
create function crear_pedido(p_producto uuid, p_punto uuid, p_horario text)
returns uuid language plpgsql security definer set search_path = public as $$
declare prod productos; pid uuid;
begin
  if auth.uid() is null then raise exception 'Inicia sesión primero'; end if;
  if not exists (select 1 from perfiles where id = auth.uid() and verificado and not suspendido) then
    raise exception 'Verifica tu cuenta de estudiante primero';
  end if;
  select * into prod from productos where id = p_producto and estado = 'publicado';
  if not found or prod.precio <= 0 then raise exception 'El equipo ya no está disponible'; end if;
  insert into pedidos (producto_id, comprador_id, vendedor_id, monto, comision, punto_id, horario)
    values (prod.id, auth.uid(), prod.vendedor_id, prod.precio, 0, p_punto, p_horario) returning id into pid;
  return pid;
end $$;

revoke execute on function verificar_estudiante(text), activar_vendedor(text, uuid, jsonb, text),
  marcar_entregado(uuid), confirmar_entrega(uuid), crear_pedido(uuid, uuid, text) from public, anon;
grant execute on function verificar_estudiante(text), activar_vendedor(text, uuid, jsonb, text),
  marcar_entregado(uuid), confirmar_entrega(uuid), crear_pedido(uuid, uuid, text) to authenticated;

-- ---------------------------------------------------------------- RLS
alter table universidades    enable row level security;
alter table puntos_entrega   enable row level security;
alter table modelos          enable row level security;
alter table imei_bloqueados  enable row level security;
alter table perfiles         enable row level security;
alter table productos        enable row level security;
alter table fotos            enable row level security;
alter table favoritos        enable row level security;
alter table reportes         enable row level security;
alter table pedidos          enable row level security;
alter table calificaciones   enable row level security;

-- Catálogos: lectura para cualquier sesión. imei_bloqueados no se expone (se valida con triggers/servidor).
create policy "leer universidades"  on universidades  for select to authenticated using (true);
create policy "leer puntos"         on puntos_entrega for select to authenticated using (true);
create policy "leer modelos"        on modelos        for select to authenticated using (true);

-- Perfiles: la tabla solo la ve su dueño (tiene teléfono y CLABE). Los demás ven la vista pública, sin datos de contacto.
create policy "leer mi perfil"   on perfiles for select to authenticated using (id = auth.uid());
create policy "crear mi perfil"  on perfiles for insert to authenticated
  with check (id = auth.uid() and not verificado and not es_vendedor and reputacion = 0
              and ventas_completadas = 0 and rechazos = 0 and not suspendido);
create policy "editar mi perfil" on perfiles for update to authenticated using (id = auth.uid()) with check (id = auth.uid());
-- Solo estas columnas se editan con UPDATE; verificado, es_vendedor, reputacion, ventas, rechazos y suspendido
-- cambian únicamente con las funciones de arriba, los triggers o el moderador.
revoke update on perfiles from authenticated;
grant update (nombre, carrera, semestre, telefono, punto_preferido_id, disponibilidad, clabe_simulada, ultimo_modo,
              apodo, acerca_de, donde_entregas, horario, acepta_tarjeta)
  on perfiles to authenticated;

create view perfiles_publicos with (security_invoker = false) as
  select id, nombre, apodo, acerca_de, carrera, universidad_id, verificado, es_vendedor, reputacion, ventas_completadas,
         donde_entregas, horario, acepta_tarjeta, punto_preferido_id, creado
  from perfiles;
revoke all on perfiles_publicos from anon, public;
grant select on perfiles_publicos to authenticated;

-- Productos: el comprador solo lee los publicados (y los que ya compró); el vendedor solo edita los suyos.
create policy "leer productos" on productos for select to authenticated
  using (estado = 'publicado' or vendedor_id = auth.uid()
         or id in (select producto_id from pedidos where comprador_id = auth.uid()));
create policy "crear mis productos" on productos for insert to authenticated
  with check (vendedor_id = auth.uid() and exists (select 1 from perfiles where id = auth.uid() and es_vendedor and not suspendido));
create policy "editar mis productos" on productos for update to authenticated
  using (vendedor_id = auth.uid()) with check (vendedor_id = auth.uid());
create policy "borrar mis productos" on productos for delete to authenticated
  using (vendedor_id = auth.uid() and estado in ('borrador', 'rechazado', 'pausado'));

create policy "leer fotos" on fotos for select to authenticated
  using (producto_id in (select id from productos));
create policy "gestionar mis fotos" on fotos for all to authenticated
  using (producto_id in (select id from productos where vendedor_id = auth.uid()))
  with check (producto_id in (select id from productos where vendedor_id = auth.uid()));

create policy "mis favoritos" on favoritos for all to authenticated using (usuario_id = auth.uid()) with check (usuario_id = auth.uid());
create policy "reportar"      on reportes  for insert to authenticated with check (usuario_id = auth.uid());
create policy "ver mis reportes" on reportes for select to authenticated using (usuario_id = auth.uid());

-- Pedidos: solo lectura para sus dos partes. Se crean con crear_pedido() y cambian de estado
-- con marcar_entregado() y confirmar_entrega(); la app no tiene INSERT ni UPDATE sobre la tabla.
create policy "leer mis pedidos" on pedidos for select to authenticated
  using (comprador_id = auth.uid() or vendedor_id = auth.uid());
revoke insert, update, delete on pedidos from authenticated, anon;

-- Calificaciones: se leen en el perfil público; solo el comprador califica, una vez, un pedido ya liberado.
create policy "leer calificaciones" on calificaciones for select to authenticated using (true);
create policy "calificar mi pedido" on calificaciones for insert to authenticated
  with check (pedido_id in (
    select p.id from pedidos p
    where p.comprador_id = auth.uid() and p.estado = 'liberado'));

-- Catálogo de lectura pública con la anon key (solo anuncios publicados y sus fotos). Lo demás sigue cerrado a anon.
create policy "catalogo publico productos" on productos for select to anon using (estado = 'publicado');
create policy "catalogo publico fotos" on fotos for select to anon
  using (producto_id in (select id from productos where estado = 'publicado'));
create policy "catalogo publico modelos" on modelos for select to anon using (true);
create policy "catalogo publico puntos" on puntos_entrega for select to anon using (true);
revoke all on all tables in schema public from anon;
grant select on productos, fotos, modelos, puntos_entrega to anon;

-- ---------------------------------------------------------------- Storage: bucket "fotos" (lectura pública, escritura de la carpeta propia)
insert into storage.buckets (id, name, public) values ('fotos', 'fotos', true) on conflict do nothing;
create policy "subir mis fotos" on storage.objects for insert to authenticated
  with check (bucket_id = 'fotos' and (storage.foldername(name))[1] = auth.uid()::text);
create policy "borrar mis fotos" on storage.objects for delete to authenticated
  using (bucket_id = 'fotos' and (storage.foldername(name))[1] = auth.uid()::text);
