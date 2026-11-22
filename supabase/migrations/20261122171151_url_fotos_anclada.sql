-- Fotos de anuncios: validación de URL anclada y fotos que no cambian de anuncio (revisión de seguridad).

-- 1) La URL debe EMPEZAR con el origen de Storage de este proyecto y la carpeta del vendedor, y el resto solo puede
--    llevar letras, números, punto, guion y guion bajo (sin "..", "?", "#", "%" ni barras invertidas).
--    El origen está fijo a propósito: si el proyecto cambia, hay que actualizarlo aquí.
create or replace function validar_url_foto() returns trigger language plpgsql security definer set search_path = public as $$
declare dueno uuid; base text; resto text;
begin
  if es_cliente_jwt() then
    select vendedor_id into dueno from productos where id = new.producto_id;
    if dueno is null then raise exception 'La foto debe estar en tu carpeta de Storage'; end if;
    base := 'https://otkbruduudshhnoagadi.supabase.co/storage/v1/object/public/fotos/' || dueno::text || '/';
    if left(new.url, length(base)) <> base then
      raise exception 'La foto debe estar en tu carpeta de Storage';
    end if;
    resto := substr(new.url, length(base) + 1);
    if resto !~ '^[A-Za-z0-9._-]+(/[A-Za-z0-9._-]+)*$' or position('..' in resto) > 0 then
      raise exception 'La dirección de la foto no es válida';
    end if;
  end if;
  return new;
end $$;

-- 2) Una foto no cambia de anuncio: la app las borra y las vuelve a insertar. Así, cambiar fotos siempre afecta
--    (y manda a revisión) solo al anuncio al que pertenecen.
create function fotos_no_cambian_de_anuncio() returns trigger language plpgsql security definer set search_path = public as $$
begin
  if es_cliente_jwt() and new.producto_id is distinct from old.producto_id then
    raise exception 'Una foto no se puede mover a otro anuncio';
  end if;
  return new;
end $$;
create trigger fotos_fijas before update of producto_id on fotos
  for each row execute function fotos_no_cambian_de_anuncio();
