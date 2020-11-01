package al.ahgitdevelopment.municion.ui

import al.ahgitdevelopment.municion.repository.firebase.FirebaseImageRepository.Companion.EVENT_CLOSE_APP
import al.ahgitdevelopment.municion.repository.firebase.FirebaseImageRepository.Companion.EVENT_LOGOUT
import androidx.lifecycle.ViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

open class BaseViewModel : ViewModel() {

    fun recordLogoutEvent(analytics: FirebaseAnalytics) {
        analytics.logEvent(EVENT_LOGOUT, null)
    }

    fun clearUserData(analytics: FirebaseAnalytics, crashlytics: FirebaseCrashlytics) {
        analytics.setUserId(null)
        crashlytics.setUserId("")
    }

    fun closeApp(analytics: FirebaseAnalytics) {
        analytics.logEvent(EVENT_CLOSE_APP, null)
    }
}
