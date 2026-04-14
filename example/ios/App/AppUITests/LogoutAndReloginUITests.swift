import XCTest

/// Verifies the full logout → re-login cycle in a single session.
///
/// Mirrors frontegg-ios-swift MultiRegion `testLogoutAndReloginInSameRegion`.
final class LogoutAndReloginUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    func testLogoutAndReloginSucceeds() throws {
        // 1. First login.
        performLogin(app: app)

        // 2. Logout.
        let logoutButton = findLogoutButton(app: app)
        XCTAssertTrue(logoutButton.waitForExistence(timeout: 5))
        logoutButton.tap()

        let notLoggedIn = app.staticTexts["Not Logged In"]
        XCTAssertTrue(notLoggedIn.waitForExistence(timeout: 10), "Logout did not return to login state")

        // 3. Second login — hosted login should open again and succeed.
        performLogin(app: app)

        // 4. Cleanup.
        let logoutAgain = findLogoutButton(app: app)
        XCTAssertTrue(logoutAgain.waitForExistence(timeout: 5))
        logoutAgain.tap()
    }
}
