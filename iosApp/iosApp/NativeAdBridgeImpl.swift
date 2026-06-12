import UIKit
import GoogleMobileAds
import Shared

/// Loads native ads through the Google Mobile Ads SDK and hands each `NativeAd` back to Kotlin as an
/// opaque handle (`Any`). Implements the Kotlin `NativeAdLoader` bridge.
final class IOSNativeAdLoader: NSObject, NativeAdLoader {
    private var loaders: [AdLoader] = []
    private var delegates: [NativeAdLoaderDelegateImpl] = []

    func load(adUnitId: String, count: Int32, onLoaded: @escaping (Any) -> Void) {
        let options = MultipleAdsAdLoaderOptions()
        options.numberOfAds = Int(count)

        let delegate = NativeAdLoaderDelegateImpl(onLoaded: onLoaded)
        delegates.append(delegate)

        let loader = AdLoader(
            adUnitID: adUnitId,
            rootViewController: currentRootViewController(),
            adTypes: [.native],
            options: [options]
        )
        loader.delegate = delegate
        loader.load(Request())
        loaders.append(loader)
    }

    func destroy(handles: [Any]) {
        loaders.removeAll()
        delegates.removeAll()
    }
}

private final class NativeAdLoaderDelegateImpl: NSObject, NativeAdLoaderDelegate {
    private let onLoaded: (Any) -> Void

    init(onLoaded: @escaping (Any) -> Void) {
        self.onLoaded = onLoaded
    }

    func adLoader(_ adLoader: AdLoader, didReceive nativeAd: NativeAd) {
        onLoaded(nativeAd)
    }

    func adLoader(_ adLoader: AdLoader, didFailToReceiveAdWithError error: Error) {
        // Ignored: an empty pool just renders no ad in the list.
    }
}

/// Builds the iOS native ad card (`NativeAdView`) for a previously loaded handle. Mirrors the Android
/// layout: a full-width media area on top (≥120pt so AdMob doesn't demonetize) with the "Publicidad"
/// badge + headline + body + CTA below.
final class IOSNativeAdViewFactory: NSObject, NativeAdViewFactory {
    private static let accent = UIColor(red: 0.42, green: 0.46, blue: 0.20, alpha: 1.0)
    private static let mediaHeight: CGFloat = 150

    func createNativeAdView(handle: Any) -> UIView {
        guard let nativeAd = handle as? NativeAd else { return UIView() }
        return Self.buildView(for: nativeAd)
    }

    private static func buildView(for nativeAd: NativeAd) -> UIView {
        let adView = NativeAdView()
        adView.backgroundColor = .secondarySystemBackground
        adView.layer.cornerRadius = 12
        adView.clipsToBounds = true

        let mediaView = MediaView()
        mediaView.contentMode = .scaleAspectFill
        mediaView.clipsToBounds = true
        mediaView.mediaContent = nativeAd.mediaContent
        mediaView.translatesAutoresizingMaskIntoConstraints = false
        adView.addSubview(mediaView)
        adView.mediaView = mediaView

        // High-contrast filled chip (mirrors the Android badge). A low-contrast plain label is not
        // reliably detected by the AdMob native ad validator, which flags "Ad attribution missing".
        let badge = PaddingLabel()
        badge.text = "Publicidad"
        badge.font = .systemFont(ofSize: 11, weight: .semibold)
        badge.textColor = .label
        badge.backgroundColor = .systemGray5
        badge.layer.cornerRadius = 4
        badge.clipsToBounds = true

        let headline = UILabel()
        headline.text = nativeAd.headline
        headline.font = .systemFont(ofSize: 16, weight: .semibold)
        headline.textColor = .label
        headline.numberOfLines = 1
        adView.headlineView = headline

        let body = UILabel()
        body.text = nativeAd.body
        body.font = .systemFont(ofSize: 13)
        body.textColor = .secondaryLabel
        body.numberOfLines = 2
        body.isHidden = (nativeAd.body ?? "").isEmpty
        adView.bodyView = body

        let infoStack = UIStackView(arrangedSubviews: [badge, headline, body])
        infoStack.axis = .vertical
        infoStack.spacing = 2
        infoStack.alignment = .leading
        infoStack.setContentHuggingPriority(.defaultLow, for: .horizontal)

        let cta = UIButton(type: .system)
        var config = UIButton.Configuration.filled()
        config.title = nativeAd.callToAction
        config.baseBackgroundColor = accent
        config.baseForegroundColor = .white
        config.cornerStyle = .capsule
        cta.configuration = config
        cta.isUserInteractionEnabled = false // The SDK handles the click via callToActionView.
        cta.isHidden = (nativeAd.callToAction ?? "").isEmpty
        cta.setContentHuggingPriority(.required, for: .horizontal)
        adView.callToActionView = cta

        let bottomStack = UIStackView(arrangedSubviews: [infoStack, cta])
        bottomStack.axis = .horizontal
        bottomStack.spacing = 12
        bottomStack.alignment = .center
        bottomStack.translatesAutoresizingMaskIntoConstraints = false
        adView.addSubview(bottomStack)

        NSLayoutConstraint.activate([
            mediaView.topAnchor.constraint(equalTo: adView.topAnchor),
            mediaView.leadingAnchor.constraint(equalTo: adView.leadingAnchor),
            mediaView.trailingAnchor.constraint(equalTo: adView.trailingAnchor),
            mediaView.heightAnchor.constraint(equalToConstant: mediaHeight),

            bottomStack.topAnchor.constraint(equalTo: mediaView.bottomAnchor, constant: 12),
            bottomStack.leadingAnchor.constraint(equalTo: adView.leadingAnchor, constant: 12),
            bottomStack.trailingAnchor.constraint(equalTo: adView.trailingAnchor, constant: -12),
            bottomStack.bottomAnchor.constraint(lessThanOrEqualTo: adView.bottomAnchor, constant: -12),
        ])

        adView.nativeAd = nativeAd
        return adView
    }
}

/// A `UILabel` with internal padding so a background-filled badge ("Publicidad") doesn't hug its text.
private final class PaddingLabel: UILabel {
    private let insets = UIEdgeInsets(top: 2, left: 6, bottom: 2, right: 6)

    override func drawText(in rect: CGRect) {
        super.drawText(in: rect.inset(by: insets))
    }

    override var intrinsicContentSize: CGSize {
        let size = super.intrinsicContentSize
        return CGSize(width: size.width + insets.left + insets.right,
                      height: size.height + insets.top + insets.bottom)
    }
}
