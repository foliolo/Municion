package al.ahgitdevelopment.municion.ads

import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import timber.log.Timber

class RewardedAdLoadCallbackManager : RewardedAdLoadCallback() {
    override fun onAdFailedToLoad(error: LoadAdError) {
        super.onAdFailedToLoad(error)
        Timber.i("onRewardedAdFailedToLoad: ${error.message}")
    }

    override fun onAdLoaded(add: RewardedAd) {
        super.onAdLoaded(add)
        Timber.i("onRewardedAdLoaded")
    }
}
