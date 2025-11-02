### STAGE 1:BUILD ###
FROM node:18.10.0 AS node
WORKDIR /app

# Variables de entorno para el build
ARG NG_APP_API_ENDPOINT
ARG NG_APP_INTERNAL_ENDPOINT
ARG NG_APP_WHITELIST
ARG NG_APP_BLACKLIST

# Exportar las variables para que estén disponibles durante el build
ENV NG_APP_API_ENDPOINT=$NG_APP_API_ENDPOINT
ENV NG_APP_INTERNAL_ENDPOINT=$NG_APP_INTERNAL_ENDPOINT
ENV NG_APP_WHITELIST=$NG_APP_WHITELIST
ENV NG_APP_BLACKLIST=$NG_APP_BLACKLIST

RUN npm cache clean --force
COPY . .
RUN npm install

# Debug: Mostrar variables durante el build
RUN echo "Variables durante build:"
RUN echo "NG_APP_API_ENDPOINT: $NG_APP_API_ENDPOINT"
RUN echo "NG_APP_WHITELIST: $NG_APP_WHITELIST"

# Build explícitamente para producción
RUN npm run build -- --configuration=production

### STAGE 2:RUN ###
FROM nginx:latest AS nginx
COPY --from=node /app/dist/indigo-client /usr/share/nginx/html
COPY /nginx.conf  /etc/nginx/conf.d/default.conf
EXPOSE 80
