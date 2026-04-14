import XCTest

/// Stability tests — multiple launch cycles without crash.
///
/// Mirrors frontegg-ios-swift UIKit/MultiRegion `testMultipleLaunchCyclesDoNotCrash`.
final class StabilityUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    func testMultipleLaunchCyclesDoNotCrash() throws {
        // Login first.
        performLogin(app: app)

        // Cycle through 3 terminate/relaunch pairs.
        for cycle in 1...3 {
            app.terminate()
            app.launch()
            Thread.sleep(forTimeInterval: 2)

            // App should be alive — showing either Logout (session restored)
            // or Login (session expired). Either is fine; a crash is not.
            let logoutButton = findLogoutButton(app: app)
            let loginButton = app.buttons["Login"]

            let logoutExists = logoutButton.waitForExistence(timeout: 15)
            let loginExists = loginButton.waitForExistence(timeout: 5)

            XCTAssertTrue(logoutExists || loginExists,
                "App appears to have crashed on relaunch cycle \(cycle)")
        }

        // Cleanup if still authenticated.
        let logoutButton = findLogoutButton(app: app)
        if logoutButton.waitForExistence(timeout: 3) {
            logoutButton.tap()
        }
    }
}
