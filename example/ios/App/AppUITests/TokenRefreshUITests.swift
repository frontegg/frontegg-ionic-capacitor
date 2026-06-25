import XCTest

/// Verifies that tapping "Refresh Token" issues a new JWT.
///
/// The example app's profile tab shows accessToken (last 40 chars),
/// "Is Refreshing Token: true/false", and a "Refresh Token" button.
final class TokenRefreshUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    func testRefreshTokenUpdatesAccessToken() throws {
        // 1. Login.
        performLogin(app: app)

        // 2. Read the current accessToken suffix.
        let tokenPredicate = NSPredicate(format: "label BEGINSWITH 'accessToken:'")
        let tokenLabel = app.staticTexts.matching(tokenPredicate).firstMatch
        XCTAssertTrue(tokenLabel.waitForExistence(timeout: 5), "accessToken text not found on profile tab")
        let originalToken = tokenLabel.label

        // 3. Tap "Refresh Token".
        let refreshButton = app.buttons["Refresh Token"]
        XCTAssertTrue(refreshButton.waitForExistence(timeout: 5), "Refresh Token button not found")
        refreshButton.tap()

        // 4. Wait for the refresh cycle.
        let refreshing = app.staticTexts.matching(
            NSPredicate(format: "label CONTAINS 'Is Refreshing Token: true'")
        ).firstMatch
        // The transient "true" may be brief; don't fail if we miss it.
        _ = refreshing.waitForExistence(timeout: 10)

        let done = app.staticTexts.matching(
            NSPredicate(format: "label CONTAINS 'Is Refreshing Token: false'")
        ).firstMatch
        XCTAssertTrue(done.waitForExistence(timeout: 30), "Token refresh did not complete")

        // Small delay for the UI to update.
        Thread.sleep(forTimeInterval: 1)

        // 5. Read the new token.
        let newTokenLabel = app.staticTexts.matching(tokenPredicate).firstMatch
        XCTAssertTrue(newTokenLabel.waitForExistence(timeout: 5), "accessToken text not found after refresh")
        let newToken = newTokenLabel.label

        // 6. Assert the token changed.
        XCTAssertNotEqual(originalToken, newToken, "Access token should change after refresh")

        // 7. Cleanup.
        findLogoutButton(app: app).tap()
    }
}
