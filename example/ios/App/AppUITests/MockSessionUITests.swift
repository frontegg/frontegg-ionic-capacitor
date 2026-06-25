import XCTest

/// Tests login/logout and session persistence using the local mock auth server.
final class MockSessionUITests: MockServerTestCase {

    private let testEmail = "session-test@example.com"

    // MARK: - Tests

    /// Verifies end-to-end password login via the mock server and session
    /// persistence across app relaunches.
    ///
    /// 1. Launch the app pointed at the mock server.
    /// 2. Login via the mock hosted login page.
    /// 3. Verify authenticated state.
    /// 4. Terminate and relaunch without resetting state.
    /// 5. Verify session is restored (Logout button appears without re-login).
    func testPasswordLoginViaLocalMockAndSessionRestore() throws {
        launchApp()
        loginViaHostedMock(email: testEmail)
        waitForAuthenticated()

        // Verify isAuthenticated shows true.
        let authText = app.staticTexts.matching(
            NSPredicate(format: "label CONTAINS %@", "isAuthenticated: true")
        ).firstMatch
        XCTAssertTrue(authText.waitForExistence(timeout: 5), "isAuthenticated should be true after login")

        // Terminate and relaunch without resetting state.
        app.terminate()
        launchApp(resetState: false)

        // Session should be restored.
        waitForAuthenticated(timeout: 30)

        let restoredAuthText = app.staticTexts.matching(
            NSPredicate(format: "label CONTAINS %@", "isAuthenticated: true")
        ).firstMatch
        XCTAssertTrue(restoredAuthText.waitForExistence(timeout: 5),
            "isAuthenticated should be true after session restore")
    }

    /// Verifies that logging out clears the session so it is not restored on
    /// subsequent app launches.
    ///
    /// 1. Login via mock.
    /// 2. Tap Logout.
    /// 3. Verify login page appears.
    /// 4. Relaunch the app.
    /// 5. Verify login page appears (session was not persisted).
    func testLogoutClearsSessionOnMockServer() throws {
        launchApp()
        loginViaHostedMock(email: testEmail)
        waitForAuthenticated()

        // Tap Logout.
        let logoutButton = findLogoutButton()
        logoutButton.tap()

        // Verify login page appears after logout.
        waitForLoginPage(timeout: 15)

        // Verify the mock server received the logout request.
        let logoutReceived = Self.server.waitForRequest(
            method: "POST",
            path: "/oauth/logout/token",
            timeout: 10
        )
        XCTAssertTrue(logoutReceived, "Mock server did not receive logout request")

        // Relaunch the app and verify the session was not persisted.
        app.terminate()
        launchApp(resetState: false)

        waitForLoginPage(timeout: 15)
    }
}
