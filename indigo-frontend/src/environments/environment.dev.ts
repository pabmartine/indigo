// environment.dev.ts - Solo para desarrollo local
export const environment = {
  production: false,
  endpoint: 'http://localhost:8080/api/',
  internalEndpoint: 'http://localhost:8080/api/',
  whiteList: [
    "localhost:8080",
    "127.0.0.1:8080"
  ],
  blackList: []
};
