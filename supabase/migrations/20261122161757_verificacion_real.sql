-- Verificación real de estudiante: Microsoft (Entra ID) o código OTP al correo institucional.
-- Ya no se confía en el correo que manda la app.

-- Dominios institucionales permitidos (agregar más universidades = insertar filas).
create table dominios_permitidos (
  dominio         text primary key check (dominio = lower(dominio)),
  universidad_id  uuid not null references universidades(id),
  -- Si se llena, la identidad Microsoft debe venir de ese tenant (el correo de Microsoft solo no basta:
  -- cualquier organización puede escribir el mismo correo). Se carga cuando se conozca el tenant de la universidad.
  azure_tenant_id uuid
);
alter table dominios_permitidos enable row level security;   -- sin políticas: solo la leen las funciones del servidor
revoke all on dominios_permitidos from anon, authenticated;

insert into dominios_permitidos (dominio, universidad_id)
select lower(dominio), id from universidades;

drop function verificar_estudiante(text);

create function verificar_estudiante() returns void
language plpgsql security definer set search_path = public, auth as $$
declare uid uuid := auth.uid(); correo text; uni uuid;
begin
  if uid is null then raise exception 'Inicia sesión primero'; end if;

  -- 1) Microsoft: identidad azure ligada a la cuenta, con correo de un dominio permitido (y del tenant, si se conoce).
  select lower(coalesce(i.identity_data ->> 'email', i.identity_data ->> 'preferred_username')), d.universidad_id
    into correo, uni
  from auth.identities i
  join dominios_permitidos d
    on split_part(lower(coalesce(i.identity_data ->> 'email', i.identity_data ->> 'preferred_username')), '@', 2) = d.dominio
  where i.user_id = uid and i.provider = 'azure'
    and (d.azure_tenant_id is null
         or i.identity_data ->> 'iss' like 'https://login.microsoftonline.com/' || d.azure_tenant_id || '/%')
  limit 1;

  -- 2) Código OTP: la sesión se abrió con un código enviado al correo de la cuenta (amr = otp), que es institucional.
  if uni is null
     and exists (select 1 from jsonb_array_elements(coalesce(auth.jwt() -> 'amr', '[]'::jsonb)) a
                 where a ->> 'method' in ('otp', 'magiclink')) then
    select lower(u.email), d.universidad_id into correo, uni
    from auth.users u
    join dominios_permitidos d on split_part(lower(u.email), '@', 2) = d.dominio
    where u.id = uid and u.email_confirmed_at is not null;
  end if;

  if uni is null then
    raise exception 'No pudimos comprobar tu cuenta institucional';
  end if;
  update perfiles set verificado = true, universidad_id = uni, correo_institucional = correo where id = uid;
  if not found then raise exception 'No existe el perfil'; end if;
end $$;

revoke execute on function verificar_estudiante() from public, anon;
grant execute on function verificar_estudiante() to authenticated;
