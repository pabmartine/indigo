# Búsqueda parcial por título y autor

Revisión: 2026-09-30.

## Problemas encontrados

Las búsquedas avanzada y global terminaban en `CustomBookRepositoryImpl.buildSearchQuery`. La avanzada aplicaba expresiones regulares insensibles a mayúsculas a `title` y `authors`, sin restringir primero los candidatos. La barra superior enviaba el texto en el campo histórico `path`, que se buscaba en la ruta física del archivo. Esto también omitía autores que no apareciesen en el nombre de la carpeta.

El índice de igualdad de títulos incorporado para la importación no resuelve `contiene`. MongoDB documenta que [las expresiones regulares insensibles a mayúsculas no aprovechan índices con collation](https://www.mongodb.com/docs/manual/core/index-case-insensitive/). Cambiar a una búsqueda por palabras o únicamente por prefijo habría cambiado el comportamiento solicitado.

En el frontend, al introducir otro texto o cambiar la ordenación, la petición anterior seguía activa y podía incorporar sus libros al resultado nuevo. Borrar la barra o pulsar Enter tampoco cancelaba la búsqueda programada con el debounce.

## Cambios

`BookSearchIndex` genera campos internos derivados de título y autores, normalizados a minúsculas y sin acentos, y fragmentos de uno, dos y tres caracteres Unicode. Un índice multikey `book_substring_idx` sobre `search.grams, _id` selecciona candidatos. Para textos de más de tres caracteres se comprueba además la coincidencia literal completa: compartir fragmentos desordenados o repartidos entre distintos autores no basta para aparecer en el resultado.

- Global: el texto debe estar contenido en el título **o en cualquiera de los autores**. `path` se mantiene como nombre del parámetro por compatibilidad con los enlaces existentes, pero ya no significa buscar carpetas.
- Avanzada: título y autor usan `contiene`; si se rellenan ambos, deben cumplirse ambos filtros. Se conservan idiomas, categorías, páginas, fechas, series, ordenación, paginación y total exacto.
- Se admiten búsquedas de uno y dos caracteres, aunque las muy comunes naturalmente producen más candidatos. Los términos de hasta tres caracteres se resuelven directamente mediante el fragmento indexado.
- Los caracteres como `.*`, `+` o `[` se interpretan literalmente. El usuario no necesita escribir comodines para obtener coincidencias parciales.
- `garcia` encuentra `García`; `arcia mar` encuentra `García Márquez`; `vierno` encuentra `El invierno`.
- Las consultas parciales indican el índice apropiado para impedir que la ordenación por `_id` lleve al planificador a recorrer todo el catálogo buscando una coincidencia rara.
- Los campos internos no se incluyen en las respuestas del catálogo.

Las altas y modificaciones de libros actualizan los campos derivados en la misma escritura mediante `BeforeSaveCallback`, un [callback síncrono de Spring Data](https://docs.spring.io/spring-data/mongodb/reference/mongodb/lifecycle-events.html). No se añade un servicio externo de búsqueda ni una caché de resultados que pueda quedar desactualizada.

En el frontend se cancelan las peticiones de la búsqueda anterior, se impide solicitar varias veces una página mientras está cargando y se cancela el debounce al vaciar la barra o buscar con Enter.

## Libros existentes y despliegue

En el primer arranque se crean los índices y se rellenan los campos de los libros anteriores en lotes de 500. Solo se leen `_id`, título y autores; se actualiza únicamente el campo derivado `search`. No se leen EPUB ni portadas para este proceso. La versión se guarda junto con los campos derivados, de forma que un reinicio continúa con los libros pendientes.

La actualización compara también el título y autores leídos, para no sobrescribir el índice generado por un guardado concurrente. La aplicación registra `Book substring index: … existing books updated` y finalmente `Book substring index ready`.

Mientras se prepara el índice, las búsquedas parciales devuelven temporalmente HTTP 503 con un mensaje de preparación; no devuelven resultados incompletos. Los siguientes arranques solo procesan libros sin la versión vigente. Este trabajo inicial y el espacio adicional para campos e índices son el coste de evitar el recorrido del catálogo en cada búsqueda.

Se han preparado cambios y builds locales. No se ha desplegado ni ejecutado la migración en la biblioteca del usuario.

## Validación

20 pruebas de backend seleccionadas, sin errores: repositorio, catálogo, búsquedas, migración, estado durante el arranque e importación paralela. Incluyen:

- Coincidencias por fragmentos intermedios, acentos y mayúsculas, autores secundarios y caracteres literales de expresiones regulares.
- Rechazo de falsos positivos por fragmentos desordenados o por fragmentos presentes en autores diferentes.
- Consultas cortas y caracteres fuera del plano básico Unicode.
- Combinación de autor, título, idioma, páginas y etiquetas; total correcto y páginas ordenadas.
- Actualización tras editar/eliminar un libro y migración repetible que conserva portada y descripción.
- `explain` con `IXSCAN`, índice `book_substring_idx` y pocos documentos examinados.
- Conservación del comportamiento de importación de versiones y contadores de autores/categorías.

5 pruebas en Chrome Headless correctas: debounce, Enter, borrado, cancelación de resultados anteriores y cambio de ordenación. Compilación TypeScript y build Angular de producción correctos, con la advertencia CommonJS existente de `epubjs`.

Medición de la última ejecución contra un MongoDB temporal 4.0.10 con 30.000 documentos sintéticos, portadas simuladas de 1 KB y diez coincidencias situadas al principio de la colección; búsqueda de hasta 20 resultados ordenados por `_id` descendente:

| Búsqueda | Regex anterior, ms | Índice de fragmentos, ms | Documentos examinados antes / después |
| --- | ---: | ---: | ---: |
| Título: `extraordinaria` | 57 | 3 | 30.000 / 10 |
| Autor: `Márquez` | 60 | 1 | 30.000 / 10 |
| Global: `Márquez` | 99 | 2 | 30.000 / 10 |

Se compara una consulta regex equivalente por título/autor con la consulta nueva para conservar los mismos resultados; para la global, la referencia usa OR sobre esos dos campos, no la antigua búsqueda por rutas. Son tiempos de ejecución de MongoDB medidos con `explain`, sin red HTTP, navegador ni carga de portadas. No son una predicción del tiempo del servidor real ni de términos muy frecuentes. La mejora estable que verifica la prueba es reducir los documentos examinados conservando los resultados.

El benchmark es optativo: `BookSubstringSearchIntegrationTest` lo ejecuta cuando se define `-Dsearch.benchmark.size=30000`. Las pruebas funcionales se ejecutan sin esa propiedad.
