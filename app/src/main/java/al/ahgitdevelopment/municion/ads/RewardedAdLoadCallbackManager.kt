package al.ahgitdevelopment.municion.ads

import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import timber.log.Timber

class RewardedAdLoadCallbackManager : RewardedAdLoadCallback() {

    override fun onRewardedAdLoaded() {
        super.onRewardedAdLoaded()
        Timber.i("onRewardedAdLoaded")
    }

    override fun onRewardedAdFailedToLoad(error: LoadAdError) {
        super.onRewardedAdFailedToLoad(error)
        Timber.i("onRewardedAdFailedToLoad: ${error.message}")
    }
}
