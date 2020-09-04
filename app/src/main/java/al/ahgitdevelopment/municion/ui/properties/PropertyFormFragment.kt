package al.ahgitdevelopment.municion.ui.properties

import al.ahgitdevelopment.municion.NavigationActivity
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.FragmentFormPropertyBinding
import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.di.AppComponent
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_form_property.*
import javax.inject.Inject

/**
 * Created by Alberto on 24/05/2016.
 */
class PropertyFormFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: PropertyFormViewModel by viewModels {
        viewModelFactory
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AppComponent.create(requireContext()).inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding: FragmentFormPropertyBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_form_property, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as NavigationActivity).actionBar?.title = getString(R.string.property_toolbar_title_new)

        viewModel.fabSavePropertyClicked.observe(viewLifecycleOwner) {
            Property(
                id = 0,
                nickname = form_property_nickname.editText?.text.toString(),
                brand = form_property_brand.editText?.text.toString(),
                model = form_property_model.editText?.text.toString(),
                bore1 = form_property_bore_1.editText?.text.toString(),
                bore2 = form_property_bore_2.editText?.text.toString(),
                numId = form_property_num_id.editText?.text.toString()
            ).run {
                viewModel.saveProperty(this)
            }
        }

        viewModel.closeForm.observe(viewLifecycleOwner) {
            parentFragmentManager.popBackStack()
        }
    }
}