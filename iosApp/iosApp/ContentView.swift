import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(edges: .all)
            .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
            .onAppear {
                // Screenshot mode (fastlane snapshot): no consent/ATT dialogs, no ads. Debug-only
                // QA path — release builds never read the launch argument and always start ads.
                #if DEBUG
                let isScreenshotMode = ProcessInfo.processInfo.arguments.contains("-screenshotMode")
                #else
                let isScreenshotMode = false
                #endif
                if !isScreenshotMode {
                    AdsCoordinator.shared.startIfNeeded()
                }
            }
    }
}