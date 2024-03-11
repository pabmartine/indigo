### STAGE 1:BUILD ###
FROM node:16.13.0 AS node
WORKDIR /app
RUN npm cache clean --force
COPY . .
RUN npm install
RUN npm run build --omit=dev


### STAGE 2:RUN ###
FROM nginx:latest AS nginx
COPY --from=node /app/dist/indigo-client /usr/share/nginx/html
COPY /nginx.conf  /etc/nginx/conf.d/default.conf
EXPOSE 80