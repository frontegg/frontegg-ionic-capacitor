package com.frontegg.demo

import androidx.test.uiautomator.By
import com.frontegg.demo.utils.UiTestInstrumentation
import com.frontegg.demo.utils.logout
import com.frontegg.demo.utils.performLogin
import org.junit.Before
import org.junit.Test
import java.util.regex.Pattern

/**
 * Verifies that the SDK persists auth tokens across app relaunches.
 *
 * Mirrors frontegg-ios-swift's testPasswordLoginAndSessionRestore and
 * frontegg-android-kotlin's implicit session-persistence expectation.
 */
class SessionRestoreTest {
    private lateinit var instrumentation: UiTestInstrumentation

    @Before
    fun setUp() {
        instrumentation = UiTestInstrumentation()
        instrumentation.openApp()
    }

    @Test
    fun session_survives_app_relaunch() {
        // 1. Perform full login.
        instrumentation.performLogin()

        // 2. Relaunch the app (simulates a cold start from the launcher).
        instrumentation.openApp()

        // 3. The Logout button should appear without another login — the SDK
        //    restored the session from persisted refresh tokens.
        val logoutPattern = Pattern.compile("logout", Pattern.CASE_INSENSITIVE)
        instrumentation.waitForView(By.text(logoutPattern), timeout = 30_000)
            ?: throw Exception("Session was not restored — Logout button not found after relaunch")

        // 4. Verify isAuthenticated shows true on the profile tab.
        instrumentation.waitForView(By.textContains("isAuthenticated: true"), timeout = 5_000)
            ?: throw Exception("isAuthenticated should be true after session restore")

        // 5. Cleanup.
        instrumentation.logout()
    }
}
