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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_form_purchase.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by Alberto on 24/05/2016.
 */
@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PurchaseFormFragment : Fragment() {

    private val viewModel: PurchaseFormViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding: FragmentFormPurchaseBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_form_purchase, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as NavigationActivity).actionBar?.title = getString(R.string.property_toolbar_title_new)

        viewModel.fabSavePurchaseClicked.observe(viewLifecycleOwner) {
            Purchase(
                id = 0,
                brand = form_purchase_brand.editText?.text.toString(),
                store = form_purchase_store.editText?.text.toString(),
                bore = form_purchase_bore.editText?.text.toString(),
                units = form_purchase_units.editText?.text.toString().toIntOrNull() ?: 0,
                price = form_purchase_price.editText?.text.toString().toDoubleOrNull() ?: 0.toDouble(),
                date = form_purchase_date.editText?.text.toString(),
                rating = form_purchase_rating.rating,
                weight = form_purchase_weight.editText?.text.toString().toIntOrNull() ?: 0,
                image = ""
            ).run {
                viewModel.savePurchase(this)
            }
        }

        viewModel.date.observe(viewLifecycleOwner) {
            DatePickerFragment { _, year, month, dayOfMonth ->
                "$dayOfMonth/$month/$year".let { form_purchase_date.editText?.setText(it) }
            }.show(parentFragmentManager, PURCHASE_DATE)
        }

        viewModel.closeForm.observe(viewLifecycleOwner) {
            parentFragmentManager.popBackStack()
        }
    }

    companion object {
        const val PURCHASE_DATE = "purchase_date_picker"
    }
}
