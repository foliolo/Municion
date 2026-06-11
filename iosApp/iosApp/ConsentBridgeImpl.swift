import UIKit
import UserMessagingPlatform
import Shared

/// Swift implementation of the Kotlin `PrivacyConsentBridge`: drives the "Opciones de privacidad"
/// button in Settings via the UMP privacy-options form.
final class IOSPrivacyConsentBridge: NSObject, PrivacyConsentBridge {
    var isPrivacyOptionsRequired: Bool {
        ConsentInformation.shared.privacyOptionsRequirementStatus == .required
    }

    func showPrivacyOptionsForm() {
        guard let vc = currentRootViewController() else { return }
        ConsentForm.presentPrivacyOptionsForm(from: vc) { _ in }
    }
}

/// Gathers UMP consent at startup and reports whether ads can be requested afterwards.
enum ConsentCoordinator {
    static func gatherConsent(completion: @escaping (Bool) -> Void) {
        let parameters = RequestParameters()
        ConsentInformation.shared.requestConsentInfoUpdate(with: parameters) { error in
            if error != nil {
                completion(ConsentInformation.shared.canRequestAds)
                return
            }
            guard let vc = currentRootViewController() else {
                completion(ConsentInformation.shared.canRequestAds)
                return
            }
            ConsentForm.loadAndPresentIfRequired(from: vc) { _ in
                completion(ConsentInformation.shared.canRequestAds)
            }
        }
    }
}
