import UIKit
import Capacitor
import FronteggSwift

/*
 * Under UIScene, AppDelegate's application(_:open:) and application(_:continue:) are no longer
 * called, so the Frontegg login callback has to be handled here instead.
 */
class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
        guard let windowScene = scene as? UIWindowScene else { return }

        window = UIWindow(windowScene: windowScene)
        window?.rootViewController = CAPBridgeViewController()
        window?.makeKeyAndVisible()

        /*
         * On a cold start the launch URL arrives here rather than through the callbacks below,
         * and Capacitor's proxy replays it to its own methods, not to this delegate. Without
         * this, a magic link that launches a terminated app never reaches Frontegg.
         */
        for context in connectionOptions.urlContexts where handleFronteggURL(context.url) {
            break
        }
        for userActivity in connectionOptions.userActivities {
            if let url = userActivity.webpageURL, handleFronteggURL(url) {
                break
            }
        }

        SceneDelegateProxy.shared.scene(scene, willConnectTo: session, options: connectionOptions)
    }

    func scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
        for context in URLContexts where handleFronteggURL(context.url) {
            return
        }
        SceneDelegateProxy.shared.scene(scene, openURLContexts: URLContexts)
    }

    func scene(_ scene: UIScene, continue userActivity: NSUserActivity) {
        if let url = userActivity.webpageURL, handleFronteggURL(url) {
            return
        }
        SceneDelegateProxy.shared.scene(scene, continue: userActivity)
    }

    /// Passes Frontegg callback URLs to the SDK. Returns true when the URL was a Frontegg one.
    private func handleFronteggURL(_ url: URL) -> Bool {
        guard url.absoluteString.hasPrefix(FronteggAuth.shared.baseUrl) else {
            return false
        }
        return FronteggAuth.shared.handleOpenUrl(url)
    }
}
