package com.frontegg.demo

import com.frontegg.demo.utils.MockServerTestCase
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Refresh resilience against transient failures injected by the mock server.
 */
class MockConnectivityTest : MockServerTestCase() {

    @Test
    fun refresh_recovers_from_a_transient_server_error() {
        launchApp()
        loginViaHostedMock()
        waitForAuthenticated()
        val requestsBeforeRefresh = tokenRequestCount()

        mock.enqueue("POST", TOKEN_PATH, listOf(mapOf("status" to 503, "body" to "unavailable")))
        tapButton("Refresh Token")

        val retried = mock.waitForRequestCount("POST", TOKEN_PATH, requestsBeforeRefresh + 2, timeoutMs = 30_000)

        assertTrue("Expected the SDK to retry the refresh after a 503", retried)
        waitForAuthenticated()
    }
}
