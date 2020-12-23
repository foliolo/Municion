package al.ahgitdevelopment.municion.dialogs

import al.ahgitdevelopment.municion.NavigationActivityViewModel
import al.ahgitdevelopment.municion.R
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels

class SupportDeveloperDialogFragment : DialogFragment() {

    private val viewModel: NavigationActivityViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder(it).run {
                setTitle(getString(R.string.dialog_support_developer_title))
                setMessage(getString(R.string.dialog_support_developer_message))
                setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                    viewModel.paymentSupportDeveloper()
                }
                setNegativeButton(getString(android.R.string.cancel)) { _, _ ->
                    dismiss()
                }
                create()
            }
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
