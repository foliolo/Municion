package al.ahgitdevelopment.municion.ads

import al.ahgitdevelopment.municion.ui.BaseViewModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import timber.log.Timber
import javax.inject.Singleton

@Singleton
class RewardedAdCallbackManager : RewardedAdCallback() {

    private lateinit var viewModel: BaseViewModel

    fun setViewModel(viewModel: BaseViewModel) {
        this.viewModel = viewModel
    }

    override fun onUserEarnedReward(reward: RewardItem) {
        Timber.i("onUserEarnedReward: ${reward.amount}")
        viewModel.rewardObtain()
    }

    override fun onRewardedAdOpened() {
        super.onRewardedAdOpened()
        Timber.i("onRewardedAdOpened")
    }

    override fun onRewardedAdClosed() {
        super.onRewardedAdClosed()
        Timber.i("onRewardedAdClosed")
    }

    override fun onRewardedAdFailedToShow(p0: AdError) {
        super.onRewardedAdFailedToShow(p0)
        Timber.i("onRewardedAdFailedToShow: ${p0.message}")
        viewModel.rewardCancel()
    }
}
