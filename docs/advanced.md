# Advanced options

In this guide, you'll find an overview and best practices for enabling advanced features like passkeys and multi-app configurations.

## Multi-region support

This guide outlines the steps to configure your Ionic application to support multiple regions.

### Add regions to your Frontegg configuration

Add `region` to your Frontegg configuration in `capacitor.config.ts` file:

```typescript
import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  /*...*/
  plugins: {

    /*...*/

    FronteggNative: {
      
      /** Remove baseUrl and clientId from here */
      // baseUrl: "https://{{FRONTEGG_BASE_URL}}",
      // clientId: "{{FRONTEGG_CLIENT_ID}}",
      
      regions: [ {
        key: 'REGION_1_KEY',
        baseUrl: 'https://region1.forntegg.com',
        clientId: 'REGION_1_CLIEND_ID',
        applicationId: "{{REGION_1_APPLICATION_ID}}"
      }, {
        key: 'REGION_2_KEY',
        baseUrl: 'https://region2.forntegg.com',
        clientId: 'REGION_2_CLIEND_ID',
        applicationId: "{{REGION_2_APPLICATION_ID}}"
      } ]
    }
  }
};

export default config;

```

### Create region guard service

Create a region guard service to prevent the app from initializing if no region is selected.  
The guard checks the native state from the Frontegg SDK and, if no region is found, redirects the user to the region selector page:

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

### Add region guard to application router

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

### Setup multi-region support for iOS platform
 
1. Open your `Frontegg.plist` file.
2. Remove the existing `baseUrl` and `clientId` keys.
3. Add a new boolean property called `lateInit` and set it to `true`.

Example `Frontegg.plist`:
    
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

4. In your Frontegg Portal, navigate to the environment for each region.
5. Add the corresponding iOS associated domain to each environment's settings.
6. Follow the instructions in the **Config iOS Associated Domain** to complete this step. This ensures correct routing and secure authentication for each region.

### Setup multi-region support for Android platform

1. Open your `android/app/build.gradle` file.
2. Remove any existing `buildConfigField` entries related to Frontegg:

  ```groovy
  
  android {
    //  remove this lines:
    //  buildConfigField "String", 'FRONTEGG_DOMAIN', "\"$fronteggDomain\""
    //  buildConfigField "String", 'FRONTEGG_CLIENT_ID', "\"$fronteggClientId\""
  }
  ```

3. Set up Android AssetLinks for every domain associated with your regional environments. This ensures proper routing and authentication.
4. Follow the instructions under **Config Android AssetLinks** to complete this step.
5. Update your `AndroidManifest.xml` file. The first domain is automatically configured.
6. For each additional region, manually add an `<intent-filter>` block.
7. If using **Custom Chrome Tabs**, ensure the activity name is `com.frontegg.android.HostedAuthActivity`.

Example:

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
            <data android:host="{{FRONTEGG_DOMAIN_2}}"
                  android:pathPrefix="/oauth/account/activate" />
            <data android:host="{{FRONTEGG_DOMAIN_2}}"
                  android:pathPrefix="/oauth/account/invitation/accept" />
            <data android:host="{{FRONTEGG_DOMAIN_2}}"
                  android:pathPrefix="/oauth/account/reset-password" />
            <data android:host="{{FRONTEGG_DOMAIN_2}}"
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
                    android:host="{{FRONTEGG_DOMAIN_2}}"
                    android:pathPrefix="/oauth/account/redirect/android/{{ANDROID_PACKAGE_NAME}}"
                    android:scheme="https" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />

                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />

                <data
                    android:host="{{FRONTEGG_DOMAIN_2}}"
                    android:scheme="{{ANDROID_PACKAGE_NAME}}" />
            </intent-filter>
    </activity>
</application>
```

## Multi-apps support

Configure `applicationId` in `capacitor.config.ts`. You can add the applicationId in one of two ways:

### Option 1: Add `applicationId` directly to the `FronteggNative` config

```typescript
import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
/*...*/
plugins: {

    /*...*/

    FronteggNative: {
        baseUrl: "https://{{FRONTEGG_DOMAIN}}",
        clientId: "{{FRONTEGG_CLIENT_ID}}",
        applicationId: "{{FRONTEGG_APPLICATION_ID}}",
    }
}
};

export default config;
```

### Option 2: Add `applicationId` per region (for multi-region support)

If your app supports multiple environments or regions, define `applicationId` inside each region object:

```