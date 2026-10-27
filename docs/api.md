# Frontegg Ionic Capacitor API Reference

Everything below is exported from `@frontegg/ionic-capacitor`.

```typescript
import { FronteggService, LogLevel } from '@frontegg/ionic-capacitor';
import type { Entitlement, FronteggState, User } from '@frontegg/ionic-capacitor';
```

`FronteggService` is the single entry point. Provide one instance to your app — see
[Usage Examples](https://ionic-capacitor-guide.frontegg.com/#/usage) for the Angular wiring — and
inject it where you need it.

## State

| Method | Returns | Description |
|--------|---------|-------------|
| `getState` | `FronteggState` | The last known state, read synchronously from the in-memory cache. |
| `getNativeState` | `Promise<FronteggState>` | The current state, read from the native SDK. |
| `waitForLoader` | `Promise<boolean>` | Resolves once the SDK has finished loading. Useful before deciding whether to route to a login screen. |

### Observables

Each getter returns an observable of one state field, so you can react to changes without polling.

| Observable | Emits |
|------------|-------|
| `$isAuthenticated` | `boolean` — whether a user is signed in |
| `$user` | `User \| null` — the signed-in user |
| `$accessToken` | `string \| null` — the current access token |
| `$isLoading` | `boolean` — an auth operation is in progress |
| `$refreshingToken` | `boolean` — a token refresh is in flight |
| `$selectedRegion` | `RegionConfig \| null` — the active region (`key`, `baseUrl`, `clientId`, `applicationId`) |
| `$showLoader` | `boolean` — **deprecated**, use `$isLoading` |

## Authentication

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `login` | `loginHint?: string` | `Promise<void>` | Opens the login flow. `loginHint` pre-fills the identifier field. |
| `logout` | None | `Promise<void>` | Signs the user out and clears stored credentials. |
| `refreshToken` | None | `Promise<{ success: boolean }>` | Forces a token refresh and resolves once it has finished. A failed refresh resolves `{ success: false }` and keeps the session. The SDK also refreshes on its own in the background. |
| `getOrRefreshAccessToken` | None | `Promise<string \| null>` | Returns a valid access token, refreshing it first when it expires within 15 seconds. Waits up to 20 seconds for an in-flight initialization or refresh and rejects on timeout. Resolves `null` when there is no session, the refresh token was rejected, or (Android) a multi-region app has no region selected yet. When the device is offline it resolves the cached token if offline mode is enabled (iOS tries one refresh first) and rejects if it is disabled (Android right away, iOS after a few retries). Rejects on any other refresh failure. See [Get a valid access token after app resume](usage.md#get-a-valid-access-token-after-app-resume). **Recommended way to get a token after the app resumes.** |
| `requestAuthorize` | `refreshToken: string`<br>`deviceTokenCookie?: string` | `Promise<User>` | Signs the user in silently, without the login box, using a refresh token from Frontegg identity-server APIs (for example the sign-up response). |

### Direct login

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `directLoginAction` | `type: string`<br>`data: string`<br>`ephemeralSession = true` | `Promise<boolean>` | Signs in through a provider directly, without showing the login page. Resolves `true` immediately if a session already exists. |

`type` selects what `data` means:

| `type` | `data` |
|--------|--------|
| `direct` | A SAML URL request |
| `social-login` | The provider name |
| `custom-social-login` | The provider entity ID |

## Passkeys

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `loginWithPasskeys` | None | `Promise<void>` | Signs in with a passkey stored on the device. Rejects on failure or cancellation. |
| `registerPasskeys` | None | `Promise<void>` | Registers a passkey on this device for the signed-in user. |

See [Passkeys](https://ionic-capacitor-guide.frontegg.com/#/advanced?id=passkeys) for the platform setup.

## Step-up authentication

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `isSteppedUp` | `maxAge?: number` (seconds) | `Promise<boolean>` | Whether the session is currently stepped up. Omit `maxAge` for no age limit. |
| `stepUp` | `maxAge?: number` (seconds) | `Promise<void>` | Starts step-up authentication (MFA or re-auth). |

## Tenants and regions

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `switchTenant` | `tenantId: string` | `Promise<void>` | Switches the active tenant and rejects if the switch fails. IDs come from the user's tenant list. |
| `initWithRegion` | `regionKey: string` | `Promise<void>` | Initialises the SDK against a specific region, for multi-region setups. Rejects with `INVALID_REGION` for a key that is not configured. |

## Entitlements

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `loadEntitlements` | `forceRefresh = false` | `Promise<{ success: boolean }>` | Loads entitlements into the SDK cache. Call after authentication, and again with `forceRefresh` to refetch. |
| `getFeatureEntitlement` | `key: string` | `Promise<Entitlement>` | Entitlement for a feature-flag key, read from the cache. |
| `getPermissionEntitlement` | `key: string` | `Promise<Entitlement>` | Entitlement for a permission key, read from the cache. |

Both getters read the on-device cache, so call `loadEntitlements` first.

## Admin portal

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `openAdminPortal` | None | `Promise<void>` | Opens the Frontegg admin portal. |

## Configuration

| Method | Returns | Description |
|--------|---------|-------------|
| `getConstants` | `Promise<FronteggConstants>` | The configuration the native SDK started with — base URL, client ID and related values. |

## Types

### `FronteggState`

| Field | Type | Description |
|-------|------|-------------|
| `isAuthenticated` | `boolean` | Whether a user is signed in. |
| `user` | `User \| null` | The signed-in user. |
| `accessToken` | `string \| null` | Current access token, a JWT. `null` when signed out. |
| `refreshToken` | `string \| null` | Current refresh token. `null` when signed out. |
| `refreshingToken` | `boolean` | A token refresh is in flight. |
| `isLoading` | `boolean` | An auth operation is in progress. |
| `initializing` | `boolean` | The SDK has not finished starting up. |
| `showLoader` | `boolean` | Convenience flag for rendering a loading state. |
| `isRegional` | `boolean` | Whether the app is configured for multi-region. |
| `selectedRegion` | `RegionConfig \| null` | The active region, when regional. |

### `Entitlement`

| Field | Type | Description |
|-------|------|-------------|
| `isEntitled` | `boolean` | Whether the user is entitled. |
| `justification` | `string \| null` | Why not, when `isEntitled` is false — for example `ENTITLEMENTS_DISABLED`, `NOT_AUTHENTICATED`, `MISSING_FEATURE`, `MISSING_PERMISSION`. |

### `LogLevel`

Controls SDK log output.

| Value | Meaning |
|-------|---------|
| `LogLevel.INFO` | Informational messages and above |
| `LogLevel.WARN` | Warnings and errors |
| `LogLevel.ERROR` | Errors only |
| `LogLevel.NONE` | No logging |
