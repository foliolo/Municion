package al.ahgitdevelopment.municion.ui.purchases

import al.ahgitdevelopment.municion.BaseFragment
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.PurchasesFragmentBinding
import al.ahgitdevelopment.municion.datamodel.Purchase
import al.ahgitdevelopment.municion.ui.DeleteItemOnSwipe
import al.ahgitdevelopment.municion.ui.RecyclerInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PurchasesFragment : BaseFragment(), RecyclerInterface, PurchaseAdapterListener {

    private lateinit var binding: PurchasesFragmentBinding
    private lateinit var purchaseAdapter: PurchaseAdapter

    private val viewModel: PurchasesViewModel by viewModels()

    private lateinit var auxPurchase: Purchase

    @SuppressWarnings
    private val getContent =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            viewModel.savePicture(bitmap, auxPurchase)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = PurchasesFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigateToForm.observe(viewLifecycleOwner) {
            findNavController().navigate(
                PurchasesFragmentDirections.actionPurchasesFragmentToPurchaseFormFragment(),
            )
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            Toast.makeText(
                requireContext(),
                requireContext().getString(message),
                Toast.LENGTH_LONG,
            ).show()
        }

        viewModel.exception.observe(viewLifecycleOwner) { exception ->
            Toast.makeText(
                requireContext(),
                exception.message,
                Toast.LENGTH_LONG,
            ).show()
        }

        viewModel.progressBar.observe(viewLifecycleOwner) {
            when (it) {
                true -> activity?.findViewById<ContentLoadingProgressBar>(R.id.progressBar)?.show()
                else -> activity?.findViewById<ContentLoadingProgressBar>(R.id.progressBar)?.hide()
            }
        }

        viewModel.purchases.observe(viewLifecycleOwner) {
            purchaseAdapter = PurchaseAdapter(this).apply {
                submitList(it.sortedBy { it.brand })
                setHasStableIds(true)
            }

            binding.purchasesRecyclerView.apply {
                adapter = purchaseAdapter
                layoutManager = LinearLayoutManager(requireContext())

                ItemTouchHelper(
                    DeleteItemOnSwipe(
                        object : DeleteItemOnSwipe.DeleteCallback {
                            override fun deleteOnSwipe(viewHolder: RecyclerView.ViewHolder) {
                                purchaseAdapter.currentList[viewHolder.adapterPosition]?.let {
                                    viewModel.deletePurchase(it.id)
                                }

                                undoDelete(viewHolder)

                                adapter?.notifyDataSetChanged()
                            }
                        },
                    ),
                ).attachToRecyclerView(this)
            }

            viewModel.hideProgressBar()
        }
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
        purchaseAdapter.currentList[
            (viewHolder as PurchaseAdapter.PurchaseViewHolder).adapterPosition,
        ]?.let { purchase ->
            Snackbar.make(
                binding.purchasesLayout,
                R.string.snackbar_undo_delete_message,
                Snackbar.LENGTH_LONG,
            ).setAction(R.string.snackbar_undo_delete) {
                viewModel.addPurchase(purchase)
                this?.adapter?.notifyDataSetChanged()
            }.show()
        }
    }

    override fun updateImage(purchase: Purchase) {
        // https://adambennett.dev/2020/03/introducing-the-activity-result-apis/
        auxPurchase = purchase
        getContent.launch(null)
    }
}
