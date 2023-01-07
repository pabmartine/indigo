export const environment = {
  production: true,
  endpoint: 'http://indigo-backend:8081/rest/',
  whiteList: ["localhost:8080", "127.0.0.1:8080", "localhost:8081", "127.0.0.1:8081", "192.168.1.40:8081", "krahen.synology.me:8081"],
  blackList: ["localhost:8080/rest/login", "127.0.0.1:8080/rest/login","localhost:8080/rest/login", "127.0.0.1:8081/rest/login", "192.168.1.40:8081/rest/login", "krahen.synology.me:8080/rest/login"]
};
