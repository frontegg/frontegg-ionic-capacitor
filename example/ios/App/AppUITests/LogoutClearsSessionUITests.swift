import XCTest

/// Verifies that logout clears the session and a relaunch shows the login page.
///
/// Mirrors frontegg-ios-swift Embedded/UIKit/MultiRegion
/// `testLogoutClearsSessionAndRelaunchShowsLogin`.
final class LogoutClearsSessionUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    func testLogoutClearsSessionAndRelaunchShowsLogin() throws {
        // 1. Login.
        performLogin(app: app)

        // 2. Logout.
        let logoutButton = findLogoutButton(app: app)
        XCTAssertTrue(logoutButton.waitForExistence(timeout: 5))
        logoutButton.tap()

        let notLoggedIn = app.staticTexts["Not Logged In"]
        XCTAssertTrue(notLoggedIn.waitForExistence(timeout: 10), "Logout did not return to login state")

        // 3. Terminate and relaunch.
        app.terminate()
        app.launch()

        // 4. Login page should appear — session was cleared.
        let loginButton = app.buttons["Login"]
        XCTAssertTrue(loginButton.waitForExistence(timeout: 15),
            "Relaunch after logout did not show login page — session may not have been cleared")

        let notLoggedInAgain = app.staticTexts["Not Logged In"]
        XCTAssertTrue(notLoggedInAgain.waitForExistence(timeout: 5),
            "Expected 'Not Logged In' text after relaunch post-logout")
    }
}
