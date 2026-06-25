package com.frontegg.demo

import androidx.test.uiautomator.By
import com.frontegg.demo.utils.UiTestInstrumentation
import com.frontegg.demo.utils.delay
import com.frontegg.demo.utils.logout
import com.frontegg.demo.utils.performLogin
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import java.util.regex.Pattern

/**
 * Verifies tenant switching via the Tenants tab.
 *
 * The example app's tab2 shows a button per tenant:
 *   "{name} (active)" for the current tenant, "{name}" for others.
 * Tapping an inactive tenant triggers fronteggService.switchTenant().
 *
 * If the test user belongs to only one tenant the test is skipped gracefully.
 */
class TenantSwitchTest {
    private lateinit var instrumentation: UiTestInstrumentation

    @Before
    fun setUp() {
        instrumentation = UiTestInstrumentation()
        instrumentation.openApp()
    }

    @Test
    fun switch_tenant_updates_active_tenant() {
        // 1. Login.
        instrumentation.performLogin()

        // 2. Navigate to the Tenants tab.
        instrumentation.clickByText("Tenants")
        delay(2_000) // allow tenant list to render

        // 3. Find an inactive tenant (one without "(active)" in its text).
        //    Scan all visible text elements for a tenant button.
        val activePattern = Pattern.compile(".*\\(active\\).*")
        val activeElement = instrumentation.waitForView(By.text(activePattern), timeout = 10_000)
        val activeTenantText = activeElement?.text ?: ""

        // Find a button that doesn't contain "(active)" and isn't a tab label.
        val allPattern = Pattern.compile("^(?!.*(active|Profile|Tenants|Logout|Login)).*\\S+.*$")
        val inactiveElement = instrumentation.waitForView(By.text(allPattern), timeout = 5_000)

        // Skip if only one tenant.
        assumeTrue("Only one tenant available — skipping tenant switch test", inactiveElement != null)

        val targetName = inactiveElement!!.text.trim()
        inactiveElement.click()

        // 4. Wait for the target tenant to become active.
        delay(2_000) // allow the switch to start
        instrumentation.waitForView(By.textContains("$targetName (active)"), timeout = 30_000)
            ?: throw Exception("Tenant '$targetName' did not become active after switch")

        // 5. Navigate back to Profile tab and verify Active Tenant label.
        instrumentation.clickByText("Profile")
        instrumentation.waitForView(By.textContains("Active Tenant: $targetName"), timeout = 5_000)
            ?: throw Exception("Active Tenant label did not update to '$targetName'")

        // 6. Cleanup.
        instrumentation.logout()
    }
}
