# Validación de reseñas — actualizada el 6 de septiembre de 2026

## Comprobación real

Se ejecutaron los casos de uso Java con las dependencias del JAR del backend,
la configuración de `BaseConfiguration.webClient()` (sin JavaScript ni imágenes)
y Java 25.0.4.1 dentro de Docker. La JVM usó `en_US`. El contenedor de diagnóstico
usó la imagen local `maven:3.9-eclipse-temurin-21` con el JDK 25 montado en lectura;
no se desplegó el backend ni se probó la imagen final en el servidor de producción.
Las búsquedas fueron «El hobbit», autor «J. R. R. Tolkien». No se emplearon cuentas,
proxies ni mecanismos para resolver desafíos.

| Proveedor | Respuesta observada |
| --- | --- |
| Goodreads | Búsqueda HTTP 200, ficha HTTP 200, 10 reseñas extraídas. |
| Amazon.es | Búsqueda HTTP 200; `/product-reviews/8445011405/` redirigió con HTTP 302 a `/ap/signin`, HTTP 200. Se detectó autenticación obligatoria y se pausó el proveedor sin reintentar. |
| Amazon.es, ficha pública | Búsqueda HTTP 200, `/dp/8445011405` HTTP 200, **10 reseñas extraídas sin sesión** con el cliente Java dentro de Docker. |

Una consulta anterior con wget devolvió 404 para la lista de reseñas de Amazon.
La comprobación posterior confirmó que la ficha pública sí permite extraer reseñas.
Ahora el endpoint predeterminado consulta directamente `/dp/{asin}`. Se conserva
la alternativa ante 404 para configuraciones antiguas de `/product-reviews/`;
no se activa ante autenticación, CAPTCHA, 403 o 429.
Se extrae la selección visible de la ficha, no todas las reseñas paginadas.
Si el despliegue define `METADATA_AMAZON_REVIEWS`, debe actualizarse a
`https://www.amazon.es/dp/$asin` o quitarse para usar el nuevo valor predeterminado.

La prueba de Goodreads no utilizó LibreTranslate: verifica acceso, selección y
extracción. La traducción sigue dependiendo de la conexión configurada al servicio.
El éxito puntual no garantiza acceso futuro ni desde otra IP.

## Comportamiento implementado

- Amazon ya no se omite por detectar Docker. Su habilitación sigue dependiendo
  de `FLAGS_AMAZON`, cuyo valor predeterminado es `false`.
- Búsquedas con parámetros codificados y comprobación de título y autor.
- Fechas de Amazon en español o inglés independientes del idioma de la JVM.
- Selectores de reseñas por clase completa para evitar coincidencias con
  contenedores auxiliares; máximo 10 reseñas devueltas.
- Detección de páginas de autenticación y desafíos aunque respondan HTTP 200.
- 401, 403, 429 y 503 suspenden inmediatamente el proveedor; los demás errores
  HTTP 4xx no se reintentan dentro de la política.
- Se respeta `Retry-After` en segundos o fecha HTTP, aplicando como mínimo la
  pausa configurada (30 minutos por defecto).
- `reviewProviderStates` en Mongo conserva `blockedUntil` por proveedor. Las
  instancias consultan ese estado antes de iniciar una petición sin caché.
- La caché de resultados y el intervalo entre peticiones siguen en memoria.
  Los intervalos no constituyen un límite global distribuido entre réplicas.
- Los errores de análisis de todas las tarjetas encontradas se comunican como
  errores, en lugar de guardar un resultado vacío satisfactorio.
- Si ningún proveedor devuelve reseñas y alguno falla, se conserva el contenido
  anterior y no se marca como completada la actualización de reseñas.

## Pruebas reproducibles

Ejecutar en `indigo-backend` con Maven y el JDK configurado:

```sh
mvn -Dtest=FindGoodReadsReviewsUseCaseImplTest,FindAmazonReviewsUseCaseImplTest,ReviewProviderRequestPolicyTest,ReviewPageGuardTest,FindReviewMetadataFailureTest test
```

Las pruebas usan respuestas controladas; no generan tráfico a Goodreads ni Amazon.
Cubren las reseñas existentes, fechas españolas, rechazo de otro autor, alternativa
ante 404, CAPTCHA, redirección de autenticación, 429/Retry-After, persistencia y
recarga de la pausa con Mongo simulado, y conservación de reseñas ante errores.
