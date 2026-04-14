package com.frontegg.demo

import com.frontegg.demo.utils.UiTestInstrumentation
import com.frontegg.demo.utils.logout
import com.frontegg.demo.utils.performLogin
import org.junit.Before
import org.junit.Test

/**
 * Verifies the full logout → re-login cycle in a single session.
 *
 * Mirrors frontegg-ios-swift MultiRegion `testLogoutAndReloginInSameRegion`.
 */
class LogoutAndReloginTest {
    private lateinit var instrumentation: UiTestInstrumentation

    @Before
    fun setUp() {
        instrumentation = UiTestInstrumentation()
        instrumentation.openApp()
    }

    @Test
    fun logout_and_relogin_succeeds() {
        // 1. First login.
        instrumentation.performLogin()

        // 2. Logout.
        instrumentation.logout()

        // 3. Second login — the hosted login should open again and succeed.
        instrumentation.performLogin()

        // 4. Cleanup.
        instrumentation.logout()
    }
}
