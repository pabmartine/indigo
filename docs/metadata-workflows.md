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

- Los recorridos de libros y autores guardan en Mongo (`metadataExecutions`) una fecha `lastExecution` por entidad y modalidad («todos»/«incompletos»). Tras parar o reiniciar el NAS, volver a lanzar la misma modalidad consulta solo los elementos pendientes. Encontrado, sin coincidencia y omitido cuentan como inspección terminada; una cancelación o una petición de un recorrido ya sustituido no se confirma. En autores, un error que impida completar algún campo conserva ese autor pendiente para el siguiente lanzamiento, guardando los campos ya obtenidos; en libros, el error cuenta como inspección terminada. Los errores pueden reintentarse individualmente desde Actividad. Al lanzar un recorrido cuya selección ya está completamente inspeccionada se eliminan sus marcas y comienza un nuevo ciclo; una selección vacía no borra el avance. Los nuevos elementos quedan pendientes. Las marcas se guardan separadas de las fichas para que una edición o escritura concurrente no las sobrescriba, y son independientes de las fechas de sincronización con éxito. El progreso muestra los pendientes del lanzamiento actual. El proceso de autores vuelve a comprobar las altas desde libros, pero solo consulta proveedores para los autores pendientes. Se requiere una única instancia del backend y Mongo persistente. Una caída entre la consulta y su confirmación puede repetir ese elemento, conservando el avance confirmado de los anteriores.
- Libros, autores y reseñas pueden ejecutarse simultáneamente desde Ajustes. Libros y autores conservan identificadores, contadores y parada independientes; cambiar entre todos/sin metadatos sustituye únicamente el recorrido de esa entidad. Las reseñas mantienen su cola persistente. `GET /api/metadata/stop?entity=BOOKS|AUTHORS` detiene solo esa entidad; sin parámetro mantiene la parada global de los recorridos no persistentes. Una petición ya enviada puede terminar. Las escrituras de metadatos de libro y reseñas actualizan campos separados, sin sustituir el documento completo.
- Para libros, Open Library consulta primero la versión activa del índice local. Solo usa versiones publicadas, nunca la versión en construcción. Si falta un resultado utilizable, mantiene la consulta HTTP como alternativa.
- Los autores se buscan primero en el catálogo local del [volcado oficial de Open Library](https://openlibrary.org/developers/dumps). El índice guarda nombres/alias normalizados, biografía y disponibilidad de foto solo para los autores presentes en los libros de la biblioteca. Si no hay una coincidencia única, se mantiene el flujo habitual: Wikipedia en español, en el idioma solicitado cuando sea distinto y en inglés como alternativa, y búsqueda HTTP de Open Library si faltan biografía o imagen y no hay un índice de autores activo. Con el índice disponible, no se repite la búsqueda de Open Library por HTTP. Las fotos del catálogo y de Open Library se descargan de su servicio de imágenes.
- La sincronización de autores registra `Author metadata run ... progress` cada 30 segundos y al cambiar de operación, incluyendo libros leídos, nombres únicos, autores comprobados, autores creados, autores procesados y duración de la operación. El temporizador también registra las esperas en MongoDB o Wikipedia aunque no avance un contador; no significa que haya terminado la consulta pendiente. Se cierra al terminar, cancelar o fallar el recorrido. El intervalo se puede configurar con `metadata.authors.progress-log-interval-millis`. La preparación usa lotes de hasta 1000 libros que incluyen solo ID, autores e idiomas, avanzando por ID sin `skip`, y comprueba los autores existentes por lotes sin cargar imágenes. Las altas nuevas se guardan por lotes y se invalida la caché de recuento antes de iniciar la búsqueda de metadatos. Un fallo durante la preparación también termina el estado activo y queda registrado.
- La búsqueda de autores se completa por campo: una imagen del índice no impide buscar la biografía en Wikipedia. Si siguen faltando descripción o imagen, se consulta Wikipedia en inglés y después Open Library por HTTP. Cada campo conserva la primera fuente que lo aporta, también en el modo de actualización completa. La foto solo se considera obtenida después de descargarla correctamente; una URL inexistente permite continuar con las fotos de los proveedores siguientes. Open Library HTTP admite fotos sin biografía. Si falla la traducción de una biografía se conserva su foto como resultado parcial y se busca la descripción en otras fuentes; nunca se guarda la biografía sin traducir. Las peticiones de foto tienen límites de conexión y lectura de 15 segundos, y los fallos temporales se distinguen de una foto inexistente (404). Cuando Wikipedia activa una pausa con fecha de reintento, la consulta conserva el mismo autor y el mismo idioma, espera y los reintenta. El historial permanece en curso hasta obtener un resultado definitivo, sin guardar la pausa como error de ese autor. La espera se puede cancelar; el autor no queda marcado como inspeccionado. El índice local se consulta antes de Wikipedia y Open Library HTTP después, si siguen faltando campos. Si falla la traducción de la biografía local, se continúa por Wikipedia. En «Solo autores sin información completa», un autor vuelve a intentarse si falta descripción o imagen, aunque se haya sincronizado recientemente; no se aplica un plazo de siete días. El recorrido enumera todos los autores de los idiomas de la biblioteca: los completos y los protegidos se contabilizan como omitidos, con el motivo en el log. «Encontrado» exige guardar una descripción o imagen, no basta con que el proveedor devuelva la foto que ya tenía el autor. Si no aporta ningún campo faltante se registra «sin coincidencia». El log indica por separado `descriptionPresent` e `imagePresent`.
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

El enlace **Ver historial detallado de metadatos** abre `/settings/metadata-history`, disponible para administradores. También hay accesos desde los procesos de libros y autores, con un enlace directo a sus errores. La pantalla consulta todo el historial mediante paginación en el servidor, con filtros por entidad, resultado y nombre/título. Permite ver todas las consultas de una misma entidad. La primera página se actualiza cada cinco segundos mientras la pantalla está visible; se puede desactivar.

Cada operación nueva registra fecha de inicio y fin, duración, resultado, motivo de omisión, disponibilidad de campos antes/después, cambios guardados y diagnósticos. Los recorridos de libros y autores incluyen los resultados de las fuentes consultadas; el detalle distingue una foto descargada de una URL que no aportó una foto utilizable. Las entidades protegidas también generan una operación omitida. El historial se crea al empezar la consulta y las operaciones en curso al reiniciar se conservan como errores por interrupción. Los registros anteriores siguen siendo consultables, pero no se reconstruye retrospectivamente su recorrido por proveedores. Las páginas de la lista excluyen los valores de los cambios y las imágenes; estos se cargan solo al abrir el detalle. Los endpoints son `GET /api/metadata/activity/history/page?page=0&size=25&type=AUTHORS&status=ERROR&search=...&entityId=...` y `GET /api/metadata/activity/history/{id}`. Se conservan los endpoints de las últimas 100 operaciones y las acciones de reintentar, proteger y deshacer.

- Consultar las últimas 100 entidades procesadas y las últimas 100 operaciones.
- Reintentar individualmente una consulta fallida, manteniendo las pausas y límites de los proveedores.
- Examinar los campos modificados, sus valores anterior/nuevo y proveedor cuando está disponible.
- Deshacer cambios solo si los campos todavía coinciden con el resultado de aquella operación. Una edición posterior produce conflicto y no se sobrescribe.
- Proteger/desproteger las consultas de libros, autores o reseñas por separado. La protección es por tipo y entidad, no un selector granular por campo.

Una edición manual de la ficha protege sus metadatos de libro; deshacer también protege el tipo afectado. La reconciliación de categorías al arrancar respeta esa protección. Se puede desactivar desde el panel. El historial es prospectivo: no reconstruye cambios anteriores a esta implementación.

Los endpoints `/api/metadata/activity/**` requieren autoridad `ADMIN`. El registro reside en `metadataItems`, `metadataHistory` y `metadataLocks`. Actividad e historial conservan diagnósticos por proveedor y operación (incluidas detección y traducción): timeout, conexión, restricción de acceso, límite de peticiones, pausa preventiva, errores HTTP y respuestas JSON inválidas. Se muestran también avisos de proveedores fallidos aunque otro proveedor complete la operación. No se persisten mensajes crudos de excepciones, URLs ni respuestas externas en estos diagnósticos. Los fallos de traducción de biografías se identifican como descripción pendiente, en lugar de atribuirlos a un fallo genérico de Open Library o Wikipedia. LibreTranslate tiene un cliente HTTP propio, con 5 segundos de conexión y 60 segundos de lectura, configurables mediante `METADATA_LIBRETRANSLATE_CONNECT_TIMEOUT_MILLIS` y `METADATA_LIBRETRANSLATE_READ_TIMEOUT_MILLIS`. Wikipedia serializa las peticiones entre idiomas y mantiene un intervalo fijo de un segundo entre inicios de peticiones (`METADATA_HTTP_WIKIPEDIA_MINIMUM_INTERVAL_MILLIS`). Cada petición HTTP tiene hasta tres intentos: el inicial y dos reintentos con esperas de 1 y 2 segundos, reiniciadas para cada petición. Se reintentan HTTP 408/429/5xx y errores de E/S; un 400/401/403 no se reintenta ni abre un circuito de quince minutos. Un 404 es ausencia de datos. Se respeta `Retry-After` cuando lo envía Wikipedia. La pausa solicitada por el servidor conserva la petición y el autor actuales hasta el momento indicado, también cuando supera los 10 segundos. No se recorre la biblioteca convirtiendo esa misma pausa en un error por autor. El límite de tres intentos sigue vigente; sólo cuentan las peticiones HTTP reales. Ajustes publica `waitingFor` y `waitingUntil` mientras se espera, y permite detener el proceso. Los errores no crean checkpoints; los campos recuperados se conservan. La espera, el bloqueo para serializar y la siguiente petición responden a la cancelación de la ejecución. Los timeouts de conexión/lectura siguen siendo 5/10 segundos; los 10 segundos de presupuesto limitan el backoff local, no `Retry-After` ni el tiempo de red. Los fallos no clasificables siguen remitiendo al log; los registros anteriores no se reconstruyen.

## Validación de esta entrega

Los fallos de consultas HTTP, proveedores de libros/autores, descarga de fotos y detección/traducción de LibreTranslate se registran en el backend de Indigo a nivel `WARN`, con la excepción completa y sus causas. Las consultas de entidades incluyen su identificador y nombre/título; los fallos de traducción incluyen el idioma de destino y la longitud del texto. Los errores que llegan al seguimiento incluyen el identificador del historial. El diagnóstico resumido de la pantalla se conserva. Para investigar una pausa preventiva, buscar el primer fallo anterior a `circuit opened`, o el HTTP 429 que inició la pausa, en el log del contenedor de Indigo. Los avisos de `apscheduler` en LibreTranslate corresponden al servicio de traducción, no al cliente de Wikipedia en Indigo.

Para los demás proveedores HTTP de `DataUtils` (no Wikipedia), mientras dure una pausa Indigo conserva en memoria la excepción que la inició y la incluye como causa en las consultas posteriores rechazadas. El historial mantiene el resultado `PAUSED`, la fecha de reintento y el código HTTP original cuando existe. La causa se borra al expirar la pausa. Ante HTTP 429 se espera como mínimo el mayor plazo entre `Retry-After` y la espera progresiva de Indigo (60 segundos iniciales, hasta 900 por defecto); un plazo corto del proveedor no reduce la espera progresiva. Las respuestas correctas intermedias no reinician la progresión: se restablece tras 30 minutos sin otro HTTP 429.

Se ejecutaron únicamente tests afectados, sin volver a ejecutar la suite completa:

- EPUB real temporal → extracción → eventos → Mongo → autor → valoración local → biografía traducida → reseñas → versión superior con otro nombre → descarte de versión igual.
- Reanudación de primera importación y recuperación de actualización confirmada/no confirmada, sin duplicar autores.
- Índice local, caché de traducción, analizadores de reseñas y conservación de los datos existentes.
- Historial, deshacer con control de concurrencia, protección manual y permisos administrativos.
- Corrección de expectativas antiguas en tests de importación asíncrona y controladores de metadatos.
- Compilación Angular en configuración development.

El recorrido integrado se ejecutó dentro de un contenedor temporal con Java 25 y MongoDB de Testcontainers. Los servicios externos se sustituyeron por respuestas controladas; los tests no descargaron los dumps completos, no usaron la biblioteca real y no desplegaron la aplicación de producción. La disponibilidad real de LibreTranslate, Amazon, Goodreads y Open Library con la configuración de producción sigue dependiendo del entorno externo; no se garantiza ausencia futura de bloqueos de scraping.

## Rendimiento de autores: comprobación del 4 de octubre de 2026

En la ejecución observada en el NAS, la preparación duró unos ocho minutos: 2 min 49 s leyendo libros, 4 min 41 s comprobando autores y el resto seleccionando pendientes. Dos respuestas HTTP 429 añadieron pausas de 60 y 120 segundos. En esa medición había aproximadamente 2,1 GiB de RAM disponibles.

La comprobación de nombres excluye ahora `_id` para que el índice de nombres pueda responder sin leer documentos de autores que contienen imágenes. En una prueba de 1.000 nombres del NAS, la consulta anterior examinó 1.000 documentos en 88 ms y la nueva examinó cero documentos en 5 ms. Es una medición de ese lote, no una estimación del tiempo total con caché fría.

Cuando un autor ya tiene descripción y sólo falta la foto, la petición a Wikipedia se propaga con `descriptionNeeded=false`: se devuelve la foto disponible sin enviar la biografía a LibreTranslate. El modo de actualización completa sigue solicitando y traduciendo la descripción. En esa versión se mantenían los intervalos adaptativos; la revisión del 5 de octubre los sustituye por reintentos acotados.


## Revisión del 5 de octubre de 2026

- El índice local de autores es la fuente de Open Library mientras su versión de autores coincide con la versión activa. Aunque no encuentre coincidencias o falten campos, no se vuelve a consultar su API. Sólo se usa la API si el índice de autores no está disponible. Las imágenes siguen descargándose desde la URL del catálogo.
- Cuando sólo falta una foto, el catálogo local omite también la traducción de biografías. Wikipedia ya propagaba esa selección de campos.
- Se elimina el bucle ilimitado que repetía la búsqueda completa del autor después de cada pausa. El cliente HTTP reintenta únicamente la petición que falló; un fallo al obtener detalles no repite una búsqueda que ya tuvo éxito.
- Los lotes del proceso cargan identificador y nombre, evitando cargar cien fotos/biografías que luego se volvían a leer al procesar cada autor.
- `/settings` abre el panel de metadatos, usa los colores y radios de la aplicación y adapta controles y tarjetas al móvil. La actividad detallada se carga al desplegar su panel. Los botones distinguen detener de pausar, evitan solicitudes duplicadas y ofrecen etiquetas accesibles. La consulta periódica se recupera tras errores temporales. Preparación, detención y progreso fraccionario tienen estados explícitos.
- Parámetros de reintento: `METADATA_HTTP_WIKIPEDIA_RETRY_ATTEMPTS` (3; límite defensivo 1–5), `METADATA_HTTP_WIKIPEDIA_RETRY_INITIAL_DELAY_MILLIS` (1000) y `METADATA_HTTP_WIKIPEDIA_RETRY_MAX_WAIT_MILLIS` (10000). No modifican el timeout de lectura ni aumentan entre autores.

La política sigue las recomendaciones de [MediaWiki sobre reintentos](https://www.mediawiki.org/wiki/API:Etiquette) y [Retry-After](https://www.mediawiki.org/wiki/Wikimedia_APIs/Rate_limits). Validación local: 150 pruebas de backend en 34 clases y 12 pruebas de ajustes en Chrome Headless, además de compilación Angular y revisión visual en escritorio y móvil con datos simulados. Estas comprobaciones no miden el rendimiento de la nueva versión desplegada en el NAS.


### Corrección de la propagación de pausas de Wikipedia

La primera revisión del 5 de octubre rechazaba localmente las consultas cuando `Retry-After` superaba el presupuesto de backoff. Eso permitía avanzar por miles de autores durante una sola pausa, generando errores repetidos sin peticiones HTTP reales. Ahora se espera sobre la misma petición, sin consumir intentos por estar esperando. Una vez agotados los tres intentos reales, puede registrarse el fallo; la siguiente consulta también respeta cualquier pausa vigente antes de emitir su primera petición. No se añade un bucle de reintentos sobre el autor completo ni se acumulan intervalos entre autores.

La espera es cancelable y se muestra en el proceso de autores con la hora prevista. Al finalizar o detener el proceso se limpia el estado de espera, sin afectar a libros ni a ejecuciones posteriores. Los errores históricos no se borran; esos autores conservan su condición de pendientes cuando faltaron campos por un fallo.

Validación de la corrección de pausas: 54 pruebas de backend en 8 clases y 13 pruebas de ajustes superadas, más compilación Angular. Incluye espera superior al presupuesto local, reintento de la misma petición, máximo de tres intentos reales, respeto de la pausa en la siguiente consulta, cancelación y limpieza del estado visible.
## Arranque automático de metadatos

Se puede lanzar un proceso en cada arranque del backend, cuando Spring publica
`ApplicationReadyEvent`. Está desactivado por defecto. Configuración YAML:

```yaml
metadata:
  autostart:
    enabled: true
    type: AUTHORS_EMPTY
    lang: es
```

La configuración incluida en `application.yml` admite las variables de entorno
`METADATA_AUTOSTART_ENABLED=true`, `METADATA_AUTOSTART_TYPE=AUTHORS_EMPTY` y
`METADATA_AUTOSTART_LANG=es`. Se selecciona un único tipo por arranque:

| Tipo | Proceso existente |
| --- | --- |
| `AUTHORS_ALL` | Autores, `FULL` |
| `AUTHORS_EMPTY` | Autores, `PARTIAL` |
| `BOOKS_ALL` | Libros, `FULL` |
| `BOOKS_EMPTY` | Libros, `PARTIAL` |
| `REVIEWS_ALL` | Reseñas, `FULL` |
| `REVIEWS_EMPTY` | Reseñas, `PARTIAL` |

Los valores por defecto son `enabled: false`, `type: AUTHORS_EMPTY` y `lang: es`.
Un tipo desconocido produce un error de configuración al arrancar.
`EMPTY` reutiliza los criterios actuales de datos vacíos/incompletos, por lo que
permite avanzar entre reinicios conservando las reglas de actualización y el
seguimiento de ejecuciones del proceso manual. `ALL` selecciona el modo completo
del proceso existente. El trabajo utiliza los mismos procesos en segundo plano,
historial y controles de parada que el arranque manual.

Si ya existe un trabajo de reseñas activo o pausado, se conserva y se registra
un aviso; el arranque automático no lo reemplaza ni lo reanuda si está pausado.
Los errores al lanzar el proceso se registran sin impedir el arranque del backend.
