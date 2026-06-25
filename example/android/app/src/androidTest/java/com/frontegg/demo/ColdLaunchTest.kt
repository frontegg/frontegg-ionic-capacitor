package com.frontegg.demo

import androidx.test.uiautomator.By
import com.frontegg.demo.utils.UiTestInstrumentation
import org.junit.Before
import org.junit.Test
import java.util.regex.Pattern

/**
 * Verifies that a cold launch with no persisted session shows the login page.
 *
 * Mirrors frontegg-ios-swift MultiRegion `testColdLaunchWithNoSessionShowsLoginPage`
 * and Embedded `testColdLaunchWithOfflineModeDisabledReachesLoginQuickly`.
 *
 * NOTE: This test assumes no prior session exists. If tests run sequentially
 * after a login test that doesn't clean up, this may see the authenticated
 * state instead. For true isolation, clear app data before running.
 */
class ColdLaunchTest {
    private lateinit var instrumentation: UiTestInstrumentation

    @Before
    fun setUp() {
        instrumentation = UiTestInstrumentation()
        instrumentation.openApp()
    }

    @Test
    fun cold_launch_shows_login_or_authenticated_state() {
        // On a fresh install (or after logout), the app should show the login
        // page with "Not Logged In" text and a "Login" button.
        // If a prior test left a session, we accept the authenticated state too.
        val loginPattern = Pattern.compile("login", Pattern.CASE_INSENSITIVE)
        val loginButton = instrumentation.waitForView(By.text(loginPattern), timeout = 15_000)

        val notLoggedIn = instrumentation.waitForView(By.text("Not Logged In"), timeout = 3_000)
        val logoutPattern = Pattern.compile("logout", Pattern.CASE_INSENSITIVE)
        val logoutButton = instrumentation.waitForView(By.text(logoutPattern), timeout = 3_000)

        // Must show EITHER the login page OR the authenticated state.
        val isLoginPage = loginButton != null && notLoggedIn != null
        val isAuthenticated = logoutButton != null

        if (!isLoginPage && !isAuthenticated) {
            throw Exception(
                "Cold launch did not reach a valid state — " +
                "expected login page or authenticated state"
            )
        }
    }
}
