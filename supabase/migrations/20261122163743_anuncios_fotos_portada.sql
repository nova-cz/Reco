-- Anuncios del vendedor en Supabase: fotos con portada y validación del IMEI bloqueado en el servidor.

-- La tabla de fotos de cada anuncio es `fotos` (ya la usa el catálogo); se le agrega la marca de portada.
alter table fotos add column es_portada boolean not null default false;
update fotos set es_portada = (orden = 0);
create unique index fotos_una_portada on fotos (producto_id) where es_portada;

-- Nombre pedido para el flujo de publicar: anuncio_fotos (anuncio_id, url, orden, es_portada).
-- Es una vista sobre `fotos` con las mismas reglas RLS (security_invoker), no una segunda copia de los datos.
create view anuncio_fotos with (security_invoker = true) as
  select id, producto_id as anuncio_id, url, orden, es_portada from fotos;
grant select on anuncio_fotos to anon, authenticated;

-- Hasta ahora el IMEI bloqueado solo se revisaba en la app; ahora también lo rechaza el servidor.
create function validar_imei_bloqueado() returns trigger language plpgsql security definer set search_path = public as $$
begin
  if new.imei is not null and exists (select 1 from imei_bloqueados where imei = new.imei) then
    raise exception 'IMEI bloqueado';
  end if;
  return new;
end $$;
create trigger productos_imei_bloqueado before insert or update of imei on productos
  for each row execute function validar_imei_bloqueado();

-- Storage (bucket "fotos"): lectura pública (bucket público) y escritura/borrado solo en la carpeta propia
-- <uid>/...; ya existían las políticas de insert y delete. Se agrega update para poder reemplazar (upsert) una foto propia.
create policy "reemplazar mis fotos" on storage.objects for update to authenticated
  using (bucket_id = 'fotos' and (storage.foldername(name))[1] = auth.uid()::text)
  with check (bucket_id = 'fotos' and (storage.foldername(name))[1] = auth.uid()::text);
