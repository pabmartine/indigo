# Revisión de detección e importación de EPUB

Fecha: 2026-09-30.

## Hallazgos y cambios

| Etapa | Problema encontrado | Cambio |
| --- | --- | --- |
| Detectar | `Files.walk` obtiene atributos de cada entrada; no abre los EPUB, pero no es un contador de nombres de coste constante. La caché empezaba a caducar antes de terminar el recorrido y las peticiones simultáneas podían repetirlo. | Listado nativo mediante `find -P … -type f -iname '*.epub' -print0` donde está disponible, aprovechando los tipos de las entradas del directorio. Alternativa Java portable con `walkFileTree`. Caché compartida, exclusión mutua y caducidad desde el final del recorrido. |
| Aceptar | Segundo recorrido completo; `Files.isRegularFile` volvía a consultar atributos. | Reutilización de la selección detectada durante hasta 10 minutos. Se consume una vez y se invalida al acabar el lote. Los archivos incorporados después se detectan en la siguiente operación. |
| Ocho workers | Solo la lectura inicial se hacía en paralelo. `persistenceLock` abarcaba guardado, imágenes, commit, movimiento y autores. Otros métodos tenían además un monitor global. | Bloqueos acotados por identidad de título y por libro pendiente. Los libros independientes pueden completar las fases caras en paralelo; las versiones del mismo libro siguen ordenadas hasta instalar el archivo. Los contadores de autores mantienen su sección protegida, incluyendo el commit. |
| Duplicados | `findByTitleIgnoreCase` era una consulta derivada con expresión regular insensible a mayúsculas, sin un índice adecuado para esa búsqueda. Se ejecutaba antes de guardar cada título nuevo. | Igualdad con collation `en`, fuerza 2, e índice `import_title_ci` con la misma collation. Respeta diferencias de acentos. El test de integración comprueba `IXSCAN` y un documento examinado. |
| Movimiento y estado durable | Búsquedas por ruta sin índice en `pendingImports` y `files`; lectura duplicada del estado de las tres tareas. | Índices por `source, createdAt` y por `path`; una sola lectura del estado final para construir la respuesta. |
| Finalización | Un viaje individual a MongoDB por cada marca de categorías completadas. | Escrituras por lotes de hasta 500: para 150.000 marcas, 300 ejecuciones bulk en vez de 150.000 llamadas individuales. La reconstrucción del catálogo continúa haciéndose una vez por lote. |
| Duplicados descartados | Quedaban directorios temporales `.import-*` vacíos, que engordaban futuros recorridos. | Eliminación del directorio aislado cuando queda vacío. |
| Interfaz | Se podían lanzar peticiones repetidas durante la detección o el arranque; un fallo de lectura se confundía con cero libros. | Botón con estado de carga y protección frente a clics repetidos. Los errores de detección/arranque se muestran como errores. |
| Diagnóstico | No se registraba cuánto duraba cada fase completada. | Tiempo de la fase previa, tiempo total por archivo y resumen del lote. Se conservan los avisos de workers bloqueados. |

La limitación de los índices con expresiones regulares insensibles a mayúsculas está documentada por [MongoDB](https://www.mongodb.com/docs/v8.2/core/index-case-insensitive/). La corrección de la consulta y su plan también se verifican contra MongoDB real en las pruebas.

## Mediciones y validación

Medición sintética local con 150.000 archivos vacíos en una carpeta temporal, Java 25, una ejecución con cachés del sistema sin limpiar:

| Operación | Antes | Después |
| --- | ---: | ---: |
| Detectar 150.000 rutas | 0,653 s | 0,193 s |
| Preparar la selección al aceptar | 1,143 s, segundo recorrido | 4,369 ms, copia de la lista |

Esta medición compara enumeración de nombres. No mide lectura de EPUB, imágenes, red, almacenamiento remoto ni rendimiento de importación real; no predice el tiempo de la carpeta del servidor. En un sistema de archivos que no proporcione tipos de entrada, `find` también puede necesitar consultas de atributos. Sin `find`, la alternativa Java conserva la reutilización de la selección y la semántica, pero consulta atributos por entrada.

Validación completada:

- 91 pruebas seleccionadas, sin fallos: escáner, detección, arranque, concurrencia, pendientes, política de versiones, rutas, XML, guardado, movimientos, autores e integración con MongoDB temporal.
- La prueba de concurrencia exige que ocho workers entren simultáneamente en guardado y espera a que terminen las tareas relacionadas antes de finalizar.
- La prueba de identidad conserva la exclusión entre versiones hasta finalizar el movimiento; la integración mantiene una sola entidad con la versión más alta y los contadores correctos.
- Coincidencia entre escáner nativo y Java con subdirectorios, mayúsculas, nombres con saltos de línea y comillas; exclusión de enlaces simbólicos y directorios con sufijo `.epub`.
- Consulta por título comprobada con `explain`: índice `import_title_ci`, etapa `IXSCAN`, un documento examinado; distingue `Árbol` de `Arbol`.
- Compilación TypeScript y build Angular de producción correctos. Advertencia existente de dependencia CommonJS de `epubjs`.

## Aplicación y medición en el servidor

Los cambios requieren desplegar el backend y el frontend compilados. Los índices nuevos se crean al arrancar el backend y esa primera creación puede añadir tiempo de inicio según el tamaño de la base de datos. No se ha modificado ni reiniciado la importación de producción: en el Docker accesible desde este entorno no estaba ejecutándose Indigo.

La configuración normal ya fija `book.library.import-workers` a 8 mediante `BOOK_LIBRARY_IMPORT_WORKERS`. `events.workers` controla otro ejecutor; aumentar este último no eliminaba el bloqueo global del importador.

Para atribuir con precisión los 5–10 minutos de detección y los 181 libros en 3 horas hace falta medir la instancia y el almacenamiento afectados. El código demuestra los cuellos de botella anteriores, pero no permite atribuir por sí solo todo ese tiempo al disco o a una consulta concreta. Los logs permiten contrastar:

- `Detected … EPUB files in … ms`: coste real de enumeración.
- `Starting EPUB import with … parallel worker(s)`: número efectivo de workers.
- `EPUB phase … previousPhase=… previousMs=…`: lectura OPF, espera por identidad, imágenes y guardado.
- `EPUB still processing … state=… stack=…`: estado de los hilos si ningún archivo termina en 30 segundos.
- `EPUB finished … totalMs=…` y `EPUB batch finished …`: duración por archivo y por lote, incluyendo finalización del catálogo.

Los mensajes de fases y por archivo requieren DEBUG para el importador. La detección y el resumen del lote están en INFO. El polling de progreso consulta contadores en memoria y no vuelve a contar la carpeta. La ruta paralela lee el OPF y las imágenes del ZIP local; no llama a proveedores externos de metadatos.
