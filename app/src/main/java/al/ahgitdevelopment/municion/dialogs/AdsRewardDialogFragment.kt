package al.ahgitdevelopment.municion.dialogs

import al.ahgitdevelopment.municion.NavigationActivityViewModel
import al.ahgitdevelopment.municion.R
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels

class AdsRewardDialogFragment : DialogFragment() {

    private val viewModel: NavigationActivityViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder(it).run {
                setTitle(getString(R.string.dialog_ads_reward_title))
                setMessage(
                    """
                    |Remove ads for 1 hour viewing a video.
                    |
                    |Delete ads forever with a small donation.
                    |
                    |Thanks anyway to use the app! 
                    """.trimMargin()
                )
                setPositiveButton(getString(R.string.dialog_ads_reward_btn_show)) { _, _ ->
                    viewModel.loadRewardAds()
                }
                setNeutralButton(getString(R.string.dialog_ads_reward_btn_remove)) { _, _ ->
                    viewModel.removeAds()
                }
                setNegativeButton(getString(R.string.dialog_ads_reward_btn_cancel)) { _, _ ->
                    dismiss()
                }
                create()
            }
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
