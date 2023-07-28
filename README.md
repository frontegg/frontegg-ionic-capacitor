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

```bash
ionic capacitor add android
ionic capacitor add ios
```

# Setup Android Project

1. Open Android Studio and open the android folder created by the capacitor command.
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


# Setup iOS Project


