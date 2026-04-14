import XCTest

final class ProfileDataUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    func testProfileShowsUserDataAfterLogin() throws {
        performLogin(app: app)

        // Verify isAuthenticated shows true
        let authText = app.staticTexts.matching(
            NSPredicate(format: "label CONTAINS 'isAuthenticated: true'")
        ).firstMatch
        XCTAssertTrue(authText.waitForExistence(timeout: 5), "isAuthenticated should be true")

        // Verify email is displayed (from LOGIN_EMAIL env var)
        let email = ProcessInfo.processInfo.environment["LOGIN_EMAIL"] ?? ""
        if !email.isEmpty {
            let emailText = app.staticTexts.matching(
                NSPredicate(format: "label CONTAINS %@", "Email: \(email)")
            ).firstMatch
            XCTAssertTrue(emailText.waitForExistence(timeout: 5), "Email should be displayed")
        }

        // Verify accessToken is displayed (non-empty)
        let tokenText = app.staticTexts.matching(
            NSPredicate(format: "label BEGINSWITH 'accessToken:'")
        ).firstMatch
        XCTAssertTrue(tokenText.waitForExistence(timeout: 5), "accessToken should be displayed")
        XCTAssertTrue(tokenText.label.count > "accessToken: ".count, "accessToken should not be empty")

        // Cleanup
        findLogoutButton(app: app).tap()
    }
}
