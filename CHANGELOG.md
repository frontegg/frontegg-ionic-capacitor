# Changelog

## Unreleased

Native SDKs are unchanged: iOS FronteggSwift 1.3.24, Android 1.3.43.

Features:

- Passkeys: `loginWithPasskeys()` and `registerPasskeys()`. (FR-25960 — [#123](https://github.com/frontegg/frontegg-ionic-capacitor/pull/123))
- Silent re-authorization: `requestAuthorize({ refreshToken, deviceTokenCookie? })` resolves the signed-in `User`. (FR-25960 — [#123](https://github.com/frontegg/frontegg-ionic-capacitor/pull/123))
- `getOrRefreshAccessToken()` returns an access token valid for more than 15 seconds and refreshes it first when needed. Use it before API calls after the app resumes. `FronteggService` resolves `string | null`; the plugin method resolves `{ accessToken }`. On Android, offline mode for this method is read from the `FRONTEGG_ENABLE_OFFLINE_MODE` BuildConfig field. (FR-25952 — [#123](https://github.com/frontegg/frontegg-ionic-capacitor/pull/123))

Bug fixes:

- `openAdminPortal` (iOS and Android) and `stepUp` (Android) reject with the error code and message in the right order, so JavaScript no longer sees the code as the message. ([#123](https://github.com/frontegg/frontegg-ionic-capacitor/pull/123))
- `selectedRegion` is typed as the `RegionConfig` object the native side sends, instead of `string`. `FronteggService.initWithRegion` now skips the native call when that region is already selected; before, its check never matched. `RegionConfig` is exported. (FR-25950)
- `initWithRegion` rejects with `INVALID_REGION` for a key that is not configured, as React Native and Flutter do. Before, iOS passed it to the SDK and Android resolved without switching. (FR-25950)
- Android: `refreshToken()` waits for the refresh to finish and resolves `{ success }` with its result. Before, it resolved as soon as the refresh started, so the token read right after it could still be the old one. (FR-25950)
- `refreshToken()` is typed `Promise<{ success: boolean }>`, the value it already resolved. (FR-25950)
- Android: `switchTenant` rejects when the switch fails, as on iOS. Before, it resolved `{ success: false }`. (FR-25950)
- Android: `stepUp` and `isSteppedUp` honor `maxAge`, as on iOS. Before, Android ignored it and used the server's default freshness window. (FR-25951)

Changes:

- Android: new dependency `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3`, the version the Android SDK uses. ([#123](https://github.com/frontegg/frontegg-ionic-capacitor/pull/123))

## v3.0.1

Native SDKs:

- iOS: FronteggSwift 1.3.24 or later within 1.3.x.
- Android: Frontegg Android SDK 1.3.43.

Bug fixes:

- Embedded step-up now completes after MFA. The login box went on to the application's App URL instead of returning to the app, so no stepped-up token was issued and `stepUp` never resolved. Step-up now finishes through the hosted login and returns a stepped-up token. No app changes are needed.
- Embedded step-up no longer signs the user out before the MFA challenge appears. The step-up page now opens only once the login box has loaded the user's session.
- iOS: the embedded step-up window no longer closes by itself before the MFA challenge appears.
- iOS: users are no longer signed out when a token refresh response arrives late or is lost on a slow network.
- iOS: embedded sign-in no longer flashes a white frame over the app during the OAuth redirect, and a hex `backgroundColor` in `Frontegg.plist` is now applied.

Changes:

- iOS: when `backgroundColor` is not set, the embedded login web view is now transparent instead of white while it loads, so the system background shows through (white in light mode, black in dark mode). Set `backgroundColor` to `#FFFFFF` in `Frontegg.plist` to keep white.
- iOS: FronteggSwift now ships the Apple privacy manifest that App Store Connect requires from third-party SDKs.

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
