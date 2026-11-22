#!/usr/bin/env python3
"""Datos de prueba de Reco con DummyJSON.

Baja ~25 productos (laptops, smartphones, tablets, mobile-accessories) de https://dummyjson.com,
descarga sus fotos, crea el bucket público "fotos" en Supabase Storage, las sube y genera
supabase/seed.sql (catálogos base + 4 vendedores ficticios de la BUAP + anuncios "publicado").

Uso (desde la raíz del repo; lee SUPABASE_URL y SUPABASE_SERVICE_ROLE_KEY de local.properties):
    python3 scripts/seed_dummyjson.py            # sube fotos y genera supabase/seed.sql
    python3 scripts/seed_dummyjson.py --sin-subir  # solo genera el SQL (las fotos deben existir ya)

Es idempotente: los ids se derivan con uuid5, las fotos se suben con upsert y el SQL usa
"on conflict do nothing". El SQL se aplica con: supabase db push --include-seed
"""
import hashlib
import json
import random
import sys
import urllib.error
import urllib.request
import uuid
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
USD_A_MXN = 18
FACTOR_USADO = 0.65
POR_CATEGORIA = {  # categoría DummyJSON -> (cuántos, categoría Reco)
    "laptops": (5, "laptops"),
    "smartphones": (10, "celulares"),
    "tablets": (3, "tablets"),
    "mobile-accessories": (7, "accesorios"),
}
ESPACIO = uuid.UUID("5ec0a11c-0000-4000-8000-000000000001")  # espacio de nombres para uuid5

VENDEDORES = [
    # nombre, apodo, carrera, semestre, punto, acerca_de, donde, horario, tarjeta, reputación, ventas
    ("Valeria Mendoza Ortiz", "Vale Tech", "Ing. en Ciencias de la Computación", 7, "Biblioteca Central",
     "Vendo mis equipos cuando me actualizo. Todo probado y con factura.", "En la Biblioteca Central, planta baja",
     "Lun a Vie, 10:00 a 16:00", False, 4.8, 12),
    ("Diego Hernández Cruz", "DiegoGadgets", "Ing. en Tecnologías de la Información", 6, "Cafetería FCC",
     "Estudiante de TI. Si necesitas algo, pregúntame antes de comprar.", "En la Cafetería FCC (CCO1)",
     "Mar y Jue, 12:00 a 18:00", True, 4.5, 8),
    ("Ximena Rojas Salazar", "Xime", "Ing. Mecatrónica", 8, "Explanada de Ingeniería",
     "Cuido mis cosas como nuevas. Entrego rápido.", "En la Explanada de Ingeniería",
     "Lun, Mié y Vie, 11:00 a 15:00", False, 5.0, 5),
    ("Andrés Pérez Luna", "Andy_Retro", "Lic. en Diseño Digital", 5, "Comedor universitario",
     "Coleccionista de cacharros. Precios negociables en persona.", "En la entrada del Comedor universitario",
     "Lun a Jue, 13:00 a 17:00", True, 4.2, 3),
]
COLORES = ["Negro", "Plata", "Gris espacial", "Azul", "Blanco", "Dorado"]
ALMACENAMIENTOS = {"celulares": ["64 GB", "128 GB", "256 GB"], "tablets": ["64 GB", "128 GB", "256 GB"],
                   "laptops": ["256 GB", "512 GB", "1 TB"]}
RAMS = {"celulares": ["4 GB", "6 GB", "8 GB"], "tablets": ["3 GB", "4 GB", "6 GB"], "laptops": ["8 GB", "16 GB"]}
ACCESORIOS = ["Cargador", "Cable USB", "Caja original", "Funda", "Manual"]


def leer_propiedades():
    props = {}
    for linea in (RAIZ / "local.properties").read_text().splitlines():
        if "=" in linea and not linea.lstrip().startswith("#"):
            k, v = linea.split("=", 1)
            props[k.strip()] = v.strip()
    return props


def http(url, data=None, headers=None, method=None):
    req = urllib.request.Request(url, data=data, headers=headers or {}, method=method)
    req.add_header("User-Agent", "reco-seed/1.0")
    with urllib.request.urlopen(req, timeout=60) as r:
        return r.status, r.headers.get("Content-Type", ""), r.read()


def luhn_imei(base14):
    total = 0
    for i, ch in enumerate(reversed(base14)):
        d = int(ch)
        if i % 2 == 0:
            d *= 2
            d = d - 9 if d > 9 else d
        total += d
    return base14 + str((10 - total % 10) % 10)


def sql(v):
    if v is None:
        return "null"
    if isinstance(v, bool):
        return "true" if v else "false"
    if isinstance(v, (int, float)):
        return str(v)
    if isinstance(v, list):
        return "'{" + ",".join('"' + x.replace('"', '\\"').replace("'", "''") + '"' for x in v) + "}'"
    return "'" + str(v).replace("'", "''") + "'"


def precio_mxn(usd):
    return max(100, round(usd * USD_A_MXN * FACTOR_USADO / 100) * 100)


def main():
    subir = "--sin-subir" not in sys.argv
    props = leer_propiedades()
    base = props["SUPABASE_URL"].rstrip("/")
    clave = props["SUPABASE_SERVICE_ROLE_KEY"]
    auth = {"Authorization": f"Bearer {clave}", "apikey": clave}
    rnd = random.Random(2026)

    # 1) Productos de DummyJSON
    elegidos = []
    for cat_dj, (n, cat_reco) in POR_CATEGORIA.items():
        _, _, cuerpo = http(f"https://dummyjson.com/products/category/{cat_dj}?limit=100")
        for p in json.loads(cuerpo)["products"][:n]:
            elegidos.append((cat_reco, p))
    print(f"{len(elegidos)} productos de DummyJSON")

    # 2) Bucket público "fotos"
    if subir:
        try:
            http(f"{base}/storage/v1/bucket", json.dumps({"id": "fotos", "name": "fotos", "public": True}).encode(),
                 {**auth, "Content-Type": "application/json"}, "POST")
            print("bucket 'fotos' creado")
        except urllib.error.HTTPError as e:
            print("bucket 'fotos' ya existía" if e.code in (400, 409) else f"bucket: error {e.code}")
            if e.code not in (400, 409):
                raise

    # 3) Vendedores, modelos, productos y fotos
    vend_ids = [uuid.uuid5(ESPACIO, "vendedor-" + v[1]) for v in VENDEDORES]
    filas_modelos, filas_prod, filas_fotos = [], [], []
    fotos_subidas = 0
    for i, (cat, p) in enumerate(elegidos):
        pid = uuid.uuid5(ESPACIO, f"producto-{p['id']}-{cat}")
        mid = uuid.uuid5(ESPACIO, f"modelo-{p['id']}-{cat}")
        vend = vend_ids[i % len(vend_ids)]
        marca = p.get("brand") or p["title"].split()[0]
        referencia = max(200, round(p["price"] * USD_A_MXN / 100) * 100)  # precio nuevo en MXN
        precio = min(precio_mxn(p["price"]), referencia)
        sku = ALMACENAMIENTOS.get(cat)
        grado = "ABC"[i % 3]
        celular = cat in ("celulares", "tablets")
        filas_modelos.append((mid, cat, marca, p["title"], 2019 + i % 5, referencia, sku or [], RAMS.get(cat, [])))
        filas_prod.append(dict(
            id=pid, vendedor=vend, modelo=mid, alm=rnd.choice(sku) if sku else None,
            ram=rnd.choice(RAMS[cat]) if cat in RAMS else None, color=rnd.choice(COLORES), grado=grado,
            bateria=rnd.randint(75, 98) if cat != "accesorios" else None,
            imei=luhn_imei(f"35{rnd.randrange(10**12):012d}") if celular else f"SN{int(hashlib.md5(str(pid).encode()).hexdigest(), 16) % 10**10:010d}",
            accesorios=rnd.sample(ACCESORIOS, rnd.randint(1, 3)),
            desc=p["description"], precio=precio, certificar=(i % 4 == 0)))
        urls = []
        for n, url in enumerate(list(dict.fromkeys((p.get("images") or []) + [p["thumbnail"]]))[:4]):
            ext = url.rsplit(".", 1)[-1].split("?")[0].lower()
            ext = ext if ext in ("png", "jpg", "jpeg", "webp") else "jpg"
            ruta = f"{vend}/{pid}/{n}.{ext}"
            if subir:
                _, tipo, datos = http(url)
                tipo = tipo.split(";")[0] or f"image/{ext}"
                http(f"{base}/storage/v1/object/fotos/{ruta}", datos,
                     {**auth, "Content-Type": tipo, "x-upsert": "true"}, "POST")
                fotos_subidas += 1
            urls.append(f"{base}/storage/v1/object/public/fotos/{ruta}")
        for orden, u in enumerate(urls):
            filas_fotos.append((uuid.uuid5(ESPACIO, f"foto-{pid}-{orden}"), pid, u, orden))
        print(f"  [{i + 1:2}/{len(elegidos)}] {p['title'][:40]:40} ${precio:>6} MXN  {len(urls)} fotos")
    print(f"{fotos_subidas} fotos subidas a Storage")

    # 4) supabase/seed.sql
    out = [(RAIZ / "scripts" / "seed_base.sql").read_text().rstrip(), "",
           "-- =============================================================== Generado por scripts/seed_dummyjson.py",
           "-- Productos de DummyJSON (precio = USD × 18 × 0.65 redondeado a centenas), 4 vendedores ficticios de la BUAP.",
           "-- Los vendedores se crean en auth.users solo para cumplir la llave foránea (nadie inicia sesión con ellos).", ""]
    for (nombre, apodo, carrera, sem, punto, acerca, donde, horario, tarjeta, rep, ventas), vid in zip(VENDEDORES, vend_ids):
        correo = apodo.lower().replace(" ", ".").replace("_", ".") + "@alumno.buap.mx"
        out.append(
            "insert into auth.users (instance_id, id, aud, role, email, encrypted_password, email_confirmed_at, "
            "raw_app_meta_data, raw_user_meta_data, created_at, updated_at, confirmation_token, recovery_token, "
            "email_change, email_change_token_new)\n"
            f"values ('00000000-0000-0000-0000-000000000000', '{vid}', 'authenticated', 'authenticated', {sql(correo)}, '', now(), "
            "'{\"provider\":\"email\",\"providers\":[\"email\"]}', '{}', now(), now(), '', '', '', '')\n"
            "on conflict (id) do nothing;")
        out.append(
            "insert into perfiles (id, nombre, apodo, acerca_de, carrera, semestre, universidad_id, correo_institucional, verificado, "
            "es_vendedor, ultimo_modo, punto_preferido_id, donde_entregas, horario, acepta_tarjeta, reputacion, ventas_completadas)\n"
            f"select '{vid}', {sql(nombre)}, {sql(apodo)}, {sql(acerca)}, {sql(carrera)}, {sem}, u.id, {sql(correo)}, true, true, 'vendedor', "
            f"(select id from puntos_entrega where nombre = {sql(punto)} and universidad_id = u.id), {sql(donde)}, {sql(horario)}, "
            f"{sql(tarjeta)}, {rep}, {ventas}\nfrom universidades u where u.dominio = 'alumno.buap.mx'\non conflict (id) do nothing;")
    out.append("")
    for mid, cat, marca, titulo, anio, ref, alm, ram in filas_modelos:
        out.append(
            "insert into modelos (id, categoria, marca, modelo, anio, precio_referencia, almacenamientos, rams, colores) values "
            f"('{mid}', {sql(cat)}, {sql(marca)}, {sql(titulo)}, {anio}, {ref}, {sql(alm)}, {sql(ram)}, '{{}}') on conflict do nothing;")
    out += ["", "-- Un vendedor de prueba tiene más de 3 anuncios: el límite aplica a la app, no a la carga inicial.",
            "alter table productos disable trigger productos_limite;"]
    for f in filas_prod:
        out.append(
            "insert into productos (id, vendedor_id, modelo_id, almacenamiento, ram, color, grado, salud_bateria, imei, accesorios, "
            "descripcion, precio, estado, certificar, certificado) values "
            f"('{f['id']}', '{f['vendedor']}', '{f['modelo']}', {sql(f['alm'])}, {sql(f['ram'])}, {sql(f['color'])}, {sql(f['grado'])}, "
            f"{sql(f['bateria'])}, {sql(f['imei'])}, {sql(f['accesorios'])}, {sql(f['desc'])}, {f['precio']}, 'publicado', "
            f"{sql(f['certificar'])}, {sql(f['certificar'])}) on conflict (id) do nothing;")
    out.append("alter table productos enable trigger productos_limite;")
    out.append("")
    for fid, pid, url, orden in filas_fotos:
        out.append(f"insert into fotos (id, producto_id, url, orden) values ('{fid}', '{pid}', {sql(url)}, {orden}) on conflict (id) do nothing;")
    (RAIZ / "supabase" / "seed.sql").write_text("\n".join(out) + "\n")
    print(f"supabase/seed.sql: {len(filas_prod)} productos, {len(filas_fotos)} fotos")


if __name__ == "__main__":
    main()
