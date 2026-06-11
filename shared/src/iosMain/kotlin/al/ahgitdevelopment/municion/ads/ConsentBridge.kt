package al.ahgitdevelopment.municion.ads

/**
 * Swift-implemented UMP consent entry point for iOS. Registered from `iOSApp.swift`; drives the
 * "privacy options" button in Settings via [rememberConsentOptions].
 */
interface PrivacyConsentBridge {
    val isPrivacyOptionsRequired: Boolean

    fun showPrivacyOptionsForm()
}

object ConsentBridge {
    var bridge: PrivacyConsentBridge? = null
}
