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
      mainActivityClass: 'com.frontegg.demo.MainActivity',
      useDiskCacheWebView: false,
      regions:[{
        key: 'default',
        baseUrl: 'https://autheu.davidantoon.me',
        clientId: 'b6adfe4c-d695-4c04-b95f-3ec9fd0c6cca',
        applicationId: '7ae7de24-99f7-4880-a325-1b638ad933ee'
      }]
    }
  }
};

export default config;
