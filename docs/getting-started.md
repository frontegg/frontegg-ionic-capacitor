# Getting started with Frontegg Ionic Capacitor SDK

Welcome to the Frontegg Ionic Capacitor SDK! Easily integrate Frontegg's out-of-the-box authentication and user management into your hybrid applications for a secure, cross-platform experience.

The Frontegg Ionic Capacitor SDK can be used in two ways:

1. With the hosted Frontegg login that will be called through a webview, enabling all login methods supported on the login box
2. By directly using Frontegg APIs from your custom UI, with available methods

The SDK automatically handles token refresh in the background, ensuring your users stay authenticated without manual intervention.

## Project Requirements

- Minimum iOS deployment version **=> 14**
- Min Android SDK **=> 26**

## Prepare your Frontegg environment

Navigate to Frontegg Portal [ENVIRONMENT] → `Keys & domains`
- If you don't have an application, follow the integration steps after signing up
- Copy your environment's `FronteggDomain` from Frontegg Portal domain for future steps
- Navigate to [ENVIRONMENT] → Authentication → Login method
- Make sure hosted login is toggled on.
- Add the following common redirect URL:

  ```
  {{FRONTEGG_BASE_URL}}/oauth/authorize
  ```

- Replace `{{IOS_BUNDLE_IDENTIFIER}}` with your IOS bundle identifier.
- Replace `{{ANDROID_PACKAGE_NAME}}` with your Android package name.
- Replace `{{FRONTEGG_BASE_URL}}` with your Frontegg domain, i.e `app-xxxx.frontegg.com` or your custom domain.

- For iOS add:
  ```
  {{IOS_BUNDLE_IDENTIFIER}}://{{FRONTEGG_BASE_URL}}/ios/oauth/callback
  ```

- For Android add:
  ```
  {{ANDROID_PACKAGE_NAME}}://{{FRONTEGG_BASE_URL}}/android/oauth/callback
  https://{{FRONTEGG_BASE_URL}}/oauth/account/redirect/android/{{ANDROID_PACKAGE_NAME}}  ← required for assetlinks
  ```

> [!WARNING] 
> On every step, if you have a [custom domain](https://developers.frontegg.com/guides/env-settings/custom-domain), replace the `[frontegg-domain]` and `[your-custom-domain]` placeholders with your custom domain instead of the value from the Keys & domains page.

### Add frontegg package to the project

Add Capacitor to your Ionic project if it doesn't already exist:

```
ionic integrations enable capacitor
```

Use your preferred package manager to install the Frontegg React Native library:

npm:
```
npm install -s @frontegg/react-native
```

yarn:
```
yarn add @frontegg/react-native
```

## Configure your application

1. Create or modify your `capacitor.config.ts` file:

   ```typescript 
      import { CapacitorConfig } from '@capacitor/cli';

      const config: CapacitorConfig = {
        appId: "{{FRONTEGG_APPLICATION_ID}}",
        appName: "{{YOUR_APPLICATION_NAME}}",
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
   
        plugins: {
          FronteggNative:{
            baseUrl: "https://{{FRONTEGG_BASE_URL}}",
            clientId: "{{FRONTEGG_CLIENT_ID}}",
          }
        }
      };
      
      export default config;
   ```
- Replace `{{FRONTEGG_BASE_URL}}` with the domain name from your Frontegg Portal.
- Replace `{{FRONTEGG_APPLICATION_ID}}` with the Application ID from your Frontegg Portal.
- Replace `{{YOUR_APPLICATION_NAME}}` with the Application Name from your Frontegg Portal.
- Replace `{{FRONTEGG_CLIENT_ID}}` with the Client ID from your Frontegg Portal.

2. Add the iOS and Android projects to your Ionic app by running the following commands:

    ```bash
    ionic capacitor add android
    ionic capacitor add ios
    ```
**NOTE**: skip the command if you already have the project added.
