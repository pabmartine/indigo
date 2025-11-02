### STAGE 1:BUILD ###
FROM node:18.10.0 AS node
WORKDIR /app

# Variables de entorno para el build
ARG NG_APP_API_ENDPOINT
ARG NG_APP_INTERNAL_ENDPOINT
ARG NG_APP_WHITELIST
ARG NG_APP_BLACKLIST

RUN npm cache clean --force
COPY . .
RUN npm install

# Debug: Mostrar variables durante el build
RUN echo "Variables durante build:"
RUN echo "NG_APP_API_ENDPOINT: $NG_APP_API_ENDPOINT"
RUN echo "NG_APP_WHITELIST: $NG_APP_WHITELIST"

# Build explícitamente para producción
RUN npm run build -- --configuration=production

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
