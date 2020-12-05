package al.ahgitdevelopment.municion.ui

import al.ahgitdevelopment.municion.repository.firebase.RemoteStorageDataSource.Companion.EVENT_CLOSE_APP
import al.ahgitdevelopment.municion.repository.firebase.RemoteStorageDataSource.Companion.EVENT_LOGOUT
import al.ahgitdevelopment.municion.utils.Event
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

open class BaseViewModel : ViewModel() {

    private val _progressBar = MutableLiveData<Event<Boolean>>()
    val progressBar: LiveData<Event<Boolean>> = _progressBar

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

    fun showProgressBar() {
        _progressBar.postValue(Event(true))
    }

    fun hideProgressBar() {
        _progressBar.postValue(Event(false))
    }
}
