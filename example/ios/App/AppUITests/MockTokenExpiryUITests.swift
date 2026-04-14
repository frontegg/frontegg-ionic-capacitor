import XCTest

/// Tests token expiry and refresh behavior using the local mock auth server.
///
/// Ported from frontegg-ios-swift's DemoEmbeddedE2ETests token-related tests.
final class MockTokenExpiryUITests: MockServerTestCase {

    private let testEmail = "token-expiry@example.com"

    // MARK: - Tests

    /// Verifies that an expired access token is refreshed on app relaunch when
    /// the refresh token is still valid.
    ///
    /// 1. Configure short access token TTL (21s), long refresh TTL (120s).
    /// 2. Login, wait for authenticated state.
    /// 3. Wait for the access token to expire.
    /// 4. Terminate and relaunch (without resetting state).
    /// 5. Verify session is restored (Logout button appears) — the SDK should
    ///    use the refresh token to obtain a new access token.
    func testExpiredAccessTokenRefreshesOnRelaunch() throws {
        Self.server.configureTokenPolicy(
            email: testEmail,
            accessTokenTTL: 21,
            refreshTokenTTL: 120
        )

        launchApp()
        loginViaHostedMock(email: testEmail)
        waitForAuthenticated()

        // Wait for access token to expire (21s TTL + small buffer).
        Thread.sleep(forTimeInterval: 25)

        // Terminate and relaunch without resetting state so the refresh token
        // persists in the keychain.
        app.terminate()
        launchApp(resetState: false)

        // The SDK should detect the expired access token, use the valid refresh
        // token, and restore the session.
        waitForAuthenticated(timeout: 30)
    }

    /// Verifies that the SDK's scheduled token refresh fires before expiry so
    /// the session remains valid without user intervention.
    ///
    /// The SDK typically schedules a refresh at ~80% of the access token TTL.
    /// With a 21s TTL, the refresh should fire around 16-17s.
    func testScheduledTokenRefreshFiresBeforeExpiry() throws {
        Self.server.configureTokenPolicy(
            email: testEmail,
            accessTokenTTL: 21,
            refreshTokenTTL: 120
        )

        launchApp()
        loginViaHostedMock(email: testEmail)
        waitForAuthenticated()

        // Read the initial access token.
        let tokenPredicate = NSPredicate(format: "label BEGINSWITH 'accessToken:'")
        let tokenLabel = app.staticTexts.matching(tokenPredicate).firstMatch
        XCTAssertTrue(tokenLabel.waitForExistence(timeout: 5), "accessToken text not found")
        let originalToken = tokenLabel.label

        // Wait long enough for the scheduled refresh to fire (~80% of 21s = ~17s)
        // plus a buffer for the refresh round-trip.
        Thread.sleep(forTimeInterval: 22)

        // Verify the mock server received at least one refresh request.
        let refreshed = Self.server.waitForRequest(
            method: "POST",
            path: "/oauth/token",
            timeout: 10
        )
        XCTAssertTrue(refreshed, "No token refresh request was received by the mock server")

        // The token label should have changed.
        let newTokenLabel = app.staticTexts.matching(tokenPredicate).firstMatch
        XCTAssertTrue(newTokenLabel.waitForExistence(timeout: 10), "accessToken text not found after refresh")
        let newToken = newTokenLabel.label
        XCTAssertNotEqual(originalToken, newToken, "Access token should have been refreshed automatically")
    }

    /// Verifies that when both access and refresh tokens expire, the SDK clears
    /// the session and shows the login page on relaunch.
    ///
    /// 1. Configure very short TTLs (access 3s, refresh 5s).
    /// 2. Login, wait for authenticated state.
    /// 3. Wait for both tokens to expire.
    /// 4. Terminate and relaunch.
    /// 5. Verify login page is shown (session was not restored).
    func testExpiredRefreshTokenClearsSessionAndShowsLogin() throws {
        Self.server.configureTokenPolicy(
            email: testEmail,
            accessTokenTTL: 3,
            refreshTokenTTL: 5
        )

        launchApp()
        loginViaHostedMock(email: testEmail)
        waitForAuthenticated()

        // Wait for both tokens to expire.
        Thread.sleep(forTimeInterval: 8)

        // Terminate and relaunch.
        app.terminate()
        launchApp(resetState: false)

        // Both tokens expired, so the SDK should show the login page.
        waitForLoginPage(timeout: 30)
    }
}
