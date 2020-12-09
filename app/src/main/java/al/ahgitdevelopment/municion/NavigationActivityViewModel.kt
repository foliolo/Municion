package al.ahgitdevelopment.municion

import al.ahgitdevelopment.municion.utils.Event
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.random.Random

class NavigationActivityViewModel : ViewModel() {

    private val _showAdDialog = MutableLiveData<Event<Unit>>()
    val showAdDialog: LiveData<Event<Unit>> = _showAdDialog

    private val _loadRewardedAd = MutableLiveData<Event<Unit>>()
    val loadRewardedAd: LiveData<Event<Unit>> = _loadRewardedAd

    private val _showRewardedAd = MutableLiveData<Event<Unit>>()
    val showRewardedAd: LiveData<Event<Unit>> = _showRewardedAd

    private val _removeAds = MutableLiveData<Event<Unit>>()
    val removeAds: LiveData<Event<Unit>> = _removeAds

    init {
        showAdsDialog()
    }

    private fun showAdsDialog() {
        // TODO: Only if the user do not pay for the ads

        Handler(Looper.getMainLooper()).apply {
            postDelayed(
                {
                    _showAdDialog.postValue(Event(Unit))
                },
                // Show the dialog un a random time between 1 to 10 min
                if (BuildConfig.DEBUG) {
                    10 * 1000 // 10 sec
                } else {
                    Random.nextInt(1, 10).toLong() * 60 * 1000 // 1-10 min
                }
            )
        }
    }

    fun loadRewardAds() {
        _loadRewardedAd.postValue(Event(Unit))
    }

    fun showRewardAds() {
        _showRewardedAd.postValue(Event(Unit))
    }

    fun removeAds() {
        _removeAds.postValue(Event(Unit))
    }
}
