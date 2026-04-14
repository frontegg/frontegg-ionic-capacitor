import XCTest

/// P0 login test for the Ionic Capacitor example app.
///
/// Adapted from frontegg-ios-swift's demo-embedded-e2e/DemoEmbeddedE2ETests
/// (testPasswordLoginAndSessionRestore). The embedded demo exposes internal
/// accessibility identifiers (LoginPageRoot, AuthRefreshingTokenValue, etc.)
/// that the Ionic example does not. The Ionic example's login page shows a
/// visible "Login" ion-button that calls FronteggService.login() and opens
/// the hosted login via ASWebAuthenticationSession, so this test taps that
/// button first and then drives the hosted login web view.
final class LoginPasswordUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    func testPasswordLogin() throws {
        let email = ProcessInfo.processInfo.environment["LOGIN_EMAIL"] ?? ""
        let password = ProcessInfo.processInfo.environment["LOGIN_PASSWORD"] ?? ""
        XCTAssertFalse(email.isEmpty, "LOGIN_EMAIL env var must be set")
        XCTAssertFalse(password.isEmpty, "LOGIN_PASSWORD env var must be set")

        // Tap the local "Login" button on the Ionic login page. The Ionic
        // example does not auto-launch hosted login; the user must tap first.
        let loginButton = app.buttons["Login"]
        XCTAssertTrue(loginButton.waitForExistence(timeout: 15), "Local Login button did not appear")
        loginButton.tap()

        // The hosted login opens in ASWebAuthenticationSession. The system
        // may show a consent alert first — dismiss it.
        addUIInterruptionMonitor(withDescription: "ASWebAuth consent") { alert in
            let continueButton = alert.buttons["Continue"]
            if continueButton.exists {
                continueButton.tap()
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

        // Back in the app, the logged-in state surfaces a Logout button.
        let logoutPredicate = NSPredicate(format: "label ==[c] %@", "Logout")
        let logoutButton = app.buttons.matching(logoutPredicate).firstMatch
        XCTAssertTrue(logoutButton.waitForExistence(timeout: 30), "Logout button did not appear — login likely failed")
    }
}
