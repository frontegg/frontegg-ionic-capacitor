package com.frontegg.demo

import androidx.test.uiautomator.By
import com.frontegg.demo.utils.UiTestInstrumentation
import com.frontegg.demo.utils.logout
import com.frontegg.demo.utils.performLogin
import org.junit.Before
import org.junit.Test

/**
 * Verifies that logout clears the session and a relaunch shows the login page.
 *
 * Mirrors frontegg-ios-swift Embedded/UIKit/MultiRegion
 * `testLogoutClearsSessionAndRelaunchShowsLogin`.
 */
class LogoutClearsSessionTest {
    private lateinit var instrumentation: UiTestInstrumentation

    @Before
    fun setUp() {
        instrumentation = UiTestInstrumentation()
        instrumentation.openApp()
    }

    @Test
    fun logout_clears_session_and_relaunch_shows_login() {
        // 1. Login.
        instrumentation.performLogin()

        // 2. Logout.
        instrumentation.logout()

        // 3. Relaunch the app.
        instrumentation.openApp()

        // 4. The login page should appear — session was cleared.
        instrumentation.waitForView(By.text("Not Logged In"), timeout = 15_000)
            ?: throw Exception("Relaunch after logout did not show login page — session may not have been cleared")
    }
}
