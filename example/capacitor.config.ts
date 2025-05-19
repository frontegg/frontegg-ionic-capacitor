import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.frontegg.demo',
  appName: 'FronteggIonicExample',
  webDir: 'www',
  server: {
    androidScheme: 'https'
  },
  plugins:{
    FronteggNative:{
      useChromeCustomTabs:true,
      useAssetLinks:true,
      regions:[{
        key: 'EU',
        baseUrl: 'https://autheu.davidantoon.me',
        clientId: 'b6adfe4c-d695-4c04-b95f-3ec9fd0c6cca'
      }, {
        key: 'US',
        baseUrl: 'https://authus.frontegg.com',
        clientId: 'd7d07347-2c57-4450-8418-0ec7ee6e096b'
      }]
    }
  }
};

export default config;
