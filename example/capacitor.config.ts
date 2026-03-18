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
        baseUrl: 'https://app-x4gr8g28fxr5.frontegg.com',
        clientId: '5f493de4-01c5-4a61-8642-fca650a6a9dc'
      }]
    }
  }
};

export default config;
