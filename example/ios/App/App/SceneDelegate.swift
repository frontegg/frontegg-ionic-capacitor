import UIKit
import Capacitor
import FronteggSwift

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
        guard let windowScene = scene as? UIWindowScene else { return }

        window = UIWindow(windowScene: windowScene)
        window?.rootViewController = CAPBridgeViewController()
        window?.makeKeyAndVisible()

        SceneDelegateProxy.shared.scene(scene, willConnectTo: session, options: connectionOptions)
    }

    /*
     * Under UIScene, AppDelegate's application(_:open:) and application(_:continue:) are no longer
     * called, so the Frontegg login callback has to be handled here instead.
     */
    func scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
        for context in URLContexts {
            if context.url.absoluteString.hasPrefix(FronteggAuth.shared.baseUrl) {
                _ = FronteggAuth.shared.handleOpenUrl(context.url)
                return
            }
        }
        SceneDelegateProxy.shared.scene(scene, openURLContexts: URLContexts)
    }

    func scene(_ scene: UIScene, continue userActivity: NSUserActivity) {
        if let url = userActivity.webpageURL,
           url.absoluteString.hasPrefix(FronteggAuth.shared.baseUrl) {
            _ = FronteggAuth.shared.handleOpenUrl(url)
            return
        }
        SceneDelegateProxy.shared.scene(scene, continue: userActivity)
    }
}
