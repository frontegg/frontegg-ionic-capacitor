package com.frontegg.demo

import com.frontegg.demo.utils.MockServerTestCase
import com.frontegg.demo.utils.delay
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Token lifetime behavior against the local mock server, mirroring MockTokenExpiryUITests on iOS.
 */
class MockTokenExpiryTest : MockServerTestCase() {

    @Test
    fun expired_access_token_is_refreshed_on_relaunch() {
        mock.configureTokenPolicy(DEFAULT_EMAIL, accessTTL = 21, refreshTTL = 300)
        launchApp()
        loginViaHostedMock()
        waitForAuthenticated()
        val tokenRequestsAfterLogin = tokenRequestCount()

        delay(25_000)
        launchApp(resetState = false)

        waitForAuthenticated()
        assertTrue("Expected a token refresh once the access token expired", tokenRequestCount() > tokenRequestsAfterLogin)
    }

    @Test
    fun expired_refresh_token_clears_session() {
        mock.configureTokenPolicy(DEFAULT_EMAIL, accessTTL = 10, refreshTTL = 15)
        launchApp()
        loginViaHostedMock()
        waitForAuthenticated()

        delay(25_000)
        launchApp(resetState = false)

        waitForLoginPage(timeout = 90_000)
    }

    @Test
    fun scheduled_token_refresh_fires_before_expiry() {
        mock.configureTokenPolicy(DEFAULT_EMAIL, accessTTL = 21, refreshTTL = 300)
        launchApp()
        loginViaHostedMock()
        waitForAuthenticated()
        val tokenRequestsAfterLogin = tokenRequestCount()

        val refreshed = mock.waitForRequestCount("POST", TOKEN_PATH, tokenRequestsAfterLogin + 1, timeoutMs = 30_000)

        assertTrue("Expected the SDK to refresh before the access token expired", refreshed)
        waitForAuthenticated()
    }
}
