package al.ahgitdevelopment.municion.ui.properties

import al.ahgitdevelopment.municion.BaseFragment
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.PropertiesFragmentBinding
import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.ui.DeleteItemOnSwipe
import al.ahgitdevelopment.municion.ui.RecyclerInterface
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
class PropertiesFragment : BaseFragment(), RecyclerInterface, PropertyAdapterListener {
    private lateinit var binding: PropertiesFragmentBinding

    private lateinit var propertiesAdapter: PropertyAdapter

    private val viewModel: PropertiesViewModel by viewModels()

    private lateinit var auxProperty: Property

    private val getContent =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            viewModel.savePicture(bitmap, auxProperty)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = PropertiesFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigateToForm.observe(viewLifecycleOwner) {
            findNavController().navigate(
                PropertiesFragmentDirections.actionPropertiesFragmentToPropertyFormFragment()
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
                else -> activity?.findViewById<ContentLoadingProgressBar>(R.id.progressBar)?.hide()
            }
        }

        viewModel.properties.observe(viewLifecycleOwner) {
            propertiesAdapter = PropertyAdapter(this).apply {
                submitList(it.sortedBy { it.nickname })
                setHasStableIds(true)
            }

            binding.propertiesRecyclerView.apply {
                adapter = propertiesAdapter
                layoutManager = LinearLayoutManager(requireContext())

                ItemTouchHelper(
                    DeleteItemOnSwipe(object : DeleteItemOnSwipe.DeleteCallback {
                        override fun deleteOnSwipe(viewHolder: RecyclerView.ViewHolder) {
                            propertiesAdapter.currentList[viewHolder.adapterPosition]?.let {
                                viewModel.deleteProperty(it.id)
                            }

                            undoDelete(viewHolder)

                            adapter?.notifyDataSetChanged()
                        }
                    })
                ).attachToRecyclerView(this)
            }

            viewModel.hideProgressBar()
        }
    }

    override fun onResume() {
        super.onResume()

        (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(view?.rootView?.windowToken, 0)
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
        propertiesAdapter.currentList[(viewHolder as PropertyAdapter.PropertyViewHolder).adapterPosition]?.let { property ->
            Snackbar.make(
                binding.propertiesLayout,
                R.string.snackbar_undo_delete_message,
                Snackbar.LENGTH_LONG
            ).setAction(R.string.snackbar_undo_delete) {
                viewModel.addProperty(property)
                this?.adapter?.notifyDataSetChanged()
            }.show()
        }
    }

    override fun updateImage(property: Property) {
        // https://adambennett.dev/2020/03/introducing-the-activity-result-apis/
        auxProperty = property
        getContent.launch(null)
    }
}
