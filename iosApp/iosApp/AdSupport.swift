import UIKit

/// Resolves the top-most view controller of the active scene. Used as the `rootViewController` for
/// banner/native ad requests and to present the UMP consent / privacy-options forms.
func currentRootViewController() -> UIViewController? {
    let scenes = UIApplication.shared.connectedScenes
    let windowScene = (scenes.first { $0.activationState == .foregroundActive } as? UIWindowScene)
        ?? (scenes.first as? UIWindowScene)
    let keyWindow = windowScene?.windows.first { $0.isKeyWindow } ?? windowScene?.windows.first
    var top = keyWindow?.rootViewController
    while let presented = top?.presentedViewController {
        top = presented
    }
    return top
}
