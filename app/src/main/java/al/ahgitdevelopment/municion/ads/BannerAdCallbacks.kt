package al.ahgitdevelopment.municion.ads

import al.ahgitdevelopment.municion.databinding.ActivityNavigationBinding
import android.view.View
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import timber.log.Timber

class BannerAdCallbacks(private val binding: ActivityNavigationBinding) : AdListener() {
    override fun onAdLoaded() {
        Timber.v("onAdLoaded")
        binding.adView.visibility = View.VISIBLE
    }

    override fun onAdFailedToLoad(adError: LoadAdError) {
        Timber.v("onAdFailedToLoad")
        // Code to be executed when an ad request fails.
    }

    override fun onAdOpened() {
        Timber.v("onAdOpened")
        // Code to be executed when an ad opens an overlay that
        // covers the screen.
    }

    override fun onAdClicked() {
        Timber.v("onAdClicked")
        // Code to be executed when the user clicks on an ad.
    }

    override fun onAdClosed() {
        Timber.v("onAdClosed")
        // Code to be executed when the user is about to return
        // to the app after tapping on an ad.
    }
}
