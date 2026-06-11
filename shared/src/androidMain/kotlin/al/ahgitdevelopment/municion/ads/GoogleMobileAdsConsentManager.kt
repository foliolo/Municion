package al.ahgitdevelopment.municion.ads

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

/**
 * Thin wrapper over the User Messaging Platform (UMP) SDK. Gathers GDPR consent and exposes the
 * privacy-options entry point reused by the Settings screen. Singleton per process.
 */
class GoogleMobileAdsConsentManager private constructor(
    context: Context,
) {
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    val isPrivacyOptionsRequired: Boolean
        get() =
            consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /** Requests the latest consent info and shows the consent form if required. */
    fun gatherConsent(
        activity: Activity,
        onComplete: (FormError?) -> Unit,
    ) {
        val params = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error -> onComplete(error) }
            },
            { requestError -> onComplete(requestError) },
        )
    }

    /** Re-opens the privacy options form (entry point from Settings). */
    fun showPrivacyOptionsForm(
        activity: Activity,
        onComplete: (FormError?) -> Unit = {},
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error -> onComplete(error) }
    }

    companion object {
        @Volatile
        private var instance: GoogleMobileAdsConsentManager? = null

        fun getInstance(context: Context): GoogleMobileAdsConsentManager =
            instance ?: synchronized(this) {
                instance ?: GoogleMobileAdsConsentManager(context.applicationContext).also { instance = it }
            }
    }
}
