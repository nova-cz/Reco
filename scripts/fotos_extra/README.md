# Fotos propias para el catálogo de prueba

Pon aquí tus fotos de **calculadoras** y **audífonos** y vuelve a correr `python3 scripts/seed_dummyjson.py`:
el script las comprime (máx. 1280 px, JPG), las sube a Storage (bucket `fotos`) y crea un anuncio publicado por cada producto en `supabase/seed.sql`.
Después aplica el SQL con `supabase db push --include-seed --remote`. Se puede repetir sin duplicar nada.

## Cómo organizarlas

```
scripts/fotos_extra/
  calculadoras/
    Casio fx-991EX/          <- una carpeta = un producto (de 1 a 8 fotos; la primera es la portada)
      1.jpg
      2.jpg
      datos.json             <- opcional
    TI-84 Plus.jpg           <- una foto suelta = un producto con una sola foto
  audio/
    Sony WH-CH520/
      frente.jpg
      lado.jpg
```

- Las carpetas de primer nivel son la **categoría**: `calculadoras`, `audio`, `laptops`, `celulares`, `tablets`, `gaming` o `accesorios`.
- El **nombre de la carpeta o del archivo** es "Marca Modelo" (la primera palabra se toma como marca).
- Formatos: `.jpg`, `.jpeg`, `.png`, `.webp`. Las fotos se ordenan por nombre de archivo.

## `datos.json` (opcional)

```json
{ "marca": "Casio", "precio": 600, "grado": "B", "descripcion": "Funciona perfecto, con su tapa." }
```

Si no hay `datos.json`: precio de usado por categoría (calculadoras $600, audio $900, otras $1,000), grado A/B/C rotando y una descripción genérica.
Los productos se reparten entre los 4 vendedores ficticios de la BUAP.
