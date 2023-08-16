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

    override public func load() {

        let auth = fronteggApp.auth
        var anyChange: AnyPublisher<Void, Never> {
            return Publishers.Merge8 (
                auth.$accessToken.map { _ in },
                auth.$refreshToken.map {_ in },
                auth.$user.map {_ in },
                auth.$isAuthenticated.map {_ in },
                auth.$isLoading.map {_ in },
                auth.$initializing.map {_ in },
                auth.$showLoader.map {_ in },
                auth.$appLink.map {_ in }
            )
            .eraseToAnyPublisher()
        }

        anyChange.sink(receiveValue: { () in

            self.sendEvent()
        }).store(in: &cancellables)

        self.sendEvent()
    }

    func sendEvent() {
        let auth = fronteggApp.auth

        var jsonUser: [String: Any]? = nil
        if let userData = try? JSONEncoder().encode(auth.user) {
            jsonUser = try? JSONSerialization.jsonObject(with: userData, options: .allowFragments) as? [String: Any]
        }

        let body: [String: Any?] = [
            "accessToken": auth.accessToken,
            "refreshToken": auth.refreshToken,
            "user": jsonUser,
            "isAuthenticated": auth.isAuthenticated,
            "isLoading": auth.isLoading,
            "initializing": auth.initializing,
            "showLoader": auth.showLoader,
            "appLink": auth.appLink
        ]

        self.notifyListeners("onFronteggAuthEvent", data: body as [String : Any])
    }

    @objc func getConstants(_ call: CAPPluginCall) {
        call.resolve([
            "baseUrl": fronteggApp.baseUrl,
            "clientId": fronteggApp.clientId,
            "bundleId": Bundle.main.bundleIdentifier!
        ])
    }

    @objc func login(_ call: CAPPluginCall) {
        fronteggApp.auth.login()
        call.resolve()
    }

    @objc func logout(_ call: CAPPluginCall) {
        fronteggApp.auth.logout()
        call.resolve()
    }

    @objc func switchTenant(_ call: CAPPluginCall) {
        guard let tenantId = call.options["tenantId"] as? String else {
            call.reject("No tenantId provided")
            return
        }

        fronteggApp.auth.switchTenant(tenantId: tenantId) { _ in
            call.resolve()
        }
    }

    @objc func getAuthState(_ call: CAPPluginCall) {
        let auth = fronteggApp.auth
        var jsonUser: [String: Any]? = nil
        if let userData = try? JSONEncoder().encode(auth.user) {
            jsonUser = try? JSONSerialization.jsonObject(with: userData, options: .allowFragments) as? [String: Any]
        }

        let body: [String: Any?] = [
            "accessToken": auth.accessToken,
            "refreshToken": auth.refreshToken,
            "user": jsonUser,
            "isAuthenticated": auth.isAuthenticated,
            "isLoading": auth.isLoading,
            "initializing": auth.initializing,
            "showLoader": auth.showLoader,
            "appLink": auth.appLink
        ]
        call.resolve(body as? [String: Any] ?? [:])
    }

}
