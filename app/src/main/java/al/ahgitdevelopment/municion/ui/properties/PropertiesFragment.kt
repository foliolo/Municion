package al.ahgitdevelopment.municion.ui.properties

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.PropertiesFragmentBinding
import al.ahgitdevelopment.municion.di.AppComponent
import al.ahgitdevelopment.municion.ui.DeleteItemOnSwipe
import al.ahgitdevelopment.municion.ui.RecyclerInterface
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.properties_fragment.*
import javax.inject.Inject

class PropertiesFragment : Fragment(), RecyclerInterface {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var propertiesAdapter: PropertyAdapter

    private val viewModel: PropertiesViewModel by viewModels {
        viewModelFactory
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        AppComponent.create(requireContext()).inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding: PropertiesFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.properties_fragment, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.addProperty.observe(viewLifecycleOwner, {
            findNavController().navigate(R.id.propertyFormFragment)
        })

        viewModel.properties.observe(viewLifecycleOwner) {
            propertiesAdapter = PropertyAdapter().apply {
                submitList(it)
                setHasStableIds(true)
            }

            properties_recycler_view.apply {
                adapter = propertiesAdapter
                layoutManager = LinearLayoutManager(requireContext())

                ItemTouchHelper(
                    DeleteItemOnSwipe(object : DeleteItemOnSwipe.DeleteCallback {
                        override fun deleteOnSwipe(viewHolder: RecyclerView.ViewHolder) {
                            viewModel.deleteProperty(viewHolder.itemId)

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
        viewModel.getProperties()

        (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).let {
            it.hideSoftInputFromWindow(view?.rootView?.windowToken, 0)
        }
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
