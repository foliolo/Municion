package al.ahgitdevelopment.municion.ui

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

    val _showRewardedAdDialog = MutableLiveData<Event<Unit>>()
    val showRewardedAdDialog: LiveData<Event<Unit>> = _showRewardedAdDialog

    val _loadRewardedAd = MutableLiveData<Event<Unit>>()
    val loadRewardedAd: LiveData<Event<Unit>> = _loadRewardedAd

    val _showRewardedAd = MutableLiveData<Event<Unit>>()
    val showRewardedAd: LiveData<Event<Unit>> = _showRewardedAd

    private val _removeAds = MutableLiveData<Event<Unit>>()
    val removeAds: LiveData<Event<Unit>> = _removeAds

    abstract fun showRewardedAdDialog()
    abstract fun showRewardedAd()
    abstract fun loadRewardedAd()
    abstract fun rewardObtain()
    abstract fun rewardCancel()

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
        _removeAds.postValue(Event(Unit))
    }
}
