package com.frontegg.demo

import com.frontegg.demo.utils.UiTestInstrumentation
import com.frontegg.demo.utils.delay
import com.frontegg.demo.utils.logout
import com.frontegg.demo.utils.performLogin
import org.junit.Before
import org.junit.Test
import java.util.regex.Pattern
import androidx.test.uiautomator.By

/**
 * Stability tests — multiple launch cycles without crash.
 *
 * Mirrors frontegg-ios-swift UIKit/MultiRegion `testMultipleLaunchCyclesDoNotCrash`.
 */
class StabilityTest {
    private lateinit var instrumentation: UiTestInstrumentation

    @Before
    fun setUp() {
        instrumentation = UiTestInstrumentation()
        instrumentation.openApp()
    }

    @Test
    fun multiple_launch_cycles_do_not_crash() {
        // Launch, login, then cycle through 3 relaunches without crashing.
        instrumentation.performLogin()

        repeat(3) { cycle ->
            instrumentation.openApp()
            delay(2_000)

            // App should still be alive — either showing Logout (session restored)
            // or Login (session expired). Either is acceptable; a crash is not.
            val logoutPattern = Pattern.compile("logout", Pattern.CASE_INSENSITIVE)
            val logoutVisible = instrumentation.waitForView(By.text(logoutPattern), timeout = 15_000)
            val loginVisible = instrumentation.waitForView(
                By.text(Pattern.compile("login", Pattern.CASE_INSENSITIVE)), timeout = 5_000
            )

            if (logoutVisible == null && loginVisible == null) {
                throw Exception("App appears to have crashed on relaunch cycle ${cycle + 1}")
            }
        }

        // Cleanup if still authenticated.
        val logoutPattern = Pattern.compile("logout", Pattern.CASE_INSENSITIVE)
        if (instrumentation.waitForView(By.text(logoutPattern), timeout = 3_000) != null) {
            instrumentation.logout()
        }
    }
}
