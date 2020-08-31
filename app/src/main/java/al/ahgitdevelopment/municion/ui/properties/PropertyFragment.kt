package al.ahgitdevelopment.municion.ui.properties

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.PropertiesFragmentBinding
import al.ahgitdevelopment.municion.di.AppComponent
import al.ahgitdevelopment.municion.ui.RecyclerInterface
import al.ahgitdevelopment.municion.ui.licenses.LicenseAdapter
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import javax.inject.Inject

class PropertyFragment : Fragment(), RecyclerInterface {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    // private lateinit var propertiesAdapter: PropertiesAdapter

    private val viewModel: PropertyViewModel by viewModels {
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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.onCreatedView()
    }

    override fun RecyclerView?.undoDelete(viewHolder: LicenseAdapter.LicenseViewHolder) {
        TODO("Not yet implemented")
    }
}
