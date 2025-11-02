// environment.ts
declare const process: any;

export const environment = {
  production: false,
  endpoint: process.env['NG_APP_API_ENDPOINT'],
  internalEndpoint: process.env['NG_APP_INTERNAL_ENDPOINT'],
  whiteList: process.env['NG_APP_WHITELIST']
    ? process.env['NG_APP_WHITELIST'].split(',').map(item => item.trim())
    : [],
  blackList: process.env['NG_APP_BLACKLIST']
    ? process.env['NG_APP_BLACKLIST'].split(',').map(item => item.trim())
    : []
};
