-- Endurece las fotos de los anuncios (revisión de seguridad).

-- 1) Storage: la lectura por URL pública no necesita política; la de "todos" dejaba LISTAR todo el bucket.
--    Solo se conserva lo que el reemplazo (upsert) necesita: leer los objetos de la carpeta propia.
drop policy "leer fotos publicas" on storage.objects;
create policy "leer mis fotos" on storage.objects for select to authenticated
  using (bucket_id = 'fotos' and (storage.foldername(name))[1] = auth.uid()::text);

-- Dentro de una función security definer, current_user es el dueño de la función (no "authenticated"), así que
-- es_cliente() no sirve ahí: se mira el rol del JWT. El moderador (panel de Supabase) y el servicio no entran.
create function es_cliente_jwt() returns boolean language sql stable as $$ select coalesce(auth.role(), '') = 'authenticated' $$;

-- 2) Una foto de un anuncio solo puede apuntar a la carpeta de Storage de su vendedor (no a URLs ajenas ni externas).
create function validar_url_foto() returns trigger language plpgsql security definer set search_path = public as $$
declare dueno uuid;
begin
  if es_cliente_jwt() then
    select vendedor_id into dueno from productos where id = new.producto_id;
    if dueno is null or position('/storage/v1/object/public/fotos/' || dueno::text || '/' in new.url) = 0 then
      raise exception 'La foto debe estar en tu carpeta de Storage';
    end if;
  end if;
  return new;
end $$;
create trigger fotos_validar_url before insert or update of url, producto_id on fotos
  for each row execute function validar_url_foto();

-- 3) Cambiar las fotos de un anuncio ya revisado lo manda otra vez a revisión (igual que cambiar su precio o su IMEI).
--    Los borradores, los anuncios en revisión y los rechazados no cambian de estado.
create function fotos_vuelven_a_revision() returns trigger language plpgsql security definer set search_path = public as $$
begin
  if es_cliente_jwt() then
    update productos set estado = 'en_revision'
      where id = coalesce(new.producto_id, old.producto_id) and estado in ('publicado', 'pausado', 'reservado');
  end if;
  return null;
end $$;
create trigger fotos_revision after insert or update or delete on fotos
  for each row execute function fotos_vuelven_a_revision();
