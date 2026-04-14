package com.frontegg.demo

import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Offline mode test stubs — require the Ionic Capacitor plugin to expose
 * forceNetworkPathOffline support for testing.
 *
 * These stubs document intended coverage from frontegg-ios-swift.
 */
class OfflineModeStubTest {

    @Test
    fun authenticated_offline_mode_when_network_unavailable() {
        assumeTrue("Requires forceNetworkPathOffline in Capacitor plugin", false)
    }

    @Test
    fun offline_mode_recovers_to_online_and_refreshes_token() {
        assumeTrue("Requires forceNetworkPathOffline in Capacitor plugin", false)
    }

    @Test
    fun logout_while_offline_shows_unauthenticated_state() {
        assumeTrue("Requires forceNetworkPathOffline in Capacitor plugin", false)
    }

    @Test
    fun direct_social_browser_handoff() {
        assumeTrue("Requires directLoginAction support in example app UI", false)
    }
}
