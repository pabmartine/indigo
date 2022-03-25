export const environment = {
  production: false,
  endpoint: 'http://localhost:8080/rest/',
  whiteList: ["localhost:8080", "127.0.0.1:8080", "localhost:8081", "127.0.0.1:8081"],
  blackList: ["localhost:8080/rest/login", "127.0.0.1:8080/rest/login","localhost:8080/rest/login", "127.0.0.1:8081/rest/login"]
};