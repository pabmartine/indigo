# Compilación del frontend en Jenkins

El pipeline está en `indigo-frontend/Jenkinsfile`. Para usarlo, configurar el job
como **Pipeline script from SCM**, rama `develop`, ruta de script
`indigo-frontend/Jenkinsfile`, o copiar su contenido al script del job actual.
Los cambios deben estar disponibles en `develop` antes de ejecutar Jenkins.
Mantiene los identificadores de credenciales, la red `indigo` y el puerto 8080.
El agente necesita Docker con BuildKit y curl; Maven no interviene en este job.

## Qué cambia

- La instalación copia primero `package.json` y `package-lock.json`. Un cambio
  de código no vuelve a instalar dependencias.
- `npm ci --legacy-peer-deps --no-audit --no-fund` usa el lockfile existente.
  Sin `--legacy-peer-deps`, la instalación probada con npm 8.19.2 falla por
  dependencias peer ausentes en ese lockfile.
- BuildKit conserva las descargas de npm en `/root/.npm` cuando cambian las
  dependencias. Ya no se borra esta caché.
- `.dockerignore` excluye `.angular`, `dist`, archivos de entorno y otros
  archivos locales. El archivo descargado de variables no se copia a la imagen.
- Los argumentos de entorno se declaran después del build Angular. Cambiar
  endpoints o listas solo repite la sustitución en el JavaScript generado.
- El job separa descarga de variables, construcción, validación y despliegue.
  `--progress=plain` muestra los segundos de cada paso y si usa `CACHED`.
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
como `CACHED`. La imagen pasó `nginx -t`, contenía `index.html` y el JavaScript
incluía el endpoint nuevo. Los bloques shell del Jenkinsfile pasaron `sh -n`;
el pipeline completo queda pendiente de ejecución en Jenkins.

Estos tiempos corresponden al equipo local, no al agente de Jenkins. La
instalación también emite avisos de engines por herramientas de desarrollo
modernas con el Node 18.10.0 existente; actualizar esas versiones queda fuera
de este ajuste de caché.
