# Compilación del frontend en Jenkins

El pipeline está en `indigo-frontend/Jenkinsfile`. Para usarlo, configurar el job
como **Pipeline script from SCM**, rama `develop`, ruta de script
`indigo-frontend/Jenkinsfile`, o copiar su contenido al script del job actual.
Los cambios deben estar disponibles en `develop` antes de ejecutar Jenkins.
Mantiene los identificadores de credenciales, la red `indigo` y el puerto 8080.
El agente necesita Docker y curl; Maven no interviene en este job.
El pipeline comprueba `docker buildx version`: si está disponible, usa BuildKit;
si falta, usa `DOCKER_BUILDKIT=0` sin la opción `--progress=plain`.
El Dockerfile funciona con ambos constructores y no requiere `RUN --mount`.

## Qué cambia

- La instalación copia primero `package.json` y `package-lock.json`. Un cambio
  de código no vuelve a instalar dependencias.
- `npm ci --legacy-peer-deps --no-audit --no-fund` usa el lockfile existente.
  Sin `--legacy-peer-deps`, la instalación probada con npm 8.19.2 falla por
  dependencias peer ausentes en ese lockfile.
- Docker conserva la capa de instalación y su caché npm mientras no cambien
  las dependencias. Si cambian los manifiestos, se repite la instalación; no
  se usa un montaje de caché que requiera BuildKit.
- `.dockerignore` excluye `.angular`, `dist`, archivos de entorno y otros
  archivos locales. El archivo descargado de variables no se copia a la imagen.
- Los argumentos de entorno se declaran después del build Angular. Cambiar
  endpoints o listas solo repite la sustitución en el JavaScript generado.
- El job separa descarga de variables, construcción, validación y despliegue.
  Con BuildKit, `--progress=plain` muestra los segundos de cada paso y si usa
  `CACHED`; el constructor clásico muestra `Using cache`.
- El contenedor actual sigue funcionando durante la construcción y la
  validación de Nginx e `index.html`. Se sustituye al desplegar, con una breve
  interrupción. Un fallo al arrancar el nuevo contenedor no tiene rollback
  automático.
- Se evita la ejecución simultánea de este job y se elimina el paso Maven y
  el checkout implícito. La imagen nueva se etiqueta por número de build;
  `latest` se actualiza después de arrancar el contenedor.

## Cómo interpretar los tiempos

La vista original agrupa descarga, instalación, compilación y Docker en
`Deploy`: por sí sola no demuestra qué paso consume los 40–140 minutos.
Comparar la primera ejecución del nuevo pipeline con una ejecución posterior
con un cambio de código: el paso `npm ci` debe aparecer como `CACHED` en la
segunda. Si solo cambian variables, el build Angular también debe estar cacheado.

La caché depende del daemon/builder Docker usado. Con varios agentes, conviene
usar un agente estable; cambiar de agente o purgar la caché obliga a reconstruir.
Las etiquetas `build-*` se conservan para poder identificar imágenes anteriores;
revisar su retención según el espacio disponible, sin purgar la caché entre builds.
Si Angular sigue tardando decenas de minutos, revisar CPU, RAM, swap y carga
concurrente del agente con los tiempos individuales del log.

Referencias: [caché Docker](https://docs.docker.com/build/cache/optimize/)
y [npm ci](https://docs.npmjs.com/cli/commands/npm-ci/).

## Validación local

Se construyó la imagen con el Dockerfile completo y variables de prueba:
instalación de 1.229 paquetes en 16,1 s y compilación de producción en 30,6 s.
Al repetir cambiando el endpoint, tanto `npm ci` como el build Angular aparecieron
como `CACHED` en BuildKit. La imagen pasó `nginx -t`, contenía `index.html` y el JavaScript
incluía el endpoint nuevo. Los bloques shell del Jenkinsfile pasaron `sh -n`;
el pipeline completo queda pendiente de ejecución en Jenkins.

Tras la corrección de compatibilidad se construyó también la imagen completa
con `DOCKER_BUILDKIT=0`: contexto de 2,012 MB y compilación Angular de 41,1 s.
Se comprobaron la sintaxis shell y ambas rutas de selección del constructor
con un CLI simulado, incluida la ausencia de `--progress=plain` sin Buildx.

Estos tiempos corresponden al equipo local, no al agente de Jenkins. La
instalación también emite avisos de engines por herramientas de desarrollo
modernas con el Node 18.10.0 existente; actualizar esas versiones queda fuera
de este ajuste de caché.

## Compatibilidad con Jenkins sin Buildx

El error `BuildKit is enabled but the buildx component is missing or broken`
ocurre antes de construir la imagen. Se corrigió la selección del constructor
y se retiró el montaje de caché npm del Dockerfile. No basta con cambiar la
variable del pipeline: el Dockerfile con `RUN --mount` tampoco funciona con
el constructor clásico. Actualizar ambos archivos en `develop` y el script
del job si está pegado en la configuración de Jenkins.

El constructor clásico está obsoleto; esta compatibilidad permite usar el
Docker actual del agente. Instalar Buildx donde se ejecuta el CLI de Docker
(dentro del contenedor Jenkins, si el CLI está allí) permite volver a usar
BuildKit automáticamente. Referencia:
[constructor clásico y Buildx](https://docs.docker.com/engine/deprecated/#legacy-builder-for-linux-images).
