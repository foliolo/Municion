package al.ahgitdevelopment.municion.ui.licenses

import al.ahgitdevelopment.municion.BaseFragment
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.LicensesFragmentBinding
import al.ahgitdevelopment.municion.ui.DeleteItemOnSwipe
import al.ahgitdevelopment.municion.ui.RecyclerInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.ContentLoadingProgressBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.licenses_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class LicensesFragment @Inject constructor() : BaseFragment(), RecyclerInterface {

    private lateinit var licensesAdapter: LicenseAdapter

    private val viewModel: LicensesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val binding: LicensesFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.licenses_fragment, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigateToForm.observe(viewLifecycleOwner) {
            findNavController().navigate(
                LicensesFragmentDirections.actionLicensesFragmentToLicenseFormFragment()
            )
        }

        viewModel.message.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { message ->
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(message),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        viewModel.exception.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { exception ->
                Toast.makeText(
                    requireContext(),
                    exception.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        viewModel.progressBar.observe(viewLifecycleOwner) {
            when (it.getContentIfNotHandled()) {
                true -> activity?.findViewById<ContentLoadingProgressBar>(R.id.progressBar)?.show()
                false -> activity?.findViewById<ContentLoadingProgressBar>(R.id.progressBar)?.hide()
            }
        }

        viewModel.licenses.observe(viewLifecycleOwner) {
            licensesAdapter = LicenseAdapter().apply {
                submitList(it.sortedBy { it.licenseName })
                setHasStableIds(true)
            }

            licenses_recycler_view.apply {
                adapter = licensesAdapter
                layoutManager = LinearLayoutManager(requireContext())

                ItemTouchHelper(
                    DeleteItemOnSwipe(object : DeleteItemOnSwipe.DeleteCallback {
                        override fun deleteOnSwipe(viewHolder: RecyclerView.ViewHolder) {
                            licensesAdapter.currentList[viewHolder.adapterPosition]?.let {
                                viewModel.deleteLicense(it.id)
                            }

                            undoDelete(viewHolder)

                            adapter?.notifyDataSetChanged()
                        }
                    })
                ).attachToRecyclerView(this)
            }

            viewModel.hideProgressBar()
        }

        viewModel.showRewardedAdDialog.observe(viewLifecycleOwner) {
            findNavController().navigate(
                LicensesFragmentDirections.actionLicensesFragmentToAdsRewardDialogFragment(viewModel)
            )
        }

        viewModel.showRewardedAd.observe(viewLifecycleOwner) {
            rewardedAd.show(requireActivity(), rewardedAdCallbackManager)
        }

        viewModel.loadRewardedAd.observe(viewLifecycleOwner) {
            rewardedAd = RewardedAd(requireContext(), getString(R.string.rewarded_ads_id)).apply {
                loadAd(AdRequest.Builder().build(), rewardedAdLoadCallbackManager)
            }
        }

        viewModel.removeAds.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), getString(R.string.toast_under_construction), Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onStart() {
        super.onStart()
        rewardedAdCallbackManager.setViewModel(viewModel)
    }

    override fun signOut() {
        AuthUI.getInstance()
            .signOut(requireContext())
            .addOnCompleteListener {
                viewModel.recordLogoutEvent(analytics)
                viewModel.clearUserData(analytics)
                findNavController().navigate(R.id.loginPasswordFragment)
            }
    }

    override fun settings() {
        Toast.makeText(requireContext(), "Settings click", Toast.LENGTH_SHORT).show()
    }

    override fun tutorial() {
        findNavController().navigate(R.id.tutorialViewPagerFragment)
    }

    override fun finish() {
        viewModel.closeApp(analytics)
        requireActivity().finish()
    }

    override fun RecyclerView?.undoDelete(viewHolder: RecyclerView.ViewHolder) {
        licensesAdapter.currentList[(viewHolder as LicenseAdapter.LicenseViewHolder).adapterPosition]?.let { license ->
            Snackbar.make(
                licenses_layout,
                R.string.snackbar_undo_delete_message,
                Snackbar.LENGTH_LONG
            ).setAction(R.string.snackbar_undo_delete) {
                viewModel.addLicense(license)
                this?.adapter?.notifyDataSetChanged()
            }.show()
        }
    }
}
