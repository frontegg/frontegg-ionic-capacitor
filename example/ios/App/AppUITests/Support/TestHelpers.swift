import XCTest

/// Shared helpers for E2E test classes. Extracts the login flow that every
/// test needs, reducing duplication across LoginPasswordUITests, LogoutUITests,
/// SessionRestoreUITests, TokenRefreshUITests, and TenantSwitchUITests.
extension XCTestCase {

    // MARK: - Login

    /// Performs a full email+password login against the Frontegg hosted login.
    ///
    /// 1. Taps the local "Login" button on the Ionic login page.
    /// 2. Handles the ASWebAuthenticationSession consent alert.
    /// 3. Fills in email and password on the hosted web view.
    /// 4. Waits for the authenticated state (Logout button visible).
    func performLogin(app: XCUIApplication) {
        let email = ProcessInfo.processInfo.environment["LOGIN_EMAIL"] ?? ""
        let password = ProcessInfo.processInfo.environment["LOGIN_PASSWORD"] ?? ""
        XCTAssertFalse(email.isEmpty, "LOGIN_EMAIL env var must be set")
        XCTAssertFalse(password.isEmpty, "LOGIN_PASSWORD env var must be set")

        // Tap the local Login button.
        let loginButton = app.buttons["Login"]
        XCTAssertTrue(loginButton.waitForExistence(timeout: 15), "Login button did not appear")
        loginButton.tap()

        // Dismiss ASWebAuthenticationSession consent alert if shown.
        addUIInterruptionMonitor(withDescription: "ASWebAuth consent") { alert in
            let continueBtn = alert.buttons["Continue"]
            if continueBtn.exists {
                continueBtn.tap()
                return true
            }
            return false
        }
        app.tap() // nudge the interruption monitor

        // Drive the hosted login web view.
        let webView = app.webViews.firstMatch
        XCTAssertTrue(webView.waitForExistence(timeout: 20), "Hosted login web view did not load")

        let emailField = webView.textFields.firstMatch
        XCTAssertTrue(emailField.waitForExistence(timeout: 10), "Email field not found")
        emailField.tap()
        emailField.typeText(email)

        webView.buttons["Continue"].tap()

        let passwordField = webView.secureTextFields.firstMatch
        XCTAssertTrue(passwordField.waitForExistence(timeout: 10), "Password field not found")
        passwordField.tap()
        passwordField.typeText(password)

        webView.buttons["Sign in"].tap()

        // Wait for authenticated state.
        let logoutButton = findLogoutButton(app: app)
        XCTAssertTrue(logoutButton.waitForExistence(timeout: 30), "Login did not complete — Logout button not found")
    }

    // MARK: - Element finders

    /// Finds the Logout button using a case-insensitive label match.
    func findLogoutButton(app: XCUIApplication) -> XCUIElement {
        let predicate = NSPredicate(format: "label ==[c] %@", "Logout")
        return app.buttons.matching(predicate).firstMatch
    }
}
