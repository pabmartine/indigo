# base image
FROM node:14.17.4-alpine as node

# set working directory
WORKDIR /app

# install and cache app dependencies
COPY . .
RUN npm install
RUN npm run build --prod

FROM nginx:1.20.1-alpine
COPY --from=node /app/dist/indigo-client /usr/share/ngnix/html

# Expose port 80
EXPOSE 80

# Containers run nginx with global directives and daemon off
ENTRYPOINT ["nginx", "-g", "daemon off;"]
