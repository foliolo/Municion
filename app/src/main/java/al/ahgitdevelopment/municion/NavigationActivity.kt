package al.ahgitdevelopment.municion

import al.ahgitdevelopment.municion.ads.BannerAdCallbacks
import al.ahgitdevelopment.municion.ads.RewardedAdCallbackManager
import al.ahgitdevelopment.municion.ads.RewardedAdLoadCallbackManager
import al.ahgitdevelopment.municion.databinding.ActivityNavigationBinding
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardedAd
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNavigationBinding
    private val viewModel: NavigationActivityViewModel by viewModels()

    @Inject
    lateinit var rewardedAdLoadCallbackManager: RewardedAdLoadCallbackManager

    @Inject
    lateinit var rewardedAdCallbackManager: RewardedAdCallbackManager

    private lateinit var rewardedAd: RewardedAd

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.propertiesFragment,
                R.id.purchasesFragment,
                R.id.licensesFragment,
                R.id.competitionsFragment
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.title = getString(R.string.app_name)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            setToolbarSubtitle("")

            when (destination.id) {
                R.id.loginPasswordFragment -> {
                    setToolbarSubtitle(getString(R.string.login))
                    binding.navView.visibility = View.GONE
                }
                R.id.propertiesFragment -> {
                    setToolbarSubtitle(getString(R.string.section_properties_title))
                    binding.navView.visibility = View.VISIBLE
                }
                R.id.purchasesFragment -> {
                    setToolbarSubtitle(getString(R.string.section_purchases_title))
                    binding.navView.visibility = View.VISIBLE
                }
                R.id.licensesFragment -> {
                    setToolbarSubtitle(getString(R.string.section_licenses_title))
                    binding.navView.visibility = View.VISIBLE
                }
                R.id.competitionsFragment -> {
                    setToolbarSubtitle(getString(R.string.section_competitions_title))
                    binding.navView.visibility = View.VISIBLE
                }
            }
        }

        setupAds()
    }

    override fun onStart() {
        super.onStart()
        rewardedAdLoadCallbackManager.setViewModel(viewModel)
        rewardedAdCallbackManager.setViewModel(viewModel)

        viewModel.showAdDialog.observe(
            this,
            {
                findNavController(R.id.nav_host_fragment).navigate(R.id.adsRewardDialogFragment)
            }
        )

        viewModel.loadRewardedAd.observe(
            this,
            {
                rewardedAd = RewardedAd(this@NavigationActivity, getString(R.string.rewarded_ads_id)).apply {
                    loadAd(AdRequest.Builder().build(), rewardedAdLoadCallbackManager)
                }
            }
        )

        viewModel.showRewardedAd.observe(
            this,
            {
                rewardedAd.show(this@NavigationActivity, rewardedAdCallbackManager)
            }
        )

        viewModel.removeAds.observe(
            this,
            {
                TODO("Not yet implemented")
            }
        )
    }

    private fun setToolbarSubtitle(subtitle: String) {
        supportActionBar?.subtitle = subtitle
    }

    private fun setupAds() {
        AdRequest.Builder().build().let {
            binding.adView.adListener = BannerAdCallbacks(binding)
            binding.adView.loadAd(it)
        }
    }
}
