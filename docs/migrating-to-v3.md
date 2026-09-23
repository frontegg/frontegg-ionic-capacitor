# Migrating to v3

v3 is a breaking release. It requires **Capacitor 8**, and on iOS the SDK is now consumed through
**Swift Package Manager** instead of CocoaPods.

If you are staying on Capacitor 5 or 7, stay on `@frontegg/ionic-capacitor@2.x`.

## Breaking changes

| Change | v2 | v3 |
|---|---|---|
| Capacitor | 5 (published) / 7 | **8+** |
| iOS dependency manager | CocoaPods | **Swift Package Manager** |
| iOS deployment target | 14 | **15** |
| iOS URL handling | `AppDelegate` | **`SceneDelegate`** |
| Node (build time) | 18 | **22+** |
| Android compile/target SDK | 35 | **36** |
| Android Gradle plugin / Gradle | 8.7.2 / 8.11.1 | **8.13.0 / 8.14.3** |
| Kotlin | — | **2.x** (follows your app's `kotlin_version`) |
| Java | 17 | **21** |

Minimum Android SDK is unchanged at **26**.

## 1. Upgrade Capacitor

Follow the [Capacitor 8 migration guide](https://capacitorjs.com/docs/updating/8-0), or run:

```bash
npm install @capacitor/core@^8 @capacitor/cli@^8 @capacitor/ios@^8 @capacitor/android@^8
npx cap migrate
```

Then upgrade the SDK:

```bash
npm install @frontegg/ionic-capacitor@^3
```

## 2. Move iOS to Swift Package Manager

CocoaPods is no longer supported. Capacitor 8 uses SPM by default for new apps; for an existing app,
run the Capacitor migration assistant from your project root:

```bash
npx cap spm-migration-assistant
```

It removes the `Podfile`, `Pods` and `App.xcworkspace`, creates `ios/App/CapApp-SPM`, and writes
`ios/debug.xcconfig`. Two steps are left to do in Xcode, which the assistant does not do for you:

1. Open `ios/App/App.xcodeproj`. Choose **File → Add Package Dependencies… → Add Local…**, select
   `ios/App/CapApp-SPM`, and add the `CapApp-SPM` library to the **App** target.
2. Select the project, then **Info → Configurations**, and set `debug.xcconfig` as the
   configuration file for the **Debug** configuration of both the project and the App target.
   Without it `CAPACITOR_DEBUG` is empty and Debug builds have no inspectable webview.

From then on `npx cap sync ios` keeps `CapApp-SPM/Package.swift` up to date with your plugins.

> If your `pod install` was already failing to resolve `FronteggSwift`, that is expected: the 1.3.x
> releases the SDK depends on are not published to the CocoaPods trunk. SPM resolves them from the
> Swift repository.

## 3. Move iOS URL handling to SceneDelegate

Capacitor 8 declares a `UIScene` manifest, and UIKit then stops calling `application(_:open:)` and
`application(_:continue:)` on the app delegate. Frontegg login callbacks handled there **silently
stop arriving** — magic links, password resets and IdP SSO logins never complete.

Move that code into `SceneDelegate.swift` as shown in
[Setup → Handle open app with URL for iOS](setup.md#handle-open-app-with-url-for-ios), and delete the
handlers from `AppDelegate.swift`.

Make sure you also handle the launch URL in `scene(_:willConnectTo:)`. When a link starts a
terminated app, the URL arrives there rather than through the other callbacks, and before the
plugin has initialized Frontegg, so hold it until the first `capacitorViewDidAppear` as the setup
snippet does.

If your `Info.plist` scene configuration sets `UISceneStoryboardFile`, UIKit already creates the
window and its `CAPBridgeViewController`. Only create one in `SceneDelegate` when `window` is still
`nil`, or the app starts a second Capacitor bridge.

## 4. Check two configuration defaults

- **Entitlements are now enabled** on both platforms, so `loadEntitlements`,
  `getFeatureEntitlement` and `getPermissionEntitlement` resolve against your environment. In v2 the
  native default left them switched off.
- **`useAssetLinks` and `useChromeCustomTabs` default to `false`**, which is what the native SDKs
  have always used. Earlier documentation claimed `true`. `useAssetLinks` is now honored on iOS as
  well as Android; if you relied on App Link callbacks, set it explicitly in `capacitor.config.ts`:

  ```typescript
  plugins: {
    FronteggNative: {
      useAssetLinks: true,
    },
  },
  ```

## 5. Rebuild

```bash
npm run build && npx cap sync
```

Android needs **JDK 21**. If your builds run on 17, point `JAVA_HOME` at a 21 install.

## Native SDK versions

v3 ships with FronteggSwift **1.3.21** on iOS and the Frontegg Android SDK **1.3.41**.
