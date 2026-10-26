# Authentication and usage

The Frontegg Ionic Capacitor SDK supports multiple authentication flows to improve the user experience on iOS and Android:

* **Embedded webview**: A customizable in-app webview login experience, which is enabled by default.
* **Hosted Webview**: A secure, system-level authentication flow leveraging:
  - **iOS**: [`ASWebAuthenticationSession`](https://developer.apple.com/documentation/authenticationservices/aswebauthenticationsession) for seamless integration with native browser-based login.
  - **Android**: Chrome Custom Tabs for social login and strong session isolation.

## Enable hosted webview in iOS Platform

To use `ASWebAuthenticationSession` on iOS, set `embeddedMode` to `false` in your `Frontegg.plist` file:

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

## Configure AndroidManifest for hosted authentication

To use **Custom Chrome Tab** on Android, disable the embedded activity by adding the following configuration to your `AndroidManifest.xml` file:

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

## Enable Chrome custom tabs in Capacitor configuration

To enable social login using Chrome Custom Tabs in your Android application, update the `capacitor.config.ts` file as shown below.

1. Open the `capacitor.config.ts` file.
2. Add the following content:

```groovy
   import { CapacitorConfig } from '@capacitor/cli';

   const config: CapacitorConfig = {
     appId: '{{FRONTEGG_APPLICATION_ID}}',
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
         baseUrl: '{{FRONTEGG_DOMAIN}}',
         clientId: '{{FRONTEGG_CLIENT_ID}}',
         
         useChromeCustomTabs: true
       }
     }
   };
   
   export default config;
```

4. After saving your changes, sync the project to apply the configuration.

**NOTE**: By default, the Frontegg SDK will use the Chrome browser for social login when this flag is set to `true`.


## Angular setup

### Integrate Frontegg

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

### Protect routes

To guard routes using Frontegg authentication:

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

2. Open the `src/app-routing.module.ts` file and add wrap the app routes with `loadChildren` and apply `CanActivate` guard:

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

### Get authenticated user

Use the following code to access the authenticated user details and access token:

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

### Switch account (tenant)

You can switch user tenants by calling `switchTenant` with the desired tenant ID:

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

### Entitlements

The SDK exposes the underlying Frontegg entitlements API so you can gate features and check permissions on-device. Call `loadEntitlements()` once after a successful login, and again after `switchTenant()` — the tenant context determines which features and permissions resolve.

```typescript
import { Inject } from '@angular/core';
import { FronteggService } from '@frontegg/ionic-capacitor';

@Component({
  /** .... */
})
export class MyPage {

  constructor(@Inject('Frontegg') private fronteggService: FronteggService) {}

  async ngOnInit() {
    // After login (or after switchTenant):
    await this.fronteggService.loadEntitlements();

    const feature = await this.fronteggService.getFeatureEntitlement('beta-dashboard');
    if (feature.isEntitled) {
      // show the gated feature
    } else {
      console.log('Not entitled because:', feature.justification);
    }

    const permission = await this.fronteggService.getPermissionEntitlement('billing.read');
    // permission.isEntitled / permission.justification
  }
}
```

`justification` values returned when `isEntitled` is `false`:

| Justification | Meaning |
|---|---|
| `ENTITLEMENTS_DISABLED` | Entitlements are disabled for this account. |
| `ENTITLEMENTS_NOT_LOADED` | `loadEntitlements()` was not called (or failed). |
| `MISSING_FEATURE` / `MISSING_PERMISSION` | The user does not hold this key. |
| `NOT_AUTHENTICATED` (iOS) | No user is currently signed in. |

### Get a valid access token after app resume

When the app returns to the foreground the cached access token may have expired. Call
`getOrRefreshAccessToken()` before calling your API instead of reading `accessToken` from the state:

```typescript
import { App } from '@capacitor/app';

App.addListener('resume', async () => {
  const accessToken = await this.fronteggService.getOrRefreshAccessToken();
  if (accessToken === null) {
    // No session, or the refresh token was rejected: sign the user in again.
    await this.fronteggService.login();
    return;
  }
  // Use accessToken for your API calls.
});
```

The method waits up to 20 seconds for any in-flight initialization or refresh, and rejects if one is still running
after that. It then returns the current token when it is valid for more than 15 seconds, and refreshes it otherwise:

- No session, or the server rejected the refresh token: resolves `null`.
- Android, multi-region app with no region selected yet: resolves `null` right away.
- The device is offline and offline mode is enabled (`enableOfflineMode` in `Frontegg.plist` on iOS, the
  `FRONTEGG_ENABLE_OFFLINE_MODE` build config field on Android; see
  [Offline token fallback](setup.md#offline-token-fallback)): resolves the cached access token, which may be expired.
  Android returns it without trying to refresh; iOS tries one refresh first.
- The device is offline and offline mode is disabled: rejects. Android rejects right away; iOS retries the refresh a few
  times first.
- Any other refresh failure: rejects, so you can retry later without signing the user out.

## Admin Portal (Beta)

Open the embedded Frontegg Admin Portal for authenticated users:

```typescript
await this.fronteggService.openAdminPortal();
```

See [Advanced Topics](advanced.md#admin-portal-beta) for prerequisites and platform behavior.
