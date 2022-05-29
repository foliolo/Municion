package al.ahgitdevelopment.municion

import al.ahgitdevelopment.municion.ads.RewardedAdCallbackManager
import al.ahgitdevelopment.municion.ads.RewardedAdLoadCallbackManager
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

abstract class BaseFragment : Fragment() {

    @Inject
    lateinit var analytics: FirebaseAnalytics

    @Inject
    lateinit var rewardedAdLoadCallbackManager: RewardedAdLoadCallbackManager

    @Inject
    lateinit var rewardedAdCallbackManager: RewardedAdCallbackManager

    lateinit var rewardedAd: RewardedAd

    abstract fun signOut()
    abstract fun settings()
    abstract fun tutorial()
    abstract fun finish()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        // requireActivity().toolbar?.visibility = View.VISIBLE

        setHasOptionsMenu(true)

        (requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(view.rootView?.windowToken, 0)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.menu_settings -> {
                // Toast.makeText(requireContext(), "Settings click", Toast.LENGTH_SHORT).show()
                settings()
            }
            R.id.menu_log_out -> {
                signOut()
            }
            R.id.menu_tutorial -> {
                tutorial()
            }
            R.id.menu_support_developer -> {
                findNavController().navigate(R.id.supportDeveloperDialogFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val MAX_FREE_ITEMS = 3
    }
}
