-- Reco · datos iniciales. Correr después de schema.sql.

-- azure_tenant_id y google_hd quedan en null hasta saber con qué proveedor son las cuentas de la BUAP
-- (pendiente por definir). Mientras estén en null, verificar_estudiante() falla cerrado.
-- Microsoft:  update universidades set azure_tenant_id = '<tenant-id-de-la-buap>' where dominio = 'alumno.buap.mx';
-- Google:     update universidades set google_hd = 'alumno.buap.mx' where dominio = 'alumno.buap.mx';
-- Confirmar contra una fila real de auth.identities que 'iss' y 'hd' vengan en las rutas usadas en schema.sql.
insert into universidades (nombre, dominio) values ('BUAP', 'alumno.buap.mx');

insert into puntos_entrega (universidad_id, nombre, descripcion)
select u.id, p.nombre, p.descripcion
from universidades u,
  (values
    ('Biblioteca Central',      'Ciudad Universitaria · planta baja'),
    ('Cafetería FCC',           'Edificio CCO1'),
    ('Explanada de Ingeniería', 'Ciudad Universitaria · frente a la Facultad'),
    ('Comedor universitario',   'Ciudad Universitaria · entrada principal')
  ) as p(nombre, descripcion)
where u.dominio = 'alumno.buap.mx';

-- Catálogo: 18 modelos con precio de referencia (misma lista que CatalogoModelos.kt).
insert into modelos (categoria, marca, modelo, anio, precio_referencia, almacenamientos, rams, colores, procesador, pantalla) values
  ('laptops',  'Apple',     'MacBook Air M1',    2020, 10500, '{"256 GB","512 GB"}', '{"8 GB","16 GB"}', '{"Gris espacial","Plata","Oro"}', 'Apple M1', '13.3"'),
  ('laptops',  'Lenovo',    'IdeaPad 3 Ryzen 5', 2022,  7500, '{"256 GB","512 GB"}', '{"8 GB","16 GB"}', '{"Gris","Azul"}', 'Ryzen 5 5500U', '15.6"'),
  ('laptops',  'HP',        'Pavilion 14',       2021, 10500, '{"512 GB"}', '{"8 GB","16 GB"}', '{"Plata","Negro"}', 'Core i5-1135G7', '14"'),
  ('celulares','Apple',     'iPhone 12',         2020, 10000, '{"64 GB","128 GB","256 GB"}', '{"4 GB"}', '{"Azul","Negro","Blanco","Rojo"}', 'A14 Bionic', '6.1"'),
  ('celulares','Samsung',   'Galaxy A54',        2023,  6500, '{"128 GB","256 GB"}', '{"6 GB","8 GB"}', '{"Verde lima","Negro","Blanco"}', 'Exynos 1380', '6.4"'),
  ('celulares','Xiaomi',    'Redmi Note 12',     2023,  4000, '{"128 GB"}', '{"4 GB","6 GB"}', '{"Gris","Azul"}', 'Snapdragon 685', '6.67"'),
  ('tablets',  'Apple',     'iPad 9.ª gen',      2021,  6500, '{"64 GB","256 GB"}', '{"3 GB"}', '{"Gris espacial","Plata"}', 'A13 Bionic', '10.2"'),
  ('tablets',  'Samsung',   'Galaxy Tab A8',     2022,  4000, '{"32 GB","64 GB"}', '{"3 GB","4 GB"}', '{"Gris","Plata"}', 'Unisoc T618', '10.5"'),
  ('tablets',  'Lenovo',    'Tab M10',           2021,  2500, '{"32 GB","64 GB"}', '{"3 GB"}', '{"Gris"}', 'Helio P22T', '10.1"'),
  ('audio',    'Apple',     'AirPods Pro',       2019,  3500, '{}', '{}', '{"Blanco"}', 'In-ear con cancelación de ruido', null),
  ('audio',    'Sony',      'WH-CH520',          2023,  1200, '{}', '{}', '{"Negro","Azul","Blanco"}', 'On-ear inalámbricos', null),
  ('audio',    'JBL',       'Tune 510BT',        2021,   900, '{}', '{}', '{"Negro","Azul"}', 'On-ear inalámbricos', null),
  ('gaming',   'Nintendo',  'Switch V2',         2019,  5500, '{"32 GB"}', '{}', '{"Neón","Gris"}', 'Consola híbrida', '6.2"'),
  ('gaming',   'Sony',      'PlayStation 4 Slim',2018,  4500, '{"500 GB","1 TB"}', '{}', '{"Negro"}', 'Consola de sobremesa', null),
  ('gaming',   'Microsoft', 'Xbox Series S',     2021,  6000, '{"512 GB"}', '{}', '{"Blanco"}', 'Consola de sobremesa', null),
  ('calculadoras', 'Casio', 'fx-991EX ClassWiz', 2020,   600, '{}', '{}', '{"Negro"}', 'Científica', 'Alta resolución'),
  ('calculadoras', 'Texas Instruments', 'TI-84 Plus', 2018, 1800, '{}', '{}', '{"Azul","Negro"}', 'Graficadora', '128 × 64 px'),
  ('calculadoras', 'HP',    'Prime G2',          2018,  3000, '{}', '{}', '{"Negro"}', 'Graficadora', '320 × 240 px');

-- IMEI y series reportados de prueba (los dos IMEI son válidos con Luhn, para probar el rechazo).
insert into imei_bloqueados (imei) values ('356918102345611'), ('867501049283749'), ('BLOQUEADO001');

-- Los productos de prueba se crean desde la app (modo vendedor) y se aprueban con:
--   update productos set estado = 'publicado', certificado = certificar where id = '<id>';
-- Para rechazar:
--   update productos set estado = 'rechazado', motivo_rechazo = 'Fotos borrosas' where id = '<id>';
