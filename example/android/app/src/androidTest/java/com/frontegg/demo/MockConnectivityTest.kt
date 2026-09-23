package com.frontegg.demo

import com.frontegg.demo.utils.MockServerTestCase
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Refresh resilience against transient network failures, using connection drops on the mock server.
 */
class MockConnectivityTest : MockServerTestCase() {

    @Test
    fun refresh_recovers_from_a_dropped_connection() {
        launchApp()
        loginViaHostedMock()
        waitForAuthenticated()
        val requestsBeforeRefresh = tokenRequestCount()

        mock.queueConnectionDrops("POST", TOKEN_PATH, count = 1)
        tapButton("Refresh Token")

        val retried = mock.waitForRequestCount("POST", TOKEN_PATH, requestsBeforeRefresh + 2, timeoutMs = 30_000)

        assertTrue("Expected the SDK to retry the refresh after the dropped connection", retried)
        waitForAuthenticated()
    }
}
