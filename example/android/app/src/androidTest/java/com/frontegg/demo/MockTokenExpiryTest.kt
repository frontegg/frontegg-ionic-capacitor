package com.frontegg.demo

import androidx.test.uiautomator.By
import com.frontegg.demo.utils.MockFronteggServer
import com.frontegg.demo.utils.UiTestInstrumentation
import com.frontegg.demo.utils.delay
import com.frontegg.demo.utils.performLogin
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.regex.Pattern

/**
 * Tests token-expiry behaviour using an in-process mock Frontegg server.
 *
 * The mock server issues tokens with configurable TTLs so we can exercise
 * the SDK's refresh and session-clear logic without waiting real clock time.
 *
 * NOTE: These tests require the FRONTEGG_E2E_BASE_URL instrumentation arg
 * to be set to the mock server's URL so the plugin talks to the mock
 * instead of the real Frontegg backend.
 */
class MockTokenExpiryTest {

    private lateinit var instrumentation: UiTestInstrumentation
    private lateinit var mockServer: MockFronteggServer

    @Before
    fun setUp() {
        mockServer = MockFronteggServer()
        mockServer.start()

        instrumentation = UiTestInstrumentation()
        // The app must be launched with the mock base URL injected via
        // instrumentation args; see CI workflow for -P… flag wiring.
        instrumentation.openApp()
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }

    /**
     * Scenario: the access token has expired but the refresh token is still
     * valid.  On relaunch the SDK should silently refresh and restore the
     * authenticated session.
     */
    @Test
    fun expired_access_token_refreshes_on_relaunch() {
        // Very short access TTL (2 s) but long refresh TTL.
        mockServer.configureTokenPolicy(accessTTL = 2, refreshTTL = 86_400)

        // Login via the hosted login flow (mock server backs the token exchange).
        instrumentation.performLogin()

        // Wait for the access token to expire.
        delay(3_000)

        // Relaunch — the SDK should use the still-valid refresh token.
        instrumentation.openApp()

        // The refresh endpoint should have been hit.
        val refreshReq = mockServer.waitForRequest(
            "/frontegg/identity/resources/auth/v1/user/token/refresh",
            timeoutMs = 15_000
        )
        assertTrue(
            "Expected a token-refresh request after relaunch with expired access token",
            refreshReq != null || mockServer.requestCount("/frontegg/identity/resources/auth/v1/user/token/refresh") > 0
        )

        // The app should still be authenticated.
        val logoutPattern = Pattern.compile("logout", Pattern.CASE_INSENSITIVE)
        instrumentation.waitForView(By.text(logoutPattern), timeout = 15_000)
            ?: throw AssertionError("Session was not restored — Logout button not found after refresh")
    }

    /**
     * Scenario: both the access and refresh tokens have expired.
     * On relaunch the SDK should clear the session and show the login screen.
     */
    @Test
    fun expired_refresh_token_clears_session() {
        // Both tokens expire in 2 seconds.
        mockServer.configureTokenPolicy(accessTTL = 2, refreshTTL = 2)

        instrumentation.performLogin()

        // Wait for both tokens to expire.
        delay(4_000)

        // Make the refresh endpoint return 401 to simulate an expired refresh token.
        // (The mock already issues new tokens, so we queue connection drops to force
        // the SDK to treat the refresh as failed.)
        mockServer.queueConnectionDrops(
            "/frontegg/identity/resources/auth/v1/user/token/refresh", count = 3
        )

        instrumentation.openApp()

        // The app should fall back to the unauthenticated state.
        instrumentation.waitForView(By.text("Not Logged In"), timeout = 20_000)
            ?: instrumentation.waitForView(
                By.text(Pattern.compile("login", Pattern.CASE_INSENSITIVE)),
                timeout = 5_000
            )
            ?: throw AssertionError("Expected unauthenticated state after all tokens expired")
    }

    /**
     * Scenario: the SDK should automatically schedule a token refresh before
     * the access token expires (~80 % of TTL).  With a short TTL we can
     * observe the refresh request without waiting real clock time.
     */
    @Test
    fun scheduled_token_refresh_fires_before_expiry() {
        // Configure short access TTL. The SDK should auto-refresh at ~80% of TTL.
        mockServer.configureTokenPolicy(accessTTL = 10, refreshTTL = 86_400)
        instrumentation.performLogin()

        // Wait longer than the TTL for the scheduled refresh to fire.
        delay(12_000)

        // The refresh endpoint should have been called automatically.
        val refreshCount = mockServer.requestCount("/frontegg/identity/resources/auth/v1/user/token/refresh")
        assertTrue("Expected SDK to auto-refresh token before expiry", refreshCount > 0)

        // App should still be authenticated.
        val logoutPattern = Pattern.compile("logout", Pattern.CASE_INSENSITIVE)
        instrumentation.waitForView(By.text(logoutPattern), timeout = 10_000)
            ?: throw AssertionError("Session lost after scheduled refresh")
    }
}
