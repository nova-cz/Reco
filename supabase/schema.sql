-- Reco · esquema de Supabase (tablas, funciones, triggers y RLS)
-- Se corre una vez en el SQL Editor del proyecto. Después, seed.sql.
-- El moderador no tiene pantallas: aprueba o rechaza anuncios desde el panel de Supabase
-- (SQL Editor o Table Editor), donde auth.uid() es nulo y las restricciones de abajo no aplican.

create extension if not exists "pgcrypto";

-- ---------------------------------------------------------------- Catálogo y datos de apoyo
create table universidades (
  id        uuid primary key default gen_random_uuid(),
  nombre    text not null,
  dominio   text not null unique
);

create table puntos_entrega (
  id              uuid primary key default gen_random_uuid(),
  universidad_id  uuid not null references universidades(id),
  nombre          text not null,
  descripcion     text,
  horario         text not null default 'Lun a Vie · 10:00 – 18:00'
);

create type categoria as enum ('laptops', 'celulares', 'tablets', 'audio', 'gaming', 'calculadoras');

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
  url          text not null,                -- ruta en Storage (bucket "fotos")
  orden        int not null default 0
);
create index on fotos (producto_id, orden);

-- ---------------------------------------------------------------- Carrito, favoritos, reportes
create table carrito (
  usuario_id   uuid not null references perfiles(id) on delete cascade,
  producto_id  uuid not null references productos(id) on delete cascade,
  agregado     timestamptz not null default now(),
  primary key (usuario_id, producto_id)
);

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

-- ---------------------------------------------------------------- Pedidos (uno por vendedor, con varios equipos)
create type estado_pedido as enum ('pagado_retenido', 'entregado', 'liberado', 'en_reclamo', 'cancelado');

create table pedidos (
  id                uuid primary key default gen_random_uuid(),
  comprador_id      uuid not null references perfiles(id),
  vendedor_id       uuid not null references perfiles(id),
  monto             int not null check (monto > 0),
  comision          int not null check (comision >= 0),
  stripe_payment_id text,
  punto_id          uuid references puntos_entrega(id),
  horario           text,
  estado            estado_pedido not null default 'pagado_retenido',
  creado            timestamptz not null default now(),
  check (comprador_id <> vendedor_id)
);

create table pedido_items (
  pedido_id    uuid not null references pedidos(id) on delete cascade,
  producto_id  uuid not null references productos(id),
  primary key (pedido_id, producto_id),
  unique (producto_id)                       -- cada equipo es pieza única
);

create table calificaciones (
  id           uuid primary key default gen_random_uuid(),
  pedido_id    uuid not null unique references pedidos(id),
  estrellas    int not null check (estrellas between 1 and 5),
  etiquetas    text[] not null default '{}', -- Puntual, Como se describió, Amable, Buen precio
  comentario   text,
  creado       timestamptz not null default now()
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
-- Solo el moderador (sin auth.uid(), desde el panel) pasa un anuncio a publicado o rechazado.
create function proteger_estado_anuncio() returns trigger language plpgsql as $$
begin
  if auth.uid() is not null then
    if new.estado in ('publicado', 'rechazado') and new.estado is distinct from old.estado then
      raise exception 'Solo el moderador puede publicar o rechazar un anuncio';
    end if;
    if new.certificado is distinct from old.certificado or new.motivo_rechazo is distinct from old.motivo_rechazo then
      raise exception 'Solo el moderador puede certificar o rechazar';
    end if;
  end if;
  return new;
end $$;
create trigger productos_estado before update on productos
  for each row execute function proteger_estado_anuncio();

-- Los anuncios nuevos nacen como borrador o en revisión, nunca ya publicados.
create function proteger_alta_anuncio() returns trigger language plpgsql as $$
begin
  if auth.uid() is not null and new.estado not in ('borrador', 'en_revision') then
    raise exception 'Un anuncio nuevo solo puede ser borrador o estar en revisión';
  end if;
  return new;
end $$;
create trigger productos_alta before insert on productos
  for each row execute function proteger_alta_anuncio();

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
create trigger productos_limite before insert or update of estado on productos
  for each row execute function limitar_anuncios_activos();

-- Nadie compra sus propios anuncios ni equipos que no están publicados.
create function validar_item_pedido() returns trigger language plpgsql as $$
declare p productos; ped pedidos;
begin
  select * into p from productos where id = new.producto_id;
  select * into ped from pedidos where id = new.pedido_id;
  if p.vendedor_id = ped.comprador_id then raise exception 'No puedes comprar tu propio anuncio'; end if;
  if p.vendedor_id <> ped.vendedor_id then raise exception 'El equipo no es de este vendedor'; end if;
  if p.estado <> 'publicado' then raise exception 'El equipo ya no está disponible'; end if;
  update productos set estado = 'vendido' where id = p.id;   -- sale del catálogo al pagarse
  return new;
end $$;
create trigger pedido_items_validar before insert on pedido_items
  for each row execute function validar_item_pedido();

-- Con 3 reportes el anuncio se oculta y vuelve a revisión.
create function ocultar_por_reportes() returns trigger language plpgsql as $$
begin
  if (select count(*) from reportes where producto_id = new.producto_id) >= 3 then
    update productos set estado = 'en_revision' where id = new.producto_id and estado = 'publicado';
  end if;
  return new;
end $$;
create trigger reportes_ocultar after insert on reportes
  for each row execute function ocultar_por_reportes();

-- Con 3 rechazos por fraude la cuenta se suspende.
create function contar_rechazos() returns trigger language plpgsql as $$
begin
  if new.estado = 'rechazado' and old.estado is distinct from 'rechazado' then
    update perfiles set rechazos = rechazos + 1, suspendido = (rechazos + 1 >= 3) where id = new.vendedor_id;
  end if;
  return new;
end $$;
create trigger productos_rechazos after update of estado on productos
  for each row execute function contar_rechazos();

-- Al liberar un pago sube el contador de ventas; al calificar se recalcula la reputación.
create function sumar_venta() returns trigger language plpgsql as $$
begin
  if new.estado = 'liberado' and old.estado is distinct from 'liberado' then
    update perfiles set ventas_completadas = ventas_completadas + 1 where id = new.vendedor_id;
  end if;
  return new;
end $$;
create trigger pedidos_venta after update of estado on pedidos
  for each row execute function sumar_venta();

create function recalcular_reputacion() returns trigger language plpgsql as $$
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

-- ---------------------------------------------------------------- RLS
alter table universidades    enable row level security;
alter table puntos_entrega   enable row level security;
alter table modelos          enable row level security;
alter table imei_bloqueados  enable row level security;
alter table perfiles         enable row level security;
alter table productos        enable row level security;
alter table fotos            enable row level security;
alter table carrito          enable row level security;
alter table favoritos        enable row level security;
alter table reportes         enable row level security;
alter table pedidos          enable row level security;
alter table pedido_items     enable row level security;
alter table calificaciones   enable row level security;

-- Catálogos: lectura para cualquier sesión. imei_bloqueados no se expone (se valida en el servidor).
create policy "leer universidades"  on universidades  for select to authenticated using (true);
create policy "leer puntos"         on puntos_entrega for select to authenticated using (true);
create policy "leer modelos"        on modelos        for select to authenticated using (true);

-- Perfiles: cada quien ve y edita el suyo; los demás ven el perfil público (nombre, reputación, ventas).
create policy "leer perfiles"   on perfiles for select to authenticated using (true);
create policy "crear mi perfil" on perfiles for insert to authenticated with check (id = auth.uid());
create policy "editar mi perfil" on perfiles for update to authenticated using (id = auth.uid()) with check (id = auth.uid());

-- Productos: el comprador solo lee los publicados (y los suyos); el vendedor solo edita los suyos.
create policy "leer productos" on productos for select to authenticated
  using (estado = 'publicado' or vendedor_id = auth.uid()
         or id in (select producto_id from pedido_items i join pedidos p on p.id = i.pedido_id
                   where p.comprador_id = auth.uid()));
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

create policy "mi carrito"    on carrito   for all to authenticated using (usuario_id = auth.uid()) with check (usuario_id = auth.uid());
create policy "mis favoritos" on favoritos for all to authenticated using (usuario_id = auth.uid()) with check (usuario_id = auth.uid());
create policy "reportar"      on reportes  for insert to authenticated with check (usuario_id = auth.uid());
create policy "ver mis reportes" on reportes for select to authenticated using (usuario_id = auth.uid());

-- Pedidos: el comprador y el vendedor ven los suyos. Nadie crea un pedido sobre un producto propio
-- (lo vuelve a revisar el trigger de pedido_items). El pago real lo crea la Edge Function crear-pago.
create policy "leer mis pedidos" on pedidos for select to authenticated
  using (comprador_id = auth.uid() or vendedor_id = auth.uid());
create policy "crear mi pedido" on pedidos for insert to authenticated
  with check (comprador_id = auth.uid() and vendedor_id <> auth.uid());
create policy "actualizar mi pedido" on pedidos for update to authenticated
  using (comprador_id = auth.uid() or vendedor_id = auth.uid());

create policy "leer items de mis pedidos" on pedido_items for select to authenticated
  using (pedido_id in (select id from pedidos where comprador_id = auth.uid() or vendedor_id = auth.uid()));
create policy "agregar items a mi pedido" on pedido_items for insert to authenticated
  with check (pedido_id in (select id from pedidos where comprador_id = auth.uid()));

-- Calificaciones: se leen en el perfil público; solo el comprador califica, una vez, con el pedido liberado.
create policy "leer calificaciones" on calificaciones for select to authenticated using (true);
create policy "calificar mi pedido" on calificaciones for insert to authenticated
  with check (pedido_id in (select id from pedidos where comprador_id = auth.uid() and estado = 'liberado'));

-- ---------------------------------------------------------------- Storage: bucket "fotos" (lectura pública, escritura de la carpeta propia)
insert into storage.buckets (id, name, public) values ('fotos', 'fotos', true) on conflict do nothing;
create policy "subir mis fotos" on storage.objects for insert to authenticated
  with check (bucket_id = 'fotos' and (storage.foldername(name))[1] = auth.uid()::text);
create policy "borrar mis fotos" on storage.objects for delete to authenticated
  using (bucket_id = 'fotos' and (storage.foldername(name))[1] = auth.uid()::text);
