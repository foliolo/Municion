package al.ahgitdevelopment.municion.ads

import al.ahgitdevelopment.municion.ui.BaseViewModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import timber.log.Timber
import javax.inject.Singleton

@Singleton
class RewardedAdCallbackManager : OnUserEarnedRewardListener {

    private lateinit var viewModel: BaseViewModel

    fun setViewModel(viewModel: BaseViewModel) {
        this.viewModel = viewModel
    }

    // override fun onUserEarnedReward(reward: RewardItem) {
    //     Timber.i("onUserEarnedReward: ${reward.amount}")
    //     viewModel.navigateToForm()
    // }
    //
    // override fun onRewardedAdOpened() {
    //     super.onRewardedAdOpened()
    //     Timber.i("onRewardedAdOpened")
    // }
    //
    // override fun onRewardedAdClosed() {
    //     super.onRewardedAdClosed()
    //     Timber.i("onRewardedAdClosed")
    //     viewModel.loadRewardedAd()
    // }
    //
    // override fun onRewardedAdFailedToShow(p0: AdError) {
    //     super.onRewardedAdFailedToShow(p0)
    //     Timber.i("onRewardedAdFailedToShow: ${p0.message}")
    //     viewModel.rewardCancel()
    // }

    override fun onUserEarnedReward(p0: RewardItem) {
        viewModel.navigateToForm()
    }
}
