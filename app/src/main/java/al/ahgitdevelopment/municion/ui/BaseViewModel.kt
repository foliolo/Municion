package al.ahgitdevelopment.municion.ui

import al.ahgitdevelopment.municion.di.FirebaseModule
import androidx.lifecycle.ViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

open class BaseViewModel(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val firebaseCrashlytics: FirebaseCrashlytics
) : ViewModel() {

    fun recordLogoutEvent() {
        firebaseAnalytics.logEvent(FirebaseModule.EVENT_LOGOUT, null)
    }

    fun clearUserData() {
        firebaseAnalytics.setUserId(null)
        firebaseCrashlytics.setUserId("")
    }
}
