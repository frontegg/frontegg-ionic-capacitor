# Changelog

## v3.0.0

Breaking release: `@frontegg/ionic-capacitor` now targets **Capacitor 8**, and on iOS it is consumed through **Swift Package Manager** instead of CocoaPods. Apps staying on Capacitor 5 or 7 should stay on `2.x`. Upgrade steps are in [docs/migrating-to-v3.md](docs/migrating-to-v3.md). ([#113](https://github.com/frontegg/frontegg-ionic-capacitor/pull/113))

Breaking changes:

| | v2 | v3 |
|---|---|---|
| Capacitor | 5 / 7 | **8+** |
| iOS dependency manager | CocoaPods | **Swift Package Manager** |
| iOS deployment target | 14 | **15** |
| iOS URL handling | `AppDelegate` | **`SceneDelegate`** |
| Android compile / target SDK | 35 | **36** |
| Android Gradle plugin / Gradle | 8.7.2 / 8.11.1 | **8.13.0 / 8.14.3** |
| Java | 17 | **21** |
| Node (build time) | 18 | **22+** |

Minimum Android SDK is unchanged at 26.

- Move iOS URL handling to `SceneDelegate`. Capacitor 8 declares a `UIScene` manifest, so UIKit stops calling the `AppDelegate` URL methods and magic-link, password-reset and SSO callbacks silently stop arriving. The [setup guide](docs/setup.md#handle-open-app-with-url-for-ios) has the `SceneDelegate` to copy.
- Entitlements are enabled on both platforms, so `loadEntitlements`, `getFeatureEntitlement` and `getPermissionEntitlement` resolve against your environment.
- `useAssetLinks` and `useChromeCustomTabs` default to `false`, matching the native SDKs. `useAssetLinks` is now honored on iOS too; set it explicitly if you rely on App Link callbacks.

Native SDKs:

- iOS: FronteggSwift 1.3.21, through Swift Package Manager.
- Android: Frontegg Android SDK 1.3.41. ([#111](https://github.com/frontegg/frontegg-ionic-capacitor/pull/111))

Bug fixes:

- Android: the plugin compiles again against the current native SDK (`FronteggApp.init` / `initWithRegions` signatures), and `stepUp` / `isSteppedUp` work through a Kotlin bridge. ([#113](https://github.com/frontegg/frontegg-ionic-capacitor/pull/113))
- Android: a destroyed bridge no longer keeps reading SDK state. ([#113](https://github.com/frontegg/frontegg-ionic-capacitor/pull/113))
- iOS: `initWithRegion` resolves instead of hanging. (FR-25945 — [#92](https://github.com/frontegg/frontegg-ionic-capacitor/pull/92))
- Android: a failed token refresh no longer logs the user out; `refreshToken` resolves `{ success }`. (FR-25946 — [#93](https://github.com/frontegg/frontegg-ionic-capacitor/pull/93))
- `login`, `directLogin` and `switchTenant` reject on failure, and `logout` can be awaited. (FR-25947 — [#94](https://github.com/frontegg/frontegg-ionic-capacitor/pull/94))
- A configuration error in `load()` no longer terminates the app. (FR-25948 — [#95](https://github.com/frontegg/frontegg-ionic-capacitor/pull/95))
- iOS example and docs: custom-scheme login callbacks are no longer dropped, launch URLs wait for the SDK to initialize, and only one Capacitor bridge is created per scene. ([#113](https://github.com/frontegg/frontegg-ionic-capacitor/pull/113))

CI:

- iOS and Android mock-server E2E suites run on every push and gate the build. ([#113](https://github.com/frontegg/frontegg-ionic-capacitor/pull/113))

## v2.1.2 and earlier

See [GitHub Releases](https://github.com/frontegg/frontegg-ionic-capacitor/releases).
