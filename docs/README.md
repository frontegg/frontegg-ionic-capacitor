# Frontegg Ionic SDK
![Frontegg_Ionic_SDK](/images/frontegg-ionic.png)

Welcome to the official **Frontegg Ionic SDK** — your all-in-one solution for
integrating authentication and user management into your Ionic mobile
app. [Frontegg](https://frontegg.com/) is a self-served user management platform, built for modern
SaaS applications. Easily implement authentication, SSO, RBAC, multi-tenancy, and more — all from a
single SDK.

## 📚 Documentation

This repository includes:

- A [Get Started](https://ionic-capacitor-guide.frontegg.com/#/getting-started) guide for quick integration
- A [Setup Guide](https://ionic-capacitor-guide.frontegg.com/#/setup) with detailed setup instructions
- [Usage Examples](https://ionic-capacitor-guide.frontegg.com/#/usage) with common implementation patterns
- [Advanced Topics](https://ionic-capacitor-guide.frontegg.com/#/advanced) for complex integration scenarios
- A [Embedded](https://github.com/frontegg/frontegg-ionic-capacitor/tree/master/example) example projects to help you get started quickly

For full documentation, visit the Frontegg Developer Portal:  
🔗 [https://developers.frontegg.com](https://developers.frontegg.com)

---

## 🧩 Entitlements & Admin Portal

The wrapper bridges two native capabilities into your Ionic app:

- **Entitlements** — gate features and permissions on-device with `loadEntitlements()`, `getFeatureEntitlement({ key })`, and `getPermissionEntitlement({ key })` (each resolves to `{ isEntitled, justification }`). See the [Entitlements guide](https://github.com/frontegg/frontegg-ionic-capacitor/blob/master/docs/usage.md#entitlements).
- **Admin Portal** — open the embedded Frontegg Admin Portal for authenticated users with `openAdminPortal()`. It opens through the native iOS/Android token bridge, so users aren't prompted to log in again. See the [Admin Portal guide](https://github.com/frontegg/frontegg-ionic-capacitor/blob/master/docs/advanced.md#admin-portal-beta).

---

## 🔐 Native SDK versions

The Ionic Capacitor wrapper depends on the underlying native SDKs:

- On **Android**, the plugin and example app use `com.frontegg.sdk:android:1.3.34`.
- On **iOS**, the plugin depends on `FronteggSwift` **1.3.10** via CocoaPods.

After upgrading, run `pod install` in your iOS project and rebuild both platforms.

---

## 🧑‍💻 Getting Started with Frontegg

Don't have a Frontegg account yet?  
Sign up here → [https://portal.us.frontegg.com/signup](https://portal.us.frontegg.com/signup)

---

## 💬 Support

Need help? Our team is here for you:  
[https://support.frontegg.com/frontegg/directories](https://support.frontegg.com/frontegg/directories)
