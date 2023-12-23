package al.ahgitdevelopment.municion.ui.purchases

import al.ahgitdevelopment.municion.NavigationActivity
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.FragmentFormPurchaseBinding
import al.ahgitdevelopment.municion.datamodel.Purchase
import al.ahgitdevelopment.municion.ui.dialogs.DatePickerFragment
import android.annotation.SuppressLint
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
class PurchaseFormFragment : Fragment() {

    private lateinit var binding: FragmentFormPurchaseBinding
    private val viewModel: PurchaseFormViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFormPurchaseBinding.inflate(inflater, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        requireActivity().title = getString(R.string.purchase_toolbar_subtitle_new)

        return binding.root
    }

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as NavigationActivity).actionBar?.title = getString(R.string.property_toolbar_subtitle_new)

        viewModel.fabSavePurchaseClicked.observe(viewLifecycleOwner) {
            Purchase(
                brand = binding.formPurchaseBrand.editText?.text.toString(),
                store = binding.formPurchaseStore.editText?.text.toString(),
                bore = binding.formPurchaseBore.editText?.text.toString(),
                units = binding.formPurchaseUnits.editText?.text.toString().toIntOrNull() ?: 0,
                price = binding.formPurchasePrice.editText?.text.toString().toDoubleOrNull() ?: 0.toDouble(),
                date = binding.formPurchaseDate.editText?.text.toString(),
                rating = binding.formPurchaseRating.rating,
                weight = binding.formPurchaseWeight.editText?.text.toString().toIntOrNull() ?: 0,
                image = "",
            ).run {
                viewModel.savePurchase(this)
            }
        }

        viewModel.date.observe(viewLifecycleOwner) {
            DatePickerFragment { _, year, month, dayOfMonth ->
                "$dayOfMonth/$month/$year".let { binding.formPurchaseDate.editText?.setText(it) }
            }.show(parentFragmentManager, PURCHASE_DATE)
        }

        viewModel.closeForm.observe(viewLifecycleOwner) {
            findNavController().navigate(
                PurchaseFormFragmentDirections.actionPurchaseFormFragmentToPurchasesFragment(),
            )
        }
    }

    companion object {
        const val PURCHASE_DATE = "purchase_date_picker"
    }
}
