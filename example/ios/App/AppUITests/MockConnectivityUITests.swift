import XCTest

/// Tests connectivity resilience using the local mock auth server's ability to
/// simulate connection drops and probe timeouts.
final class MockConnectivityUITests: MockServerTestCase {

    private let testEmail = "connectivity-test@example.com"

    // MARK: - Tests

    /// Verifies that the app recovers a session after transient connection drops
    /// during token refresh on relaunch.
    ///
    /// 1. Login and reach authenticated state.
    /// 2. Queue connection drops on the token refresh path.
    /// 3. Terminate and relaunch (without reset).
    /// 4. The first refresh attempt(s) will fail due to connection drops, but
    ///    the SDK should retry and eventually restore the session.
    func testRelaunchWithConnectionDropRecoverSession() throws {
        launchApp()
        loginViaHostedMock(email: testEmail)
        waitForAuthenticated()

        // Queue 2 connection drops on the token refresh endpoint. The mock
        // server will close the connection for the first 2 POST requests to
        // /oauth/token, then respond normally on the 3rd.
        try Self.server.queueConnectionDrops(
            method: "POST",
            path: "/oauth/token",
            count: 2
        )

        // Terminate and relaunch without resetting state.
        app.terminate()
        launchApp(resetState: false)

        // The SDK should retry token refresh after connection drops and
        // eventually restore the session. Allow extra time for retries.
        waitForAuthenticated(timeout: 45)
    }

    /// Verifies that transient probe timeouts on cold launch do not cause a
    /// "NoConnection" page to flash before the real content loads.
    ///
    /// The SDK probes the server on launch (HEAD /test). If the probe times out,
    /// the SDK should not immediately show a "no connection" error but should
    /// retry or proceed gracefully.
    func testColdLaunchTransientProbeTimeoutsDoNotBlinkNoConnectionPage() throws {
        // Queue 2 probe timeouts (1500ms delay each). After these, the server
        // responds normally so the app should eventually load.
        try Self.server.queueProbeTimeouts(count: 2, delayMs: 1500)

        launchApp()

        // Give the app a moment to process the slow probes.
        Thread.sleep(forTimeInterval: 1)

        // Check that no "NoConnection" or error state is shown. The app should
        // either be showing the login page or still loading.
        let noConnectionText = app.staticTexts.matching(
            NSPredicate(format: "label CONTAINS[c] %@", "NoConnection")
        ).firstMatch
        let noConnectionVisible = noConnectionText.waitForExistence(timeout: 3)
        XCTAssertFalse(noConnectionVisible, "NoConnection page should not flash during transient probe timeouts")

        // The app should eventually reach the login page.
        waitForLoginPage(timeout: 20)
    }
}
