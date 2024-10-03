# @frontegg/ionic-capacitor

The @frontegg/ionic-capacitor SDK integrates Frontegg's authentication and user management capabilities into Ionic Capacitor apps. It simplifies adding features like login, signup, and user profile management, enhancing security and user experience for mobile applications. Follow the integration steps below to use the SDK:

## Table of Contents

- [Project Requirements](#project-requirements)
- [Getting Started](#getting-started)
  - [Prepare Frontegg workspace](#prepare-frontegg-workspace)
  - [Setup Hosted Login](#setup-hosted-login)
  - [Add frontegg package to the project](#add-frontegg-package-to-the-project)
  - [Configure your application](#configure-your-application)
- [Setup iOS Project](#setup-ios-project)
  - [Create Frontegg plist file](#create-frontegg-plist-file)
  - [Config iOS associated domain](#config-ios-associated-domain)
- [Setup Android Project](#setup-android-project)
  - [Set minimum SDK version](#set-minimum-sdk-version)
  - [Configure build config fields](#configure-build-config-fields)
  - [Config Android AssetLinks](#config-android-assetlinks)
  - [Enabling Chrome Custom Tabs for Social Login](#enabling-chrome-custom-tabs-for-social-login)
- [Angular Usages](#angular-usages)
  - [Integrate Frontegg](#integrate-frontegg)
  - [Protect Routes](#protect-routes)
  - [Get Logged In User](#get-logged-in-user)
  - [Switch Tenant](#switch-tenant)
- [Embedded Webview vs Hosted](#embedded-webview-vs-hosted)
  - [Enable hosted webview in iOS Platform](#enable-hosted-webview-in-ios-platform)
  - [Enable hosted webview in Android Platform](#enable-hosted-webview-in-android-platform)
- [Multi-Region Support](#multi-region-support)
  - [Step 1: Add regions to your Frontegg configuration](#step-1-add-regions-to-your-frontegg-configuration)
  - [Setup multi-region support for iOS Platform](#setup-multi-region-support-for-ios-platform)
  - [Setup multi-region support for Android Platform](#setup-multi-region-support-for-android-platform)
- [Multi-apps Android Support](#multi-apps-support)
  - [Step 1: Add application id to your Frontegg configuration](#step-1-add-application-id-to-your-frontegg-configuration)

## Project Requirements

- Minimum iOS deployment version **=> 14**
- Min Android SDK **=> 26**

## Getting Started

### Prepare Frontegg workspace

Navigate to [Frontegg Portal Settings](https://portal.frontegg.com/development/settings), If you don't have application
follow integration steps after signing up.
Copy FronteggDomain to future steps
from [Frontegg Portal Domain](https://portal.frontegg.com/development/settings/domains)

### Setup Hosted Login

- Navigate to [Login Method Settings](https://portal.frontegg.com/development/authentication/hosted)
- Toggle Hosted login method for iOS:
  - Add `{{IOS_BUNDLE_IDENTIFIER}}://{{FRONTEGG_BASE_URL}}/ios/oauth/callback`
- Toggle Hosted login method for Android:
  - Add `{{ANDROID_PACKAGE_NAME}}://{{FRONTEGG_BASE_URL}}/android/oauth/callback` **(without assetlinks)**
  - Add `Add https://{{FRONTEGG_BASE_URL}}/oauth/account/redirect/android/{{ANDROID_PACKAGE_NAME}}` **(required for assetlinks)**
- Add `{{FRONTEGG_BASE_URL}}/oauth/authorize`
- Replace `IOS_BUNDLE_IDENTIFIER` with your application identifier
- Replace `FRONTEGG_BASE_URL` with your frontegg base url
- Replace `ANDROID_PACKAGE_NAME` with your android package name
-

### Add frontegg package to the project

Add capacitor to the ionic project if not exists:

```bash
ionic integrations enable capacitor
```

Use a package manager npm/yarn to install frontegg React Native library.

**NPM:**

```bash
npm install -s @frontegg/react-native
```

**Yarn:**

```bash
yarn add @frontegg/react-native
```

## Configure your application

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
   
        plugins: {
          FronteggNative:{
            baseUrl: 'https://{FRONTEGG_DOMAIN_HOST.com}',
            clientId: '{FRONTEGG_CLIENT_ID}',
          }
        }
      };
      
      export default config;
   ```

2. Add the iOS and Android projects to your ionic app by running the following commands:

   **NOTE: skip the command if you already have the project added.**

    ```bash
    ionic capacitor add android
    ionic capacitor add ios
    ```

## Setup iOS Project

### Create Frontegg plist file

To setup your SwiftUI application to communicate with Frontegg.

1. Open the ios folder created by capacitor, and run this command:
   ```bash
      ionic capacitor open ios
   ```
   or open the Xcode manually.
2. Create a new file named `Frontegg.plist` under your root project directory, this file will store values to be used
   variables by Frontegg SDK:

    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
    <plist version="1.0">
    <dict>
        <key>lateInit</key>
        <true/>
    </dict>
    </plist>
    ```

3. Enable `CODE_SIGNING_ALLOWED` in the Podfile under `/ios/App` folder.

### Handle Open App with URL

To handle Login with magic link and other authentication methods that require to open the app with a URL, you have to
add the following code to the `AppDelegate.swift` file.

```swift

import UIKit
import Capacitor
import FronteggSwift

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    /*
     * Called when the app was launched with a url. Feel free to add additional processing here,
     * but if you want the App API to support tracking app url opens, make sure to keep this call
     */
    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey: Any] = [:]) -> Bool {
        
        if(FronteggAuth.shared.handleOpenUrl(url)){
            return true
        }
        
        return ApplicationDelegateProxy.shared.application(app, open: url, options: options)
    }
    
    /*
     * Called when the app was launched with an activity, including Universal Links.
     * Feel free to add additional processing here, but if you want the App API to support
     * tracking app url opens, make sure to keep this call
     */
    func application(_ application: UIApplication, continue userActivity: NSUserActivity, restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void) -> Bool {
        
        if let url = userActivity.webpageURL {
            if(FronteggAuth.shared.handleOpenUrl(url)){
                return true
            }
        }
        return ApplicationDelegateProxy.shared.application(application, continue: userActivity, restorationHandler: restorationHandler)
    }
}
```

### Config iOS associated domain

Configuring your iOS associated domain is required for Magic Link authentication / Reset Password / Activate Account.

In order to add your iOS associated domain to your Frontegg application, you will need to update in each of your
integrated Frontegg Environments the iOS associated domain that you would like to use with that Environment. Send a POST
request to `https://api.frontegg.com/vendors/resources/associated-domains/v1/ios` with the following payload:

```
{
    “appId”:[YOUR_ASSOCIATED_DOMAIN]
}
```

In order to use our API’s, follow [this guide](‘https://docs.frontegg.com/reference/getting-started-with-your-api’) to
generate a vendor token.

### Setup Android Project

## Setup Android Project

### Set minimum sdk version

To set up your Android minimum sdk version, open root gradle file at`android/variables.gradle`,

Modify the minSdkVersion to 26:

```groovy
ext {
 minSdkVersion = 26
 ...
}
```

### Configure build config fields

To set up your Android application on to communicate with Frontegg, you have to add `buildConfigField` property the
gradle `android/app/build.gradle`.
This property will store frontegg hostname (without https) and client id from previous step:

```groovy

def fronteggDomain = "FRONTEGG_DOMAIN_HOST.com" // without protocol https://
def fronteggClientId = "FRONTEGG_CLIENT_ID"

android {
    defaultConfig {

        manifestPlaceholders = [
                "package_name" : applicationId,
                "frontegg_domain" : fronteggDomain,
                "frontegg_client_id": fronteggClientId
        ]
    }
    
    
}
```

Add bundleConfig=true if not exists inside the android section inside the app gradle `android/app/build.gradle`

```groovy
android {
  buildFeatures {
    buildConfig = true
  }
}
```

### Add permissions to AndroidManifest.xml

Add `INTERNET` permission to the app's manifest file.

```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
```

### Config Android AssetLinks

Configuring your Android `AssetLinks` is required for Magic Link authentication / Reset Password / Activate Account /
login with IdPs.

To add your `AssetLinks` to your Frontegg application, you will need to update in each of your integrated Frontegg
Environments the `AssetLinks` that you would like to use with that Environment. Send a POST request
to `https://api.frontegg.com/vendors/resources/associated-domains/v1/android` with the following payload:

```
{
    "packageName": "YOUR_APPLICATION_PACKAGE_NAME",
    "sha256CertFingerprints": ["YOUR_KEYSTORE_CERT_FINGERPRINTS"]
}
```

Each Android app has multiple certificate fingerprint, to get your `DEBUG` sha256CertFingerprint you have to run the
following command:

For Debug mode, run the following command and copy the `SHA-256` value

NOTE: make sure to choose the Variant and Config equals to `debug`

```bash
./gradlew signingReport

###################
#  Example Output:
###################

#  Variant: debug
#  Config: debug
#  Store: /Users/davidfrontegg/.android/debug.keystore
#  Alias: AndroidDebugKey
#  MD5: 25:F5:99:23:FC:12:CA:10:8C:43:F4:02:7D:AD:DC:B6
#  SHA1: FC:3C:88:D6:BF:4E:62:2E:F0:24:1D:DB:D7:15:36:D6:3E:14:84:50
#  SHA-256: D9:6B:4A:FD:62:45:81:65:98:4D:5C:8C:A0:68:7B:7B:A5:31:BD:2B:9B:48:D9:CF:20:AE:56:FD:90:C1:C5:EE
#  Valid until: Tuesday, 18 June 2052

```

For Release mode, Extract the SHA256 using keytool from your `Release` keystore file:

```bash
keytool -list -v -keystore /PATH/file.jks -alias YourAlias -storepass *** -keypass ***
```

In order to use our API’s, follow [this guide](https://docs.frontegg.com/reference/getting-started-with-your-api) to
generate a vendor token.


### Enabling Chrome Custom Tabs for Social Login

To enable social login via Chrome Custom Tabs in Android application, modify your `capacitor.config.ts` file and set the useChromeCustomTabs flag to true . By default, the SDK uses the Chrome browser for social login.

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
 
      plugins: {
        FronteggNative:{
          baseUrl: 'https://{FRONTEGG_DOMAIN_HOST.com}',
          clientId: '{FRONTEGG_CLIENT_ID}',
          
          useChromeCustomTabs: true
        }
      }
    };
    
    export default config;
 ```


## Angular Usages

### Integrate Frontegg:

Open the `src/app/app.module.ts` file and add the following line to the before `@NgModule` section:

```typescript
import { FronteggService } from '@frontegg/ionic-capacitor';

@NgModule({
  // ...
  providers: [ {
    provide: 'Frontegg',
    useValue: new FronteggService(),
  } ]
  // ...
})
```

### Protect Routes:

1. Create AuthGuard file `src/app/auth.guard.ts`:

    ```typescript
    import { CanActivateFn } from '@angular/router';
    import { Inject, Injectable } from '@angular/core';
    import { FronteggService } from '@frontegg/ionic-capacitor';
    
    
    @Injectable({
      providedIn: 'root'
    })
    export class AuthGuard {
    
      constructor(@Inject('Frontegg') private fronteggService: FronteggService) {
    
        /**
         * Listens to $isAuthenticated changes
         * Reload the page to trigger canActivate function again
         */
        this.fronteggService.$isAuthenticated.subscribe(async () => {
          window.location.reload()
        });
   
       /**
         * Listens to application visibility changes
         * Reload the page to trigger canActivate
         * when application returns from login page without authentication
         */
        document.addEventListener('visibilitychange', () => {
          if (document.visibilityState === 'visible' && !this.fronteggService.getState().isAuthenticated) {
            window.location.reload()
          }
        });
      }
    
      /**
       * Wait for loader to finish
       * @private
       */
      private waitForLoader() {
        return new Promise((resolve) => {
          const unsubscribe = this.fronteggService.$isLoading
            .subscribe((isLoading) => {
              if (!isLoading) {
                resolve(true);
                unsubscribe();
              }
            });
        })
      }
    
      /**
       * Navigate to login page if user is not authenticated
       * @private
       */
      private async navigateToLoginIfNeeded(): Promise<boolean> {
        const { isAuthenticated } = this.fronteggService.getState();
        if (!isAuthenticated) {
          await this.fronteggService.login()
          return false /** prevent navigation */
        }
        return true /** activate navigation */
      }
    
    
      canActivate: CanActivateFn = () => {
        const { showLoader } = this.fronteggService.getState();
    
        if (!showLoader) {
          /**
           * if showLoader false
           * check if user is authenticated
           */
          return this.navigateToLoginIfNeeded()
        }
    
        /**
         * if showLoader true
         * wait for loader to finish and then
         * check if user is authenticated
         */
        return new Promise<boolean>(async (resolve) => {
          await this.waitForLoader()
          const activated = await this.navigateToLoginIfNeeded()
          resolve(activated)
        })
      }
    }
    
    
    ```

2. Open the `src/app-routing.module.ts` file and add wrap the app routes with loadChildren and apply CanActivate guard:

```typescript
import { AuthGuard } from './auth.guard';

const routes: Routes = [
  {
    path: '',
    canActivate: [ AuthGuard ],
    loadChildren: () => import('./tabs/tabs.module').then(m => m.TabsPageModule)
  },
];
```

### Get Logged In User

Find full example under `example/src/app/tab1` files.

```typescript
import { Inject } from '@angular/core';
import { FronteggService, FronteggState } from '@frontegg/ionic-capacitor';

@Component({
  /** .... */
})
export class MyPage implements OnInit {

  constructor(private ngZone: NgZone, @Inject('Frontegg') private fronteggService: FronteggService) {
  }

  user: FronteggState['user'] = null
  accessToken: string | null = null

  ngOnInit() {
    const { user, accessToken } = this.fronteggService.getState();
    this.user = user;
    this.user = accessToken;

    this.fronteggService.$user.subscribe((user) => {
      console.log('change user', user)
      this.ngZone.run(() => this.user = user)
    })
    this.fronteggService.$accessToken.subscribe((accessToken) => {
      console.log('change accessToken', accessToken)
      this.ngZone.run(() => this.accessToken = accessToken)
    })
  }
}
```

### Switch Tenant

Find full example under `example/src/app/tab2` files.

```typescript
import { Inject } from '@angular/core';
import { FronteggService, FronteggState } from '@frontegg/ionic-capacitor';

@Component({
  /** .... */
})
export class MyPage implements OnInit {

  constructor(private ngZone: NgZone, @Inject('Frontegg') private fronteggService: FronteggService) {
  }

  user: FronteggState['user'] = null
  accessToken: string | null = null

  ngOnInit() {
    const { user } = this.fronteggService.getState();
    this.user = user;
    this.fronteggService.$user.subscribe((user) => {
      this.ngZone.run(() => this.user = user)
    })
  }


  switchTenant(tenantId: string) {
    this.fronteggService.switchTenant(tenantId)
  }
}
```

## Embedded Webview vs Hosted

Frontegg SDK supports two authentication methods:

- Embedded Webview
- Hosted Webview
  - `iOS`: ASWebAuthenticationSession
  - `Android`: Custom Chrome Tab

By default, Frontegg SDK will use Embedded Webview.

### Enable hosted webview in iOS Platform

To use ASWebAuthenticationSession you have to set `embeddedMode` to `NO` in `Frontegg.plist` file:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN">
<plist version="1.0">
    <dict>
        <key>lateInit</key>
        <true/>
        <!-- START -->
        <key>embeddedMode</key>
        <false/>
        <!-- END -->
    </dict>
</plist>
```

### Enable hosted webview in Android Platform

to use Custom Chrome Tab you have to set disable embedded activity by adding below code to
the application manifest:

```xml

<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          xmlns:tools="http://schemas.android.com/tools">
    <application>
        <!-- ... -->

        <activity android:name="com.frontegg.android.EmbeddedAuthActivity" tools:replace="android:enabled"
                  android:enabled="false"/>
        <activity android:name="com.frontegg.android.HostedAuthActivity" tools:replace="android:enabled"
                  android:enabled="true"/>

        <!-- ... -->
    </application>
</manifest>
```

## Multi-Region Support

This guide outlines the steps to configure your Ionic application to support multiple regions.

### Step 1: Add regions to your Frontegg configuration

Add `region` to your Frontegg configuration in `capacitor.config.ts` file:

Find example code in [example/capacitor.config.ts](example/capacitor.config.ts) file.

```typescript
import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  /*...*/
  plugins: {

    /*...*/

    FronteggNative: {
      
      /** Remove baseUrl and clientId from here */
      // baseUrl: 'https://{FRONTEGG_DOMAIN_HOST.com}',
      // clientId: '{FRONTEGG_CLIENT_ID}',
      
      regions: [ {
        key: 'REGION_1_KEY',
        baseUrl: 'https://region1.forntegg.com',
        clientId: 'REGION_1_CLIEND_ID',
      }, {
        key: 'REGION_2_KEY',
        baseUrl: 'https://region2.forntegg.com',
        clientId: 'REGION_2_CLIEND_ID',
      } ]
    }
  }
};

export default config;

```

### Step 2: Create region guard service

Create region guard service, this guard will prevent application init if region not selected,
and checks if specific region selected by getting the native state from the Frontegg SDK.
If the region not exists, the guard will redirect to region selector page.

Find example code in [example/src/app/region.guard.ts](example/src/app/region.guard.ts) file.

```typescript
import { CanActivateFn, Router } from '@angular/router';
import { Inject, Injectable } from '@angular/core';
import { FronteggService } from '@frontegg/ionic-capacitor';

@Injectable({
  providedIn: 'root'
})
export class RegionGuard {
  constructor(@Inject('Frontegg') private fronteggService: FronteggService, private router: Router) {
    /**
     * Listens to $isAuthenticated changes
     * Reload the page to trigger canActivate function again
     */
    this.fronteggService.$selectedRegion.subscribe(async () => {
      window.location.reload()
    });
  }

  canActivate: CanActivateFn = async () => {
    const { isRegional } = await this.fronteggService.getConstants();
    const nativeState = await this.fronteggService.getNativeState()

    if (!isRegional || nativeState.selectedRegion != null) {
      /**
       * region already selected, activate navigation
       */
      return true
    }

    /**
     * region not selected, redirect to region selector page
     */
    return this.router.navigate([ '/select-region' ])
  }
}
```

### Step 3: Add region guard to application router

Find example code in [example/src/app/app-routing.module.ts](example/src/app/app-routing.module.ts) file.

```typescript
const routes: Routes = [
  {
    path: '',
    canActivate: [ RegionGuard ],
    children: [
      /**
       * Wrap all routes with region guard
       * to redirect to region selector page
       * if region not exists
       */
      {
        path: '',
        canActivate: [ AuthGuard ],
        loadChildren: () => import('./tabs/tabs.module').then(m => m.TabsPageModule)
      },
    ]
  }, {
    /**
     * Add region selector page
     * to select region if not exists
     */
    path: 'select-region',
    component: SelectRegionComponent
  }
];
```

### Step 4: Setup multi-region support for iOS Platform
 
Following guide outlines the steps to configure iOS application to support multiple regions.

**First, Adjust Your Frontegg.plist File for Multiple Regions:**
  - Remove the existing `baseUrl` and `clientId` keys.
  - Add a new boolean property, `lateInit`, and set it to `true`.
    
    Example Frontegg.plist Structure:
    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
    <plist version="1.0">
    <dict>  
      <key>lateInit</key>
      <true/>
    </dict>
    </plist>
    ```
**Secondly, Add Associated Domains for Each Region:**

Configure the associated domains for each region in your application's settings. This step is crucial for correct API routing and authentication.
Follow the guide [Config iOS Associated Domain](#config-ios-associated-domain) to add your iOS associated domain to your Frontegg application.


### Step 5: Setup multi-region support for Android Platform

Following guide outlines the steps to configure Android application to support multiple regions.

**First, Modify the Build.gradle file**
  - remove buildConfigFields from your build.gradle file: `legacy`

  ```groovy
  
  android {
    //  remove this lines:
    //  buildConfigField "String", 'FRONTEGG_DOMAIN', "\"$fronteggDomain\""
    //  buildConfigField "String", 'FRONTEGG_CLIENT_ID', "\"$fronteggClientId\""
  }
  ```

**Secondly, Add AssetLinks for Each Region:**

For each region, configuring your Android `AssetLinks`. This is vital for proper API routing and authentication.
Follow [Config Android AssetLinks](#config-android-assetlinks) to add your Android domains to your Frontegg application.

**Lastly, Add Intent-Filter in Manifest.xml:**

The first domain will be placed automatically in the `AndroidManifest.xml` file. For each additional region, you will
need to add an `intent-filter`.
Replace `${FRONTEGG_DOMAIN_2}` with the second domain from the previous step.

NOTE: if you are using `Custom Chrome Tab` you have to use `android:name` `com.frontegg.android.HostedAuthActivity` instead of `com.frontegg.android.EmbeddedAuthActivity`

```xml

<application>
    <activity android:exported="true" android:name="com.frontegg.android.EmbeddedAuthActivity"
              tools:node="merge">
        <intent-filter android:autoVerify="true">
            <action android:name="android.intent.action.VIEW" />

            <category android:name="android.intent.category.DEFAULT" />
            <category android:name="android.intent.category.BROWSABLE" />

            <data android:scheme="https" />
            <!-- DO NOT COMBINE THE FOLLOWING LINES INTO ONE LINE OR SPLIT TO MULTIPLE -->
            <data android:host="${FRONTEGG_DOMAIN_2}"
                  android:pathPrefix="/oauth/account/activate" />
            <data android:host="${FRONTEGG_DOMAIN_2}"
                  android:pathPrefix="/oauth/account/invitation/accept" />
            <data android:host="${FRONTEGG_DOMAIN_2}"
                  android:pathPrefix="/oauth/account/reset-password" />
            <data android:host="${FRONTEGG_DOMAIN_2}"
                  android:pathPrefix="/oauth/account/login/magic-link" />
        </intent-filter>
    </activity>

    <activity android:exported="true" android:name="com.frontegg.android.AuthenticationActivity"
              tools:node="merge">
            <intent-filter android:autoVerify="true">
                <action android:name="android.intent.action.VIEW" />

                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />

                <!-- DONT NOT COMBINE THE FOLLOWING LINES INTO ONE LINE-->
                <data
                    android:host="${FRONTEGG_DOMAIN_2}"
                    android:pathPrefix="/oauth/account/redirect/android/${package_name}"
                    android:scheme="https" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />

                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />

                <data
                    android:host="${FRONTEGG_DOMAIN_2}"
                    android:scheme="${package_name}" />
            </intent-filter>
    </activity>
</application>
```

## Multi-apps Support

This guide outlines the steps to configure your application to support multiple applications.

### Step 1: Add application id to your Frontegg configuration

Add `applicationId` to your Frontegg configuration in `capacitor.config.ts` file:

Find example code in [example/capacitor.config.ts](example/capacitor.config.ts) file.


```typescript
import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
/*...*/
plugins: {

    /*...*/

    FronteggNative: {
        baseUrl: 'https://{FRONTEGG_DOMAIN_HOST.com}',
        clientId: '{FRONTEGG_CLIENT_ID}',
        applicationId: '{FRONTEGG_APPLICATION_ID}',
    }
}
};

export default config;
```

Or add `applicationId` to the `regions`:

```typescript
import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  /*...*/
  plugins: {

    /*...*/

    FronteggNative: {
      
      /** Remove baseUrl and clientId from here */
      // baseUrl: 'https://{FRONTEGG_DOMAIN_HOST.com}',
      // clientId: '{FRONTEGG_CLIENT_ID}',
      // applicationId: '{FRONTEGG_APPLICATION_ID}',
      
      regions: [ {
        key: 'REGION_1_KEY',
        baseUrl: 'https://region1.forntegg.com',
        clientId: 'REGION_1_CLIEND_ID', 
        applicationId: '{FRONTEGG_REGION_1_APPLICATION_ID}'
      }, {
        key: 'REGION_2_KEY',
        baseUrl: 'https://region2.forntegg.com',
        clientId: 'REGION_2_CLIEND_ID', 
        applicationId: '{FRONTEGG_REGION_2_APPLICATION_ID}',
      } ]
    }
  }
};

export default config;
```
