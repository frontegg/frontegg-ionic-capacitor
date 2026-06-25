package com.frontegg.demo

import androidx.test.uiautomator.By
import com.frontegg.demo.utils.Env
import com.frontegg.demo.utils.UiTestInstrumentation
import com.frontegg.demo.utils.logout
import com.frontegg.demo.utils.performLogin
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.regex.Pattern

/**
 * Verifies that the profile tab shows correct user data after login.
 *
 * This test does NOT use the mock server — it runs against the real Frontegg
 * backend and validates the data the SDK exposes in the example app's UI.
 */
class ProfileDataTest {

    private lateinit var instrumentation: UiTestInstrumentation

    @Before
    fun setUp() {
        instrumentation = UiTestInstrumentation()
        instrumentation.openApp()
    }

    /**
     * After a successful email+password login the profile tab should display:
     *   - An email field that contains the login email address
     *   - isAuthenticated: true
     *   - A non-empty accessToken string
     */
    @Test
    fun profile_shows_user_data_after_login() {
        // 1. Perform login with real credentials.
        instrumentation.performLogin()

        // 2. Verify isAuthenticated shows true.
        instrumentation.waitForView(By.textContains("isAuthenticated: true"), timeout = 10_000)
            ?: throw AssertionError("isAuthenticated should be true after login")

        // 3. Verify the email address is displayed somewhere on the profile tab.
        val emailPattern = Pattern.compile(
            Pattern.quote(Env.loginEmail),
            Pattern.CASE_INSENSITIVE
        )
        val emailView = instrumentation.waitForView(By.text(emailPattern), timeout = 5_000)
        assertTrue(
            "Expected login email '${Env.loginEmail}' to appear on the profile tab",
            emailView != null
        )

        // 4. Verify that an accessToken value is shown (non-empty).
        val tokenPattern = Pattern.compile("^accessToken:\\s*.+")
        val tokenView = instrumentation.waitForView(By.text(tokenPattern), timeout = 5_000)
        assertTrue(
            "Expected a non-empty accessToken on the profile tab",
            tokenView != null
        )

        // 5. Cleanup.
        instrumentation.logout()
    }
}
