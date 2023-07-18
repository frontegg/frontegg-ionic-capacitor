import FronteggSwift
import Foundation
import Combine
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(FronteggNativePlugin)
public class FronteggNativePlugin: CAPPlugin {
    public let fronteggApp = FronteggApp.shared
    var cancellables = Set<AnyCancellable>()

    @objc func getConstants() -> [AnyHashable : Any]! {
      return [
        "baseUrl": fronteggApp.baseUrl,
        "clientId": fronteggApp.clientId,
        "bundleId": Bundle.main.bundleIdentifier!
      ]
    }

    @objc func login() {
        fronteggApp.auth.login()
    }

    @objc func logout() {
      fronteggApp.auth.logout()
    }
    
    
    @objc func subscribe() {
//      fronteggApp.auth.logout()
    }
}
