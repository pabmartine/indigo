// environment.prod.ts - Para producción
export const environment = {
  production: true,
  endpoint: '__NG_APP_API_ENDPOINT__',
  internalEndpoint: '__NG_APP_INTERNAL_ENDPOINT__',
  whiteList: '__NG_APP_WHITELIST__'.split(',').map(item => item.trim()).filter(item => item.length > 0),
  blackList: '__NG_APP_BLACKLIST__'.split(',').map(item => item.trim()).filter(item => item.length > 0)
};
