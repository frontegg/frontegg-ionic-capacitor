import XCTest

/// Verifies that the SDK persists auth tokens across app relaunches.
///
/// Mirrors frontegg-ios-swift's testPasswordLoginAndSessionRestore.
final class SessionRestoreUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    func testSessionSurvivesRelaunch() throws {
        // 1. Login.
        performLogin(app: app)

        // 2. Terminate and relaunch.
        app.terminate()
        app.launch()

        // 3. Logout button should appear without another login (session restored).
        let logoutButton = findLogoutButton(app: app)
        XCTAssertTrue(logoutButton.waitForExistence(timeout: 30),
            "Session was not restored — Logout button not found after relaunch")

        // 4. Verify isAuthenticated shows true.
        let authText = app.staticTexts.matching(
            NSPredicate(format: "label CONTAINS %@", "isAuthenticated: true")
        ).firstMatch
        XCTAssertTrue(authText.waitForExistence(timeout: 5),
            "isAuthenticated should be true after session restore")

        // 5. Cleanup.
        logoutButton.tap()
    }
}
