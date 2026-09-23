import UIKit
import Capacitor
import FronteggSwift

/*
 * Under UIScene, AppDelegate's application(_:open:) and application(_:continue:) are no longer
 * called, so the Frontegg login callback has to be handled here instead.
 */
class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?
    private var launchURLObserver: NSObjectProtocol?

    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
        guard let windowScene = scene as? UIWindowScene else { return }

        // With UISceneStoryboardFile set, UIKit has already created the window and its bridge.
        if window == nil {
            window = UIWindow(windowScene: windowScene)
            window?.rootViewController = CAPBridgeViewController()
            window?.makeKeyAndVisible()
        }

        // A link that launches the app arrives here, before the plugin has initialized Frontegg.
        let launchURLs = connectionOptions.urlContexts.map(\.url) + connectionOptions.userActivities.compactMap(\.webpageURL)
        if !launchURLs.isEmpty {
            handleFronteggURLsOnceCapacitorLoads(launchURLs)
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

    /// Waits for the first bridge appearance, by which time the plugin has initialized Frontegg.
    private func handleFronteggURLsOnceCapacitorLoads(_ urls: [URL]) {
        launchURLObserver = NotificationCenter.default.addObserver(forName: .capacitorViewDidAppear, object: nil, queue: .main) { [weak self] _ in
            guard let self else { return }
            if let observer = self.launchURLObserver {
                NotificationCenter.default.removeObserver(observer)
                self.launchURLObserver = nil
            }
            for url in urls where self.handleFronteggURL(url) {
                break
            }
        }
    }

    /// Passes the URL to the SDK, which also recognizes its custom-scheme callback. Returns true when it was a Frontegg one.
    private func handleFronteggURL(_ url: URL) -> Bool {
        return FronteggAuth.shared.handleOpenUrl(url)
    }
}
