import XCTest

/// Social login edge-case tests — require embedded login mode which
/// the Ionic example currently uses hosted login instead.
/// Mirrors frontegg-ios-swift DemoEmbeddedE2ETests social scenarios.
final class SocialLoginEdgeCaseUITests: XCTestCase {

    func testDirectSocialBrowserHandoff() throws {
        throw XCTSkip("Requires directLoginAction support in example app UI — Ionic example uses hosted login")
    }

    func testCustomSSOBrowserHandoff() throws {
        throw XCTSkip("Requires custom SSO configuration in example app — Ionic example uses hosted login")
    }

    func testEmbeddedGoogleSocialLoginDoesNotShowOAuthErrorToast() throws {
        throw XCTSkip("Requires embedded login mode — Ionic example uses hosted login via ASWebAuthenticationSession")
    }

    func testEmbeddedSamlLogin() throws {
        throw XCTSkip("Requires embedded login mode with SAML IdP mock — Ionic example uses hosted login")
    }
}
