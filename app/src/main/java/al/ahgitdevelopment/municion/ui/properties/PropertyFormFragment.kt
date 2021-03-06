package al.ahgitdevelopment.municion.ui.properties

import al.ahgitdevelopment.municion.NavigationActivity
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.FragmentFormPropertyBinding
import al.ahgitdevelopment.municion.datamodel.Property
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.android.synthetic.main.fragment_form_property.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by Alberto on 24/05/2016.
 */
@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PropertyFormFragment : Fragment() {

    private val viewModel: PropertyFormViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding: FragmentFormPropertyBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_form_property, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        activity?.toolbar?.subtitle = getString(R.string.property_toolbar_subtitle_new)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as NavigationActivity).actionBar?.title = getString(R.string.property_toolbar_subtitle_new)

        viewModel.fabSavePropertyClicked.observe(viewLifecycleOwner) {
            Property(
                nickname = form_property_nickname.editText?.text.toString(),
                brand = form_property_brand.editText?.text.toString(),
                model = form_property_model.editText?.text.toString(),
                bore1 = form_property_bore_1.editText?.text.toString(),
                bore2 = form_property_bore_2.editText?.text.toString(),
                numId = form_property_num_id.editText?.text.toString(),
                image = ""
            ).run {
                viewModel.saveProperty(this)
            }
        }

        viewModel.closeForm.observe(viewLifecycleOwner) {
            findNavController().navigate(
                PropertyFormFragmentDirections.actionPropertyFormFragmentToPropertiesFragment()
            )
        }
    }
}
