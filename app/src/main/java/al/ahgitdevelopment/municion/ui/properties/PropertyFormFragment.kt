package al.ahgitdevelopment.municion.ui.properties

import al.ahgitdevelopment.municion.NavigationActivity
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.FragmentFormPropertyBinding
import al.ahgitdevelopment.municion.datamodel.Property
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by Alberto on 24/05/2016.
 */
@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PropertyFormFragment : Fragment() {

    private lateinit var binding: FragmentFormPropertyBinding
    private val viewModel: PropertyFormViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentFormPropertyBinding.inflate(inflater, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        requireActivity().title = getString(R.string.property_toolbar_subtitle_new)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as NavigationActivity).actionBar?.title = getString(R.string.property_toolbar_subtitle_new)

        viewModel.fabSavePropertyClicked.observe(viewLifecycleOwner) {
            Property(
                nickname = binding.formPropertyNickname.editText?.text.toString(),
                brand = binding.formPropertyBrand.editText?.text.toString(),
                model = binding.formPropertyModel.editText?.text.toString(),
                bore1 = binding.formPropertyBore1.editText?.text.toString(),
                bore2 = binding.formPropertyBore2.editText?.text.toString(),
                numId = binding.formPropertyNumId.editText?.text.toString(),
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
