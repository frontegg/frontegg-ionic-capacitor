import XCTest

/// Base class for all mock-server-dependent UI tests.
///
/// Starts a `LocalMockAuthServer` once per test class, resets it between tests,
/// and provides helpers for launching the app pointed at the mock and performing
/// login via the mock hosted login flow.
class MockServerTestCase: XCTestCase {
    static var server: LocalMockAuthServer!
    var app: XCUIApplication!

    override class func setUp() {
        super.setUp()
        server = try! LocalMockAuthServer()
    }

    override class func tearDown() {
        server?.stop()
        server = nil
        super.tearDown()
    }

    override func setUpWithError() throws {
        continueAfterFailure = false
        try Self.server.reset()
        app = XCUIApplication()
    }

    // MARK: - Launch helpers

    /// Launches the app with the mock server environment injected.
    func launchApp(resetState: Bool = true) {
        let env = Self.server.launchEnvironment(resetState: resetState)
        app.launchEnvironment = env
        app.launch()
    }

    // MARK: - Mock login flow

    /// Performs a full login against the mock server's hosted login page.
    ///
    /// The mock server renders a simple HTML page at `/oauth/prelogin` with an
    /// email field (`hosted-email`), a password field (`hosted-password`), and a
    /// "Sign in" button. This helper taps the app's Login button, handles the
    /// ASWebAuthenticationSession consent alert, then fills in the mock hosted
    /// login form.
    func loginViaHostedMock(email: String = "test@example.com", password: String = "Testpassword1!") {
        // Tap the local Login button.
        let loginButton = app.buttons["Login"]
        XCTAssertTrue(loginButton.waitForExistence(timeout: 15), "Login button did not appear")
        loginButton.tap()

        // Handle ASWebAuthenticationSession consent alert.
        addUIInterruptionMonitor(withDescription: "ASWebAuth") { alert in
            if alert.buttons["Continue"].exists {
                alert.buttons["Continue"].tap()
                return true
            }
            return false
        }
        app.tap()

        // The mock server renders a hosted login page in a webview.
        let webView = app.webViews.firstMatch
        XCTAssertTrue(webView.waitForExistence(timeout: 20), "Mock hosted login webview did not load")

        // The mock's hosted email step shows an email input and "Continue" button.
        let emailField = webView.textFields.firstMatch
        XCTAssertTrue(emailField.waitForExistence(timeout: 10), "Email field not found on mock login page")
        emailField.tap()
        emailField.typeText(email)

        let continueButton = webView.buttons["Continue"]
        XCTAssertTrue(continueButton.waitForExistence(timeout: 5), "Continue button not found on mock login page")
        continueButton.tap()

        // The mock's hosted password step shows a password input and "Sign in" button.
        let passwordField = webView.secureTextFields.firstMatch
        XCTAssertTrue(passwordField.waitForExistence(timeout: 10), "Password field not found on mock login page")
        passwordField.tap()
        passwordField.typeText(password)

        let signInButton = webView.buttons["Sign in"]
        XCTAssertTrue(signInButton.waitForExistence(timeout: 5), "Sign in button not found on mock login page")
        signInButton.tap()
    }

    // MARK: - Wait helpers

    /// Waits for the authenticated state (Logout button visible).
    func waitForAuthenticated(timeout: TimeInterval = 30) {
        let logoutButton = findLogoutButton()
        XCTAssertTrue(logoutButton.waitForExistence(timeout: timeout), "Did not reach authenticated state")
    }

    /// Waits for the login page (Login button visible).
    func waitForLoginPage(timeout: TimeInterval = 15) {
        let loginButton = app.buttons["Login"]
        XCTAssertTrue(loginButton.waitForExistence(timeout: timeout), "Did not reach login page")
    }

    /// Finds the Logout button using a case-insensitive label match.
    func findLogoutButton() -> XCUIElement {
        let predicate = NSPredicate(format: "label ==[c] %@", "Logout")
        return app.buttons.matching(predicate).firstMatch
    }
}
