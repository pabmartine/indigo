# syntax=docker/dockerfile:1
### STAGE 1:BUILD ###
FROM node:18.10.0 AS node
WORKDIR /app

# La instalación solo cambia cuando cambian las dependencias.
COPY package.json package-lock.json ./
# El lockfile existente no incluye la resolución automática de peer dependencies.
RUN --mount=type=cache,target=/root/.npm npm ci --legacy-peer-deps --no-audit --no-fund

COPY . .
RUN npm run build -- --configuration=production

# Las variables solo invalidan la sustitución, no la compilación Angular.
ARG NG_APP_API_ENDPOINT
ARG NG_APP_INTERNAL_ENDPOINT
ARG NG_APP_WHITELIST
ARG NG_APP_BLACKLIST

# Reemplazar las variables en los archivos generados
RUN find /app/dist -name "*.js" -exec sed -i "s|__NG_APP_API_ENDPOINT__|$NG_APP_API_ENDPOINT|g" {} \;
RUN find /app/dist -name "*.js" -exec sed -i "s|__NG_APP_INTERNAL_ENDPOINT__|$NG_APP_INTERNAL_ENDPOINT|g" {} \;
RUN find /app/dist -name "*.js" -exec sed -i "s|__NG_APP_WHITELIST__|$NG_APP_WHITELIST|g" {} \;
RUN find /app/dist -name "*.js" -exec sed -i "s|__NG_APP_BLACKLIST__|$NG_APP_BLACKLIST|g" {} \;

### STAGE 2:RUN ###
FROM nginx:latest AS nginx
COPY --from=node /app/dist/indigo-client /usr/share/nginx/html
COPY /nginx.conf  /etc/nginx/conf.d/default.conf
EXPOSE 80
