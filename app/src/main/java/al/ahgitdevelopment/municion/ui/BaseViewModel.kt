package al.ahgitdevelopment.municion.ui

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.repository.firebase.RemoteStorageDataSource.Companion.EVENT_CLOSE_APP
import al.ahgitdevelopment.municion.repository.firebase.RemoteStorageDataSource.Companion.EVENT_LOGOUT
import al.ahgitdevelopment.municion.utils.Event
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import java.io.Serializable

abstract class BaseViewModel : ViewModel(), Serializable {

    private val _progressBar = MutableLiveData<Event<Boolean>>()
    val progressBar: LiveData<Event<Boolean>> = _progressBar

    val _navigateToForm = MutableLiveData<Event<Unit>>()
    val navigateToForm: LiveData<Event<Unit>> = _navigateToForm

    private val _message = MutableLiveData<Event<Int>>()
    val message: LiveData<Event<Int>> = _message

    val _exception = MutableLiveData<Event<Throwable>>()
    val exception: LiveData<Event<Throwable>> = _exception

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
        _progressBar.postValue(Event(true))
    }

    fun hideProgressBar() {
        _progressBar.postValue(Event(false))
    }

    fun removeMaxLimitation() {
        _message.postValue(Event(R.string.toast_under_construction))
    }
}
