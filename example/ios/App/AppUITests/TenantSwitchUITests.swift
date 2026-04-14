import XCTest

/// Verifies tenant switching via the Tenants tab.
///
/// The example app's tab2 shows a button per tenant:
///   "{name} (active)" for the current tenant, "{name}" for others.
/// Tapping an inactive tenant triggers fronteggService.switchTenant().
///
/// If the test user belongs to only one tenant the test is skipped gracefully.
final class TenantSwitchUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    func testSwitchTenantUpdatesActiveTenant() throws {
        // 1. Login.
        performLogin(app: app)

        // 2. Navigate to the Tenants tab.
        let tenantsTab = app.buttons["Tenants"]
        XCTAssertTrue(tenantsTab.waitForExistence(timeout: 5), "Tenants tab not found")
        tenantsTab.tap()
        Thread.sleep(forTimeInterval: 2) // allow tenant list to render

        // 3. Find an inactive tenant (one without "(active)" in its label).
        let allButtons = app.buttons.allElementsBoundByIndex
        var inactiveButton: XCUIElement?
        let skipLabels: Set<String> = ["Profile", "Tenants", "Logout", "Login", "Refresh Token"]
        for button in allButtons {
            let label = button.label
            if label.isEmpty { continue }
            if label.contains("(active)") { continue }
            if skipLabels.contains(label) { continue }
            inactiveButton = button
            break
        }

        // Skip if only one tenant.
        try XCTSkipIf(inactiveButton == nil, "Only one tenant available — skipping tenant switch test")

        let targetName = inactiveButton!.label.trimmingCharacters(in: .whitespaces)
        inactiveButton!.tap()

        // 4. Wait for the target tenant to become active.
        let activePredicate = NSPredicate(format: "label CONTAINS %@", "\(targetName) (active)")
        let activeButton = app.buttons.matching(activePredicate).firstMatch
        XCTAssertTrue(activeButton.waitForExistence(timeout: 30),
            "Tenant '\(targetName)' did not become active after switch")

        // 5. Navigate back to Profile tab and verify Active Tenant label.
        app.buttons["Profile"].tap()
        let tenantLabel = app.staticTexts.matching(
            NSPredicate(format: "label CONTAINS %@", "Active Tenant: \(targetName)")
        ).firstMatch
        XCTAssertTrue(tenantLabel.waitForExistence(timeout: 5),
            "Active Tenant label did not update to '\(targetName)'")

        // 6. Cleanup.
        findLogoutButton(app: app).tap()
    }
}
