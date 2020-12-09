package al.ahgitdevelopment.municion.ads

import al.ahgitdevelopment.municion.NavigationActivityViewModel
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import timber.log.Timber

class RewardedAdLoadCallbackManager : RewardedAdLoadCallback() {

    private lateinit var viewModel: NavigationActivityViewModel

    fun setViewModel(viewModel: NavigationActivityViewModel) {
        this.viewModel = viewModel
    }

    override fun onRewardedAdLoaded() {
        super.onRewardedAdLoaded()
        Timber.i("onRewardedAdLoaded")
        viewModel.showRewardAds()
    }

    override fun onRewardedAdFailedToLoad(p0: LoadAdError) {
        super.onRewardedAdFailedToLoad(p0)
        Timber.i("onRewardedAdFailedToLoad: ${p0.message}")
    }
}
