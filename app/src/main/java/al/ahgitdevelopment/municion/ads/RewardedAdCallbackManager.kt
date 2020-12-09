package al.ahgitdevelopment.municion.ads

import al.ahgitdevelopment.municion.NavigationActivityViewModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import timber.log.Timber
import javax.inject.Singleton

@Singleton
class RewardedAdCallbackManager : RewardedAdCallback() {

    private var rewardsEarned = 0
    private lateinit var viewModel: NavigationActivityViewModel

    fun setViewModel(viewModel: NavigationActivityViewModel) {
        this.viewModel = viewModel
    }

    override fun onUserEarnedReward(reward: RewardItem) {
        Timber.i("onUserEarnedReward: ${reward.amount}")
        rewardsEarned += reward.amount
        Timber.i("rewards earned: $rewardsEarned")
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
    }
}
