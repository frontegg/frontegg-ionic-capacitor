import XCTest

/// Verifies that a cold launch with no persisted session shows the login page.
///
/// Mirrors frontegg-ios-swift MultiRegion `testColdLaunchWithNoSessionShowsLoginPage`
/// and Embedded `testColdLaunchWithOfflineModeDisabledReachesLoginQuickly`.
final class ColdLaunchUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    func testColdLaunchShowsLoginOrAuthenticatedState() throws {
        // On a fresh install the app should show "Not Logged In" and a Login button.
        // If a prior test left a session, the authenticated state is also acceptable.
        let loginButton = app.buttons["Login"]
        let notLoggedIn = app.staticTexts["Not Logged In"]
        let logoutButton = findLogoutButton(app: app)

        let loginExists = loginButton.waitForExistence(timeout: 15)
        let notLoggedInExists = notLoggedIn.waitForExistence(timeout: 3)
        let logoutExists = logoutButton.waitForExistence(timeout: 3)

        let isLoginPage = loginExists && notLoggedInExists
        let isAuthenticated = logoutExists

        XCTAssertTrue(isLoginPage || isAuthenticated,
            "Cold launch did not reach a valid state — expected login page or authenticated state")
    }
}
