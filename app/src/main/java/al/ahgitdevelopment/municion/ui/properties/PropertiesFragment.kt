package al.ahgitdevelopment.municion.ui.properties

import al.ahgitdevelopment.municion.BaseFragment
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.PropertiesFragmentBinding
import al.ahgitdevelopment.municion.ui.DeleteItemOnSwipe
import al.ahgitdevelopment.municion.ui.RecyclerInterface
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.properties_fragment.*

@AndroidEntryPoint
class PropertiesFragment : BaseFragment(), RecyclerInterface {

    private lateinit var propertiesAdapter: PropertyAdapter

    private val viewModel: PropertiesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding: PropertiesFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.properties_fragment, container, false)
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

        viewModel.properties.observe(viewLifecycleOwner) {
            propertiesAdapter = PropertyAdapter().apply {
                submitList(it.sortedBy { it.nickname })
                setHasStableIds(true)
            }

            properties_recycler_view.apply {
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
                viewModel.clearUserData(analytics, crashlytics)
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
                properties_layout,
                R.string.snackbar_undo_delete_message,
                Snackbar.LENGTH_LONG
            ).setAction(R.string.snackbar_undo_delete) {
                viewModel.addProperty(property)
                this?.adapter?.notifyDataSetChanged()
            }.show()
        }
    }
}
