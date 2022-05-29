package al.ahgitdevelopment.municion.ui.licenses

import al.ahgitdevelopment.municion.NavigationActivity
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.FragmentFormLicenseBinding
import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.ui.dialogs.DatePickerFragment
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
class LicenseFormFragment : Fragment() {

    private lateinit var binding: FragmentFormLicenseBinding
    private val viewModel: LicenseFormViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentFormLicenseBinding.inflate(layoutInflater, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        requireActivity().title = getString(R.string.license_toolbar_subtitle_new)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as NavigationActivity).actionBar?.title = getString(R.string.license_toolbar_subtitle_new)

        viewModel.issueDate.observe(viewLifecycleOwner) {
            DatePickerFragment { _, year, month, dayOfMonth ->
                "$dayOfMonth/$month/$year".let { binding.formLicenseDateIssue.editText?.setText(it) }
            }.show(parentFragmentManager, ISSUE_DATE_PICKER)
        }

        viewModel.expiryDate.observe(viewLifecycleOwner) {
            DatePickerFragment { _, year, month, dayOfMonth ->
                "$dayOfMonth/$month/$year".let { binding.formLicenseDateExpiry.editText?.setText(it) }
            }.show(parentFragmentManager, EXPIRY_DATE_PICKER)
        }

        viewModel.fabSaveLicenseClicked.observe(viewLifecycleOwner) {
            License(
                licenseName = binding.formLicenseName.editText?.text.toString(),
                licenseNumber = binding.formLicenseNumber.editText?.text.toString(),
                issueDate = binding.formLicenseDateIssue.editText?.text.toString(),
                expiryDate = binding.formLicenseDateExpiry.editText?.text.toString(),
                insuranceNumber = binding.formLicenseInsuranceNumber.editText?.text.toString()
            ).run {
                viewModel.saveLicense(this)
            }
        }

        viewModel.closeForm.observe(viewLifecycleOwner) {
            findNavController().navigate(
                LicenseFormFragmentDirections.actionLicenseFormFragmentToLicensesFragment()
            )
        }
    }

    companion object {
        const val ISSUE_DATE_PICKER = "issue_date_picker"
        const val EXPIRY_DATE_PICKER = "expiry_date_picker"
    }
}
