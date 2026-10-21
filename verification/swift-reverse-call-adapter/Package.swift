// swift-tools-version: 6.0
import Foundation
import PackageDescription

let environment = ProcessInfo.processInfo.environment
guard let integrationPackage = environment["SWIFT_REVERSE_INTEGRATION_PACKAGE"],
      let nativeProducts = environment["SWIFT_REVERSE_NATIVE_PRODUCTS"] else {
    fatalError("Read README.md: a separately verified coherent Integration package and Native archive are required")
}
let package = Package(
    name: "ReverseCallChecks",
    platforms: [.macOS(.v15)],
    dependencies: [.package(name: "Integration", path: integrationPackage)],
    targets: [
        .target(name: "ReverseCallAdapter"),
        .testTarget(
            name: "ReverseCallAdapterTests",
            dependencies: ["ReverseCallAdapter", .product(name: "IntegrationLibrary", package: "Integration")],
            linkerSettings: [.unsafeFlags(["-L", nativeProducts, "-lIntegration"])])
    ],
    swiftLanguageModes: [.v6]
)
