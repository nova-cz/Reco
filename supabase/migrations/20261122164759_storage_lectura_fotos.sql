-- Storage (bucket "fotos"): lectura pública explícita. El bucket ya es público para las URL, pero un upsert
-- (subir reemplazando) necesita además poder leer el objeto; sin esta política Storage responde "Unauthorized".
create policy "leer fotos publicas" on storage.objects for select to anon, authenticated
  using (bucket_id = 'fotos');
