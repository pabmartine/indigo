# Importación y metadatos: comportamiento y validación

## Identidad y versiones

- ISBN equivalente (ISBN-10/ISBN-13) y título normalizado identifican una única ficha. El nombre y los bytes del archivo no definen la identidad.
- Solo una versión entrante positiva, finita y superior reemplaza el EPUB. Las versiones inferiores, iguales o desconocidas se descartan sin alterar la ficha.
- Sin ISBN suficiente se exige coincidencia de título, conjunto de autores e idioma. Las identidades ambiguas y las colisiones de ruta entre ISBN distintos se rechazan conservando el archivo entrante.
- No se descarta un entrante si falta el archivo de la ficha existente: esa importación requiere recuperación, no deduplicación.
- La actualización conserva el identificador, nombre de archivo instalado, relaciones, reseñas, datos personales y metadatos enriquecidos.

## Rendimiento de la importación por lotes

El inicio normal de importaciones usa `ParallelEpubImporter`. `BOOK_LIBRARY_IMPORT_WORKERS` configura los trabajadores (2 por defecto, acotados entre 1 y 8). Solo hay ese número de EPUB preparados o en curso, no una tarea en memoria por cada archivo de la biblioteca.

- La lectura del OPF se realiza en paralelo con `ZipFile`, abriendo una vez el ZIP y leyendo directamente las entradas necesarias. Cada archivo se aísla en un subdirectorio temporal dentro de la entrada para evitar colisiones de `cover.jpg`.
- La escritura se serializa, incluyendo la decisión de identidad/versión, confirmación de la ficha y tareas de movimiento/autores. Las imágenes se leen y procesan solo para libros nuevos: las actualizaciones y duplicados conservan las existentes.
- Las categorías se recalculan **una vez por lote**, no por EPUB. Sus tareas pendientes se confirman después del recálculo; si falla o se reinicia antes, pueden recuperarse con el mecanismo existente, sin sumar contadores dos veces. No se introducen incrementos que dependan de transacciones Mongo no configuradas.
- El coordinador espera la finalización de cada tarea mediante futuros, sin sondear cada 50 ms los contadores globales. Los contadores tienen incrementos sincronizados, y el sondeo del estado no finaliza el lote mientras quedan tareas relacionadas o el recálculo final.
- Las consultas manuales y eventos antiguos de extracción mantienen su ruta compatible. La mejora de rendimiento descrita corresponde al inicio normal de lotes.

Las pruebas comprueban preparación concurrente con escritura serializada, versiones concurrentes sin fichas duplicadas, imágenes diferidas y un solo recálculo de categorías. No se ha medido un factor de aceleración sobre la biblioteca real.

## Recuperación

Las actualizaciones crean una copia temporal y un diario `.import-*.bak.json` en el volumen de la biblioteca antes de sustituir el archivo. Tras un reinicio se consulta la versión persistida: se conserva la actualización confirmada o se restaura el EPUB anterior. Los archivos de recuperación se eliminan al terminar. Esto cubre reinicios del proceso/contenedor; no sustituye una copia de seguridad frente a pérdida del volumen o corrupción del disco.

La primera importación registra sus tareas en Mongo (`pendingImports`, `pendingImportTasks`) junto con la ficha. Al arrancar se reejecutan los consumidores pendientes; autores y categorías no repiten las tareas confirmadas. Un movimiento ya realizado puede confirmarse aunque el archivo entrante ya no exista.

En Ajustes → **Importaciones pendientes**, el botón Actualizar muestra los archivos con tareas sin confirmar (EPUB, autores y categorías). Reintentar ejecuta solo esas tareas, sin reiniciar. La respuesta comprueba las confirmaciones persistidas: si queda alguna tarea, la importación sigue visible con un aviso, no se comunica un éxito ficticio. Los reintentos manuales se serializan hasta terminar las transacciones y se rechazan mientras el proceso de importación indica que está en curso. Se conservan los archivos ante conflictos y se rechazan rutas fuera de las carpetas configuradas o con enlaces simbólicos. La recuperación al arrancar usa el mismo mecanismo.

API de administración: `GET /api/metadata/pending-imports` y `POST /api/metadata/pending-imports/{bookId}/retry`. Ambas requieren `ADMIN`. Las tareas completadas se conservan para mantener la idempotencia; esta funcionalidad no añade limpieza de registros ni reintentos periódicos.

Biblioteca, carpeta de entrada y Mongo deben usar volúmenes persistentes. La recuperación automática de archivos rechaza rutas raíz excesivamente amplias, como `/` o `/tmp`; hay que configurar una carpeta dedicada. Un diario que no pueda recuperarse conserva sus archivos y deja un error en el log.

## Proveedores

- Libros, autores y reseñas pueden ejecutarse simultáneamente desde Ajustes. Libros y autores conservan identificadores, contadores y parada independientes; cambiar entre todos/sin metadatos sustituye únicamente el recorrido de esa entidad. Las reseñas mantienen su cola persistente. `GET /api/metadata/stop?entity=BOOKS|AUTHORS` detiene solo esa entidad; sin parámetro mantiene la parada global de los recorridos no persistentes. Una petición ya enviada puede terminar. Las escrituras de metadatos de libro y reseñas actualizan campos separados, sin sustituir el documento completo.
- Para libros, Open Library consulta primero la versión activa del índice local. Solo usa versiones publicadas, nunca la versión en construcción. Si falta un resultado utilizable, mantiene la consulta HTTP como alternativa.
- Los autores se buscan primero en el catálogo local del [volcado oficial de Open Library](https://openlibrary.org/developers/dumps). El índice guarda nombres/alias normalizados, biografía y disponibilidad de foto solo para los autores presentes en los libros de la biblioteca. Si no hay una coincidencia única, se mantiene el flujo habitual: Wikipedia en el idioma del libro, Wikipedia en inglés como alternativa y búsqueda HTTP de Open Library si faltan biografía o imagen. Las fotos del catálogo y de Open Library se descargan de su servicio de imágenes.
- Las biografías de Open Library se pasan por LibreTranslate, que detecta el idioma y las deja en español si ya están en ese idioma. **Migración:** en Ajustes → Metadatos → Índice local de Open Library, reconstruir el índice antes del primer recorrido de autores y después de incorporar autores. Mientras no exista un índice de autores, se continúa por Wikipedia y Open Library. La descarga requiere espacio para los tres volcados y se inicia explícitamente, no al arrancar. Un fallo o cancelación conserva la versión activa anterior. El panel permite consultar el progreso, cancelar y reanudar.
- El panel del índice muestra la fase en español, los registros leídos en la fase actual, los MiB descargados y la última actualización del proceso. Se consulta automáticamente cada 5 segundos mientras la página está visible. `PROCESSING_EDITIONS` precede a valoraciones y autores; tener cero autores en esa fase no indica un bloqueo. Para comprobar avance antes de desplegar esta pantalla, consultar `GET /api/metadata/openlibrary/index` con la sesión habitual y comparar `processedRecords` y `updatedAt` en dos lecturas. El backend registra los cambios de fase y el progreso aproximadamente cada 30 segundos cuando llegan nuevos avances; no es un latido independiente si una operación se bloquea. El total de registros del volcado no se conoce de antemano, por lo que no se muestra un porcentaje de procesamiento.
- `CHECKING_SPACE` incluye comprobar los volcados, revisar el espacio libre y cargar ISBN desde MongoDB. El campo `detail` identifica la operación concreta en la pantalla y se guarda antes de iniciarla. El log muestra `preparing` y `prepared` con la duración. Las peticiones HTTP de comprobación tienen un límite de 30 segundos cada una, configurable mediante `metadata.openlibrary.dumps.inspect-timeout-millis` (30000 por defecto), incluida la alternativa por rango si HEAD no está soportado. Los volcados completos ya descargados se reutilizan sin otra petición de cabeceras. Al reanudar se conserva la descarga, pero el procesamiento de ediciones se repite desde el principio; los contadores retenidos durante la preparación pertenecen a la ejecución anterior. Un log de `CHECKING_SPACE` con registros antiguos no demuestra progreso nuevo. Para localizar una espera en una ejecución ya desplegada, obtener el hilo con `docker exec CONTENEDOR jcmd 1 Thread.print` y revisar la sección `open-library-indexer`; la imagen del backend incluye el JDK.
- Las biografías de Open Library consultan primero la caché y, si falta, detectan el idioma: español se conserva sin traducir; otro idioma se traduce a español. Se guardan en `metadataTranslations` tanto las traducciones correctas como el texto detectado en español. La nueva clave versionada incluye texto, destino e idioma conocido, y evita reutilizar resultados antiguos sin detección. Un fallo no se almacena como traducción válida ni introduce la biografía sin traducir.
- Wikipedia en español no llama a LibreTranslate; en otros idiomas traduce directamente. Goodreads detecta antes de traducir y compara códigos de idioma normalizados. Amazon aplica detección al texto sin idioma identificado y traducción directa si el propio contenido identifica otro idioma; el dominio o idioma de interfaz no se toman como garantía. Sus títulos se comprueban por separado. Si la detección o traducción falla, las reseñas conservan el original sin etiquetarlo como español.
- Las reseñas ya no calculan ni sustituyen la valoración global del libro. Las puntuaciones individuales siguen visibles en cada reseña. No se borra retrospectivamente una puntuación antigua de procedencia incierta.
- Las nuevas reseñas incluyen proveedor, enlace de origen, idioma cuando se puede identificar y fecha de obtención. No se inventa el idioma de una reseña por proceder de Amazon España.

## Cola persistente de reseñas

En Ajustes → Metadatos → Opiniones, las reseñas tienen un proceso independiente del recorrido de libros y autores. El estado, cursor, límite superior del recorrido y los intervalos se guardan en Mongo (`reviewQueue`); no se carga toda la biblioteca en memoria. Al reiniciar, un recorrido activo continúa y uno pausado permanece pausado. Se requiere una única instancia del backend: no es una cola distribuida para varias réplicas.

- Intervalos configurables por proveedor entre 15 y 3600 segundos, inicialmente 30 segundos. Se aplican en la conexión HTTP a búsquedas, páginas y redirecciones de Amazon y Goodreads; se conservan las últimas peticiones tras un reinicio. JavaScript, CSS e imágenes del navegador de scraping están deshabilitados.
- **Obtener todas** recorre los libros hasta el límite de identidad fijado al iniciar. Si existe otro recorrido activo o pausado, exige confirmación y sustituye su estado por un recorrido desde cero. No borra reseñas existentes. El trabajador único deja acabar la petición en curso antes de enviar otra; el recorrido sustituido no puede avanzar el cursor nuevo.
- **Obtener reseñas faltantes** solo arranca sin otro recorrido activo, pausado o terminando una petición. Selecciona libros sin reseñas (no libros con reseñas antiguas) y respeta la protección manual. Los libros añadidos después del límite del recorrido quedan para una ejecución posterior.
- **Pausar/Reanudar** conserva el cursor. **Parar** cancela el trabajo pendiente, sin borrar los resultados. Una petición ya enviada puede terminar; la espera entre peticiones consulta el estado para detenerse rápidamente. Un libro interrumpido antes de completar sus peticiones puede consultarse otra vez al reanudar.
- Se respetan los bloqueos persistidos y `Retry-After`. Un error aplaza el libro al menos 30 minutos; tras tres intentos fallidos se contabiliza como no completado y se continúa. Una restricción de acceso detectada en un resultado fallido pausa la cola y requiere revisión manual. El panel muestra progreso, errores y próxima fecha de reintento.
- La caché puede resolver un libro sin nuevas peticiones. El total inicial es orientativo si otros procesos borran libros o incorporan reseñas durante el recorrido.

API `ADMIN`: `GET /api/metadata/review-queue`, `POST /start?all=true|false&replace=true|false&lang=es`, `POST /pause`, `/resume`, `/stop`, y `POST /settings?amazon=30&goodreads=30`, bajo ese mismo prefijo. El antiguo inicio de reseñas delega en la cola y no permite sustituir un recorrido sin confirmación. La opción `metadata.reviews.queue.worker-enabled=false` desactiva solo el trabajador automático (usada en pruebas).

Estos controles reducen la frecuencia de acceso; no garantizan ausencia de bloqueos de las webs.

## Actividad, historial y protección

En Ajustes → **Actividad e historial de metadatos**:

- Consultar las últimas 100 entidades procesadas y las últimas 100 operaciones.
- Reintentar individualmente una consulta fallida, manteniendo las pausas y límites de los proveedores.
- Examinar los campos modificados, sus valores anterior/nuevo y proveedor cuando está disponible.
- Deshacer cambios solo si los campos todavía coinciden con el resultado de aquella operación. Una edición posterior produce conflicto y no se sobrescribe.
- Proteger/desproteger las consultas de libros, autores o reseñas por separado. La protección es por tipo y entidad, no un selector granular por campo.

Una edición manual de la ficha protege sus metadatos de libro; deshacer también protege el tipo afectado. La reconciliación de categorías al arrancar respeta esa protección. Se puede desactivar desde el panel. El historial es prospectivo: no reconstruye cambios anteriores a esta implementación.

Los endpoints `/api/metadata/activity/**` requieren autoridad `ADMIN`. El registro reside en `metadataItems`, `metadataHistory` y `metadataLocks`. Actividad e historial conservan diagnósticos por proveedor y operación (incluidas detección y traducción): timeout, conexión, restricción de acceso, límite de peticiones, pausa preventiva, errores HTTP y respuestas JSON inválidas. Se muestran también avisos de proveedores fallidos aunque otro proveedor complete la operación. No se persisten mensajes crudos de excepciones, URLs ni respuestas externas en estos diagnósticos. Los fallos no clasificables siguen remitiendo al log; los registros anteriores no se reconstruyen.

## Validación de esta entrega

Se ejecutaron únicamente tests afectados, sin volver a ejecutar la suite completa:

- EPUB real temporal → extracción → eventos → Mongo → autor → valoración local → biografía traducida → reseñas → versión superior con otro nombre → descarte de versión igual.
- Reanudación de primera importación y recuperación de actualización confirmada/no confirmada, sin duplicar autores.
- Índice local, caché de traducción, analizadores de reseñas y conservación de los datos existentes.
- Historial, deshacer con control de concurrencia, protección manual y permisos administrativos.
- Corrección de expectativas antiguas en tests de importación asíncrona y controladores de metadatos.
- Compilación Angular en configuración development.

El recorrido integrado se ejecutó dentro de un contenedor temporal con Java 25 y MongoDB de Testcontainers. Los servicios externos se sustituyeron por respuestas controladas; los tests no descargaron los dumps completos, no usaron la biblioteca real y no desplegaron la aplicación de producción. La disponibilidad real de LibreTranslate, Amazon, Goodreads y Open Library con la configuración de producción sigue dependiendo del entorno externo; no se garantiza ausencia futura de bloqueos de scraping.
