import XCTest

/// Negative login tests for iOS — wrong email and wrong password.
///
/// Ports the Android `LoginViaEmailAndPasswordTest` negative cases to iOS.
final class LoginNegativeUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    // MARK: - Helpers

    private func openHostedLoginAndFillEmail(_ email: String) {
        let loginButton = app.buttons["Login"]
        XCTAssertTrue(loginButton.waitForExistence(timeout: 15), "Login button not found")
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
        XCTAssertTrue(webView.waitForExistence(timeout: 20), "Hosted login web view did not load")

        let emailField = webView.textFields.firstMatch
        XCTAssertTrue(emailField.waitForExistence(timeout: 10), "Email field not found")
        emailField.tap()
        emailField.typeText(email)

        webView.buttons["Continue"].tap()
    }

    // MARK: - Tests

    func testLoginWithWrongEmailShowsError() throws {
        let wrongEmail = ProcessInfo.processInfo.environment["LOGIN_WRONG_EMAIL"]
            ?? "wrong-\(UUID().uuidString.prefix(8))@example.com"
        let password = ProcessInfo.processInfo.environment["LOGIN_PASSWORD"] ?? ""
        XCTAssertFalse(password.isEmpty, "LOGIN_PASSWORD env var must be set")

        openHostedLoginAndFillEmail(wrongEmail)

        let webView = app.webViews.firstMatch
        let passwordField = webView.secureTextFields.firstMatch
        XCTAssertTrue(passwordField.waitForExistence(timeout: 10), "Password field not found")
        passwordField.tap()
        passwordField.typeText(password)

        webView.buttons["Sign in"].tap()

        // Verify error message appears.
        let errorPredicate = NSPredicate(format: "label CONTAINS[c] 'Incorrect email or password'")
        let errorText = app.staticTexts.matching(errorPredicate).firstMatch
        // Also check inside webView.
        let webErrorText = webView.staticTexts.matching(errorPredicate).firstMatch

        let errorFound = errorText.waitForExistence(timeout: 10)
            || webErrorText.waitForExistence(timeout: 5)

        XCTAssertTrue(errorFound, "Expected 'Incorrect email or password' error message")
    }

    func testLoginWithWrongPasswordShowsError() throws {
        let email = ProcessInfo.processInfo.environment["LOGIN_EMAIL"] ?? ""
        XCTAssertFalse(email.isEmpty, "LOGIN_EMAIL env var must be set")
        let wrongPassword = ProcessInfo.processInfo.environment["LOGIN_WRONG_PASSWORD"]
            ?? "definitely-wrong-password-\(UUID().uuidString.prefix(8))"

        openHostedLoginAndFillEmail(email)

        let webView = app.webViews.firstMatch
        let passwordField = webView.secureTextFields.firstMatch
        XCTAssertTrue(passwordField.waitForExistence(timeout: 10), "Password field not found")
        passwordField.tap()
        passwordField.typeText(wrongPassword)

        webView.buttons["Sign in"].tap()

        // Verify error message appears.
        let errorPredicate = NSPredicate(format: "label CONTAINS[c] 'Incorrect email or password'")
        let errorText = app.staticTexts.matching(errorPredicate).firstMatch
        let webErrorText = webView.staticTexts.matching(errorPredicate).firstMatch

        let errorFound = errorText.waitForExistence(timeout: 10)
            || webErrorText.waitForExistence(timeout: 5)

        XCTAssertTrue(errorFound, "Expected 'Incorrect email or password' error message")
    }
}
