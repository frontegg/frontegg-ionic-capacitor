// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "FronteggIonicCapacitor",
    platforms: [.iOS(.v15)],
    products: [
        .library(
            name: "FronteggIonicCapacitor",
            targets: ["FronteggNativePlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "8.0.0"),
        .package(url: "https://github.com/frontegg/frontegg-ios-swift.git", exact: "1.3.12")
    ],
    targets: [
        .target(
            name: "FronteggNativePlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm"),
                .product(name: "FronteggSwift", package: "frontegg-ios-swift")
            ],
            path: "ios/Plugin",
            exclude: ["FronteggNativePlugin.h", "Info.plist"])
    ]
)
