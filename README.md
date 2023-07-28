# @frontegg/ionic-capacitor

Frontegg Ionic Capacitor SDK

## Add project

Add capacitor to the ionic project by running the following command:

```bash
ionic integrations enable capacitor
```

## Install

Install the frontegg ionic SDK package by running the following command:

```bash
npm install @frontegg/ionic-capacitor
```

## Configure

1. Create or Modify your `capacitor.config.ts` file:

   ```typescript 
      import { CapacitorConfig } from '@capacitor/cli';

      const config: CapacitorConfig = {
        appId: '{YOUR_APPLICATION_ID}',
        appName: '{YOUR_APPLICATION_NAME}',
        webDir: 'www',
        server: {
          androidScheme: 'https'
        },
        ios: {
          path: 'ios',
        },
        android: {
          path: 'android',
        },
      };
      
      export default config;
   ```

# Add iOS and Android packages

Add the iOS and Android projects to your ionic app by running the following commands:

**NOTE: skip the command if you already have the project added.**

```bash
ionic capacitor add android
ionic capacitor add ios
```

### Setup Android Project

1. Open Android Studio and open the android folder created by the capacitor command:
   ```bash
      ionic capacitor open android
   ```
   or open the android studio manually.
2. 
2. Open the `app/build.gradle` file and add the following line to the before `android` section:
   ```groovy
   def fronteggDomain = "frontegg-domain-without-https-protocal"
   def fronteggClientId = "frontegg-client-id"
   ```
3. Add BuildConfig inside the `android.defaultConfig` section:
   ```groovy
   manifestPlaceholders = [
     "package_name"      : applicationId,
     "frontegg_domain"   : fronteggDomain,
     "frontegg_client_id": fronteggClientId
   ]
   
   buildConfigField "String", 'FRONTEGG_DOMAIN', "\"$fronteggDomain\""
   buildConfigField "String", 'FRONTEGG_CLIENT_ID', "\"$fronteggClientId\""
   
   ```
4. Add `bundleConfig=true` if not exists inside the `android` section:
   ```groovy
   buildFeatures {
     buildConfig = true
   }
   ```
5. Open the `app/src/main/AndroidManifest.xml` file and add the following line to the before `manifest` section:
   ```xml
   <activity android:name="com.frontegg.android.AuthenticationActivity" android:exported="true">
      <intent-filter android:autoVerify="true">
          <action android:name="android.intent.action.VIEW" />

          <category android:name="android.intent.category.DEFAULT" />
          <category android:name="android.intent.category.BROWSABLE" />

          <data
              android:host="${frontegg_domain}"
              android:pathPrefix="/android/${package_name}/callback"
              android:scheme="https"
          />
      </intent-filter>
   </activity>
   ```
6. Modify the minSdkVersion to 26 inside `variables.gradle` file:
   ```groovy
   ext {
     minSdkVersion = 26
     ...
   }
   ```


### Setup iOS Project

Frontegg iOS SDK requires `CODE_SIGNING_ALLOWED=YES` to be set in the project settings.
Every time you run `ionic capacitor copy ios` the `CODE_SIGNING_ALLOWED` will be set to `NO` and you will need to set it back to `YES` manually.

1. Open Android Studio and open the android folder created by the capacitor command:
   ```bash
      ionic capacitor open ios
   ```
   or open the xcode manually.

2. Create new `Frontegg.plist` with the following xml:
   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
   <plist version="1.0">
     <dict>
       <key>baseUrl</key>
       <string>YOUR_FRONTEGG_DOMAIN</string>
       <key>clientId</key>
       <string>YOUR_FRONTEGG_CLIENT_ID</string>
     </dict>
   </plist>

   ```

3. Enable `CODE_SIGNING_ALLOWED` in the Podfile under `/ios/App` folder.




## Usage

### Initialize Frontegg

### Ionic with Angular:

1. Open the `src/app/app.module.ts` file and add the following line to the before `@NgModule` section:
   ```typescript
   import { FronteggService } from '@frontegg/ionic-capacitor';
  
   @NgModule({ 
     // ...
     providers: [ {
       provide: 'Frontegg',
       useValue: new FronteggService(),
     }]
     // ...
   })
   ```
2. Find full example under `example/src/app/tab1/tab1.page.ts` file.




