import XCTest

/// P0 logout test for the Ionic Capacitor example app.
///
/// Performs a full login then taps LOGOUT and verifies the app returns to
/// the unauthenticated state. Mirrors the Android LogoutTest.
final class LogoutUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    func testLogout() throws {
        let email = ProcessInfo.processInfo.environment["LOGIN_EMAIL"] ?? ""
        let password = ProcessInfo.processInfo.environment["LOGIN_PASSWORD"] ?? ""
        XCTAssertFalse(email.isEmpty, "LOGIN_EMAIL env var must be set")
        XCTAssertFalse(password.isEmpty, "LOGIN_PASSWORD env var must be set")

        let loginButton = app.buttons["Login"]
        XCTAssertTrue(loginButton.waitForExistence(timeout: 15))
        loginButton.tap()

        addUIInterruptionMonitor(withDescription: "ASWebAuth consent") { alert in
            if alert.buttons["Continue"].exists {
                alert.buttons["Continue"].tap()
                return true
            }
            return false
        }
        app.tap()

        let webView = app.webViews.firstMatch
        XCTAssertTrue(webView.waitForExistence(timeout: 20))

        let emailField = webView.textFields.firstMatch
        XCTAssertTrue(emailField.waitForExistence(timeout: 10))
        emailField.tap()
        emailField.typeText(email)
        webView.buttons["Continue"].tap()

        let passwordField = webView.secureTextFields.firstMatch
        XCTAssertTrue(passwordField.waitForExistence(timeout: 10))
        passwordField.tap()
        passwordField.typeText(password)
        webView.buttons["Sign in"].tap()

        let logoutPredicate = NSPredicate(format: "label ==[c] %@", "Logout")
        let logoutButton = app.buttons.matching(logoutPredicate).firstMatch
        XCTAssertTrue(logoutButton.waitForExistence(timeout: 30), "Login did not complete")
        logoutButton.tap()

        let notAuthenticated = app.staticTexts["Not Logged In"]
        XCTAssertTrue(notAuthenticated.waitForExistence(timeout: 10), "Logout did not return to unauthenticated state")
    }
}
