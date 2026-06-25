# AppUITests — iOS E2E

P0 end-to-end tests for the Ionic Capacitor example app, adapted from
[`frontegg/frontegg-ios-swift`](https://github.com/frontegg/frontegg-ios-swift)'s
`demo-embedded-e2e` target.

## Files

- `LoginPasswordUITests.swift` — hosted login with valid credentials
- `LogoutUITests.swift` — login then logout round trip
- `Support/LocalMockAuthServer.swift` — copied verbatim from
  `demo-embedded/demo-embedded-e2e/LocalMockAuthServer.swift`. Not wired into
  the Ionic tests yet (they drive the real Frontegg tenant); kept here so the
  mock-server flow from the Swift reference repo can be ported later.
- `Support/scenario-catalog.json` — copied verbatim from the same path in
  `frontegg-ios-swift`; companion to `LocalMockAuthServer.swift`.

## Xcode wiring (manual — not done automatically)

The `.xcodeproj` is not edited here because pbxproj edits by hand are
error-prone. In Xcode:

1. Open `example/ios/App/App.xcworkspace`.
2. **File → New → Target… → iOS → UI Testing Bundle**.
   - Product Name: `AppUITests`
   - Target to be Tested: `App`
   - Language: Swift
3. Delete the auto-generated `AppUITests.swift` Xcode creates inside the new
   target (we already have real tests in this folder).
4. In the Project navigator, right-click the `AppUITests` group →
   **Add Files to "App"…** and add everything under
   `example/ios/App/AppUITests/`:
   - `LoginPasswordUITests.swift`
   - `LogoutUITests.swift`
   - `Support/LocalMockAuthServer.swift`
   - `Support/scenario-catalog.json` (make sure it's in
     **Copy Bundle Resources** of the `AppUITests` target so it ships into the
     test bundle)

   When adding, uncheck "Copy items if needed" and tick only the `AppUITests`
   target.
5. In the `AppUITests` target's **Build Phases → Compile Sources**, confirm
   `LoginPasswordUITests.swift`, `LogoutUITests.swift`, and
   `LocalMockAuthServer.swift` are listed.
6. In the scheme editor (**Product → Scheme → Edit Scheme… → Test**), add the
   `AppUITests` bundle to the Test action.

## Running locally

```sh
cd example/ios/App
LOGIN_EMAIL="you@example.com" \
LOGIN_PASSWORD="..." \
xcodebuild test \
  -workspace App.xcworkspace \
  -scheme App \
  -destination 'platform=iOS Simulator,name=iPhone 15' \
  -only-testing:AppUITests
```

`LOGIN_EMAIL` and `LOGIN_PASSWORD` are read from the process environment by
the test classes.

## Notes on adaptations vs the Swift reference

- The `demo-embedded-e2e` tests use a `DemoEmbeddedUITestCase` base class and
  in-SDK accessibility identifiers (`LoginPageRoot`, `AuthRefreshingTokenValue`,
  etc.) that the **embedded** login surface exposes. The Ionic example uses
  **hosted** login via `ASWebAuthenticationSession`, so these tests instead
  drive the Frontegg hosted login web view (`app.webViews.firstMatch`) after
  tapping the Ionic example's local `Login` button. The reference's
  `testPasswordLoginAndSessionRestore` is split into `testPasswordLogin`
  (login) and `testLogout` (login + logout) for the Ionic surface; session
  restore across `terminateApp` is intentionally out of scope for P0.
- `LocalMockAuthServer.swift` and `scenario-catalog.json` are copied verbatim
  but not currently referenced by the test classes — they are carried over so
  a future iteration can swap the real tenant for the mock server the Swift
  SDK's embedded tests use.
