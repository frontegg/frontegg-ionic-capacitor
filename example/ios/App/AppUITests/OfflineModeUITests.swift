import XCTest

/// Offline mode tests — require the Ionic Capacitor plugin to expose
/// `forceNetworkPathOffline` environment variable support, mirroring
/// frontegg-ios-swift's DemoEmbeddedE2ETests offline scenarios.
///
/// These stubs document the intended coverage and will be implemented
/// once the native plugin supports network-path overrides for testing.
final class OfflineModeUITests: XCTestCase {

    func testAuthenticatedOfflineModeWhenNetworkPathUnavailable() throws {
        throw XCTSkip("Requires forceNetworkPathOffline support in Capacitor plugin — see frontegg-ios-swift DemoEmbeddedE2ETests")
    }

    func testAuthenticatedOfflineModeRecoversToOnlineAndRefreshesToken() throws {
        throw XCTSkip("Requires forceNetworkPathOffline support in Capacitor plugin")
    }

    func testAuthenticatedOfflineModeKeepsUserLoggedInUntilReconnect() throws {
        throw XCTSkip("Requires forceNetworkPathOffline support in Capacitor plugin")
    }

    func testLogoutWhileAuthenticatedOfflineShowsNoConnectionPage() throws {
        throw XCTSkip("Requires forceNetworkPathOffline support in Capacitor plugin")
    }

    func testRetryFromUnauthenticatedOfflineScreenReturnsToLogin() throws {
        throw XCTSkip("Requires forceNetworkPathOffline support in Capacitor plugin")
    }

    func testLogoutDuringTransientConnectivityLossRecoversToLogin() throws {
        throw XCTSkip("Requires forceNetworkPathOffline + mock server connectivity queue support")
    }
}
