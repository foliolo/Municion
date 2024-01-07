package al.ahgitdevelopment.municion.ui

import al.ahgitdevelopment.municion.repository.firebase.RemoteStorageDataSource.Companion.EVENT_CLOSE_APP
import al.ahgitdevelopment.municion.repository.firebase.RemoteStorageDataSource.Companion.EVENT_LOGOUT
import al.ahgitdevelopment.municion.utils.SingleLiveEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import java.io.Serializable

abstract class BaseViewModel : ViewModel(), Serializable {

    private val _progressBar = SingleLiveEvent<Boolean>()
    val progressBar: LiveData<Boolean> = _progressBar

    val _navigateToForm = SingleLiveEvent<Unit>()
    val navigateToForm: LiveData<Unit> = _navigateToForm

    private val _message = SingleLiveEvent<Int>()
    val message: LiveData<Int> = _message

    val _exception = SingleLiveEvent<Throwable>()
    val exception: LiveData<Throwable> = _exception

    abstract fun navigateToForm()

    fun recordLogoutEvent(analytics: FirebaseAnalytics) {
        analytics.logEvent(EVENT_LOGOUT, null)
    }

    fun clearUserData(analytics: FirebaseAnalytics) {
        analytics.setUserId(null)
    }

    fun closeApp(analytics: FirebaseAnalytics) {
        analytics.logEvent(EVENT_CLOSE_APP, null)
    }

    fun showProgressBar() {
        _progressBar.postValue(true)
    }

    fun hideProgressBar() {
        _progressBar.postValue(false)
    }
}
