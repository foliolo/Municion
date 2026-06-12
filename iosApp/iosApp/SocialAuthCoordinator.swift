import AuthenticationServices
import CryptoKit
import FirebaseAuth
import FirebaseCore
import GoogleSignIn
import Security
import Shared
import UIKit

final class SocialAuthCoordinator: NSObject {
    static let shared = SocialAuthCoordinator()

    private var appleCompletion: ((String?) -> Void)?
    private var currentNonce: String?

    func registerBridges() {
        IosGoogleAuthBridge.shared.signInHandler = { completion in
            self.signInWithGoogle { payload in
                _ = completion(payload)
            }
        }
        IosAppleAuthBridge.shared.signInHandler = { completion in
            self.signInWithApple { payload in
                _ = completion(payload)
            }
        }
        // Apple token revocation (account deletion). GitLive's Firebase wrapper does not expose
        // revokeToken, so we call the native Firebase iOS SDK here.
        IosAppleRevokeBridge.shared.revokeHandler = { authorizationCode, completion in
            self.revokeAppleToken(authorizationCode: authorizationCode) { errorMessage in
                _ = completion(errorMessage)
            }
        }
    }

    private func signInWithGoogle(completion: @escaping (String?) -> Void) {
        DispatchQueue.main.async {
            guard
                let presentingViewController = currentRootViewController(),
                let clientId = FirebaseApp.app()?.options.clientID
            else {
                completion(nil)
                return
            }

            GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientId)
            GIDSignIn.sharedInstance.signIn(withPresenting: presentingViewController) { result, error in
                guard
                    error == nil,
                    let user = result?.user,
                    let idToken = user.idToken?.tokenString
                else {
                    completion(nil)
                    return
                }

                let accessToken = user.accessToken.tokenString
                completion("\(idToken)|||accessToken|||\(accessToken)")
            }
        }
    }

    private func signInWithApple(completion: @escaping (String?) -> Void) {
        DispatchQueue.main.async {
            let nonce = self.randomNonceString()
            self.currentNonce = nonce
            self.appleCompletion = completion

            let request = ASAuthorizationAppleIDProvider().createRequest()
            request.requestedScopes = [.fullName, .email]
            request.nonce = self.sha256(nonce)

            let controller = ASAuthorizationController(authorizationRequests: [request])
            controller.delegate = self
            controller.presentationContextProvider = self
            controller.performRequests()
        }
    }

    /// Revokes the user's Sign in with Apple token via the native Firebase SDK. Passes back nil on
    /// success or a localized error message on failure (the Kotlin side treats it as best-effort).
    private func revokeAppleToken(authorizationCode: String, completion: @escaping (String?) -> Void) {
        DispatchQueue.main.async {
            Auth.auth().revokeToken(withAuthorizationCode: authorizationCode) { error in
                completion(error?.localizedDescription)
            }
        }
    }

    private func finishAppleSignIn(payload: String?) {
        let completion = appleCompletion
        appleCompletion = nil
        currentNonce = nil
        completion?(payload)
    }

    private func randomNonceString(length: Int = 32) -> String {
        let charset = Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")
        var result = ""
        var remainingLength = length

        while remainingLength > 0 {
            var randomBytes = [UInt8](repeating: 0, count: 16)
            let status = SecRandomCopyBytes(kSecRandomDefault, randomBytes.count, &randomBytes)
            if status != errSecSuccess {
                return UUID().uuidString.replacingOccurrences(of: "-", with: "")
            }

            randomBytes.forEach { byte in
                guard remainingLength > 0 else { return }
                if Int(byte) < charset.count {
                    result.append(charset[Int(byte)])
                    remainingLength -= 1
                }
            }
        }

        return result
    }

    private func sha256(_ input: String) -> String {
        let inputData = Data(input.utf8)
        let hashedData = SHA256.hash(data: inputData)
        return hashedData.map { String(format: "%02x", $0) }.joined()
    }
}

extension SocialAuthCoordinator: ASAuthorizationControllerDelegate {
    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization authorization: ASAuthorization
    ) {
        guard
            let credential = authorization.credential as? ASAuthorizationAppleIDCredential,
            let tokenData = credential.identityToken,
            let idToken = String(data: tokenData, encoding: .utf8),
            let rawNonce = currentNonce
        else {
            finishAppleSignIn(payload: nil)
            return
        }

        // The authorizationCode is required to revoke the Apple token on account deletion. It is
        // short-lived and single-use, so it is only consumed during a fresh re-auth at delete time.
        let authCode = credential.authorizationCode.flatMap { String(data: $0, encoding: .utf8) } ?? ""
        finishAppleSignIn(payload: "\(idToken)|||\(rawNonce)|||\(authCode)")
    }

    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError error: Error
    ) {
        finishAppleSignIn(payload: nil)
    }
}

extension SocialAuthCoordinator: ASAuthorizationControllerPresentationContextProviding {
    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        currentRootViewController()?.view.window ?? UIWindow()
    }
}
