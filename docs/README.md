<p align="center">
  <img src="https://raw.githubusercontent.com/frontegg/frontegg-ionic-capacitor/master/images/frontegg-ionic.png" alt="Frontegg Ionic Capacitor SDK" width="640" />
</p>

<h1 align="center">Frontegg Ionic Capacitor SDK</h1>

<p align="center">
  <strong>Authentication and user management for your Ionic app — one package, both platforms.</strong>
</p>

<p align="center">
  <a href="https://www.npmjs.com/package/@frontegg/ionic-capacitor"><img src="https://img.shields.io/npm/v/@frontegg/ionic-capacitor?label=npm&color=6c47ff" alt="npm version" /></a>
  <img src="https://img.shields.io/badge/iOS-14%2B-lightgrey" alt="iOS 14+" />
  <img src="https://img.shields.io/badge/Android-API%2026%2B-3ddc84" alt="Android API 26+" />
  <img src="https://img.shields.io/badge/Capacitor-ready-119eff" alt="Capacitor" />
  <a href="https://github.com/frontegg/frontegg-ionic-capacitor/blob/master/LICENSE"><img src="https://img.shields.io/github/license/frontegg/frontegg-ionic-capacitor?color=blue" alt="Licence" /></a>
</p>

---

[Frontegg](https://frontegg.com/) is a self-served user management platform for modern SaaS
applications. Drop this SDK in and your app gets a production login screen, a live session, and a
user object — without you writing an auth flow or touching a token.

| | |
| --- | --- |
| **Native login on both platforms** | Frontegg's login box through Capacitor, backed by the native iOS and Android SDKs |
| **Every method your tenants need** | Email, social, SSO, magic link, passkeys, MFA and step-up |
| **Sessions that stay alive** | Tokens refresh in the background |
| **Built for multi-tenant SaaS** | Multi-tenancy, RBAC, entitlements and multi-region support |

---

## Install

If your Ionic project does not use Capacitor yet:

```bash
ionic integrations enable capacitor
```

Then add the SDK:

```bash
npm install @frontegg/ionic-capacitor
```

> Requires **iOS 14+** and **Android API 26+**.

## Quick start

**1 · Allow the redirect URLs.** In the Frontegg Portal, under **[ENVIRONMENT] → Authentication →
Login method**, turn hosted login on and add one set per platform:

```
# iOS
{{IOS_BUNDLE_IDENTIFIER}}://{{FRONTEGG_BASE_URL}}/ios/oauth/callback

# Android
{{ANDROID_PACKAGE_NAME}}://{{FRONTEGG_BASE_URL}}/android/oauth/callback
https://{{FRONTEGG_BASE_URL}}/oauth/account/redirect/android/{{ANDROID_PACKAGE_NAME}}
```

**2 · Configure the native projects.** iOS reads a `Frontegg.plist`; Android takes its domain and
client ID from `build.gradle`. Both are covered step by step in the
[Get Started guide](https://ionic-capacitor-guide.frontegg.com/#/getting-started) — this is the one
part that is not TypeScript, and it differs per platform.

**3 · Provide the service** in `src/app/app.module.ts`.

```typescript
import { FronteggService } from '@frontegg/ionic-capacitor';

@NgModule({
  // ...
  providers: [{
    provide: 'Frontegg',
    useValue: new FronteggService(),
  }],
})
export class AppModule {}
```

**4 · Use it** — read state, or start a login.

```typescript
import { Inject, Injectable } from '@angular/core';
import { FronteggService } from '@frontegg/ionic-capacitor';

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(@Inject('Frontegg') private frontegg: FronteggService) {}

  async loginIfNeeded(): Promise<void> {
    const { isAuthenticated } = this.frontegg.getState();
    if (!isAuthenticated) {
      await this.frontegg.login();
    }
  }
}
```

The [Usage guide](https://ionic-capacitor-guide.frontegg.com/#/usage) builds this into a full route
guard.

## Documentation

| Guide | What it covers |
| --- | --- |
| [Get Started](https://ionic-capacitor-guide.frontegg.com/#/getting-started) | Requirements, environment prep, iOS and Android setup |
| [Setup](https://ionic-capacitor-guide.frontegg.com/#/setup) | Detailed configuration |
| [Usage Examples](https://ionic-capacitor-guide.frontegg.com/#/usage) | Providing the service, route guards, login flows |
| [API Reference](https://ionic-capacitor-guide.frontegg.com/#/api) | Every method, observable and type the SDK exports |
| [Advanced Topics](https://ionic-capacitor-guide.frontegg.com/#/advanced) | Complex integration scenarios |

Full platform documentation lives at [developers.frontegg.com](https://developers.frontegg.com).

## Example app

A complete integration you can run:
[example](https://github.com/frontegg/frontegg-ionic-capacitor/tree/master/example).

## Support

No Frontegg account yet? [Sign up free](https://portal.us.frontegg.com/signup).

Questions, or something broken? Reach the team at
[support.frontegg.com](https://support.frontegg.com/frontegg/directories) or
[open an issue](https://github.com/frontegg/frontegg-ionic-capacitor/issues).

Licensed under the [LICENSE](https://github.com/frontegg/frontegg-ionic-capacitor/blob/master/LICENSE) in this repository.
