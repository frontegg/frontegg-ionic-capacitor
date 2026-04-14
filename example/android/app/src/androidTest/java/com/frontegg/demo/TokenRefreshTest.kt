package com.frontegg.demo

import androidx.test.uiautomator.By
import com.frontegg.demo.utils.UiTestInstrumentation
import com.frontegg.demo.utils.delay
import com.frontegg.demo.utils.logout
import com.frontegg.demo.utils.performLogin
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import java.util.regex.Pattern

/**
 * Verifies that tapping "Refresh Token" issues a new JWT.
 *
 * The example app's profile tab (tab1.page.html) shows:
 *   - "accessToken: <last 40 chars>"
 *   - "Is Refreshing Token: true/false"
 *   - A "Refresh Token" button (when authenticated).
 */
class TokenRefreshTest {
    private lateinit var instrumentation: UiTestInstrumentation

    @Before
    fun setUp() {
        instrumentation = UiTestInstrumentation()
        instrumentation.openApp()
    }

    @Test
    fun refresh_token_updates_access_token() {
        // 1. Login.
        instrumentation.performLogin()

        // 2. Read the current accessToken suffix from the profile tab.
        val tokenPattern = Pattern.compile("^accessToken:.*")
        val tokenElement = instrumentation.waitForView(By.text(tokenPattern), timeout = 5_000)
            ?: throw Exception("accessToken text not found on profile tab")
        val originalToken = tokenElement.text

        // 3. Tap the "Refresh Token" button.
        instrumentation.clickByText("Refresh Token")

        // 4. Wait for the refresh cycle to complete.
        //    "Is Refreshing Token: true" appears transiently, then returns to false.
        instrumentation.waitForView(By.textContains("Is Refreshing Token: true"), timeout = 10_000)
        instrumentation.waitForView(By.textContains("Is Refreshing Token: false"), timeout = 30_000)
            ?: throw Exception("Token refresh did not complete — still showing 'true'")

        // Small delay for the UI to update the token display.
        delay(1_000)

        // 5. Read the new token.
        val newTokenElement = instrumentation.waitForView(By.text(tokenPattern), timeout = 5_000)
            ?: throw Exception("accessToken text not found after refresh")
        val newToken = newTokenElement.text

        // 6. Assert the token changed (new JWT issued).
        assertNotEquals("Access token should change after refresh", originalToken, newToken)

        // 7. Cleanup.
        instrumentation.logout()
    }
}
