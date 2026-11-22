-- Seguridad: el IMEI/serie completo solo lo lee su dueño; los demás ven los últimos 4 dígitos.
alter table productos add column imei_ultimos4 text generated always as (right(imei, 4)) stored;

revoke select on productos from anon, authenticated;
grant select (id, vendedor_id, modelo_id, almacenamiento, ram, color, grado, salud_bateria, imei_ultimos4, accesorios,
              descripcion, precio, carreras, estado, certificar, certificado, revision_manual, motivo_rechazo, creado)
  on productos to anon, authenticated;

-- El vendedor recupera su propio IMEI completo (para corregir o continuar un borrador).
create function obtener_mi_imei(p_producto uuid) returns text
language sql stable security definer set search_path = public as $$
  select imei from productos where id = p_producto and vendedor_id = auth.uid()
$$;
revoke execute on function obtener_mi_imei(uuid) from public, anon;
grant execute on function obtener_mi_imei(uuid) to authenticated;

-- Los vendedores ficticios de la carga inicial no usan correos reales de la BUAP y no pueden iniciar sesión.
update auth.users set email = replace(email, '@alumno.buap.mx', '@example.invalid'), banned_until = 'infinity'
 where encrypted_password = '' and email like '%@alumno.buap.mx';
update perfiles set correo_institucional = replace(correo_institucional, '@alumno.buap.mx', '@example.invalid')
 where correo_institucional like '%@example.invalid' or id in (select id from auth.users where email like '%@example.invalid');
