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
                // Screenshot mode (fastlane snapshot): no consent/ATT dialogs, no ads.
                if !ProcessInfo.processInfo.arguments.contains("-screenshotMode") {
                    AdsCoordinator.shared.startIfNeeded()
                }
            }
    }
}