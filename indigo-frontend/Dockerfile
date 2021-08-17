# base image
FROM node:14.17.4-alpine as node

# set working directory
WORKDIR /app

# install and cache app dependencies
COPY package*.json ./
RUN npm install

# Copy the main application
COPY . ./

# Arguments
ARG configuration=production

# Build the application
RUN npm run build -- --outputPath=./dist/out --configuration $configuration

#### Stage 2, use the compiled app, ready for production with Nginx
FROM nginx:1.20.1-alpine

# Copy the angular build from Stage 1
COPY --from=build /app/dist/out/ /usr/share/nginx/html

# Copy our custom nginx config
COPY /nginx-custom.conf /etc/nginx/conf.d/default.conf


# Expose port 80 to the Docker host, so we can access it
# from the outside.
EXPOSE 80

ENTRYPOINT ["nginx","-g","daemon off;"]
