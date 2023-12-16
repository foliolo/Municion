package al.ahgitdevelopment.municion.dialogs

import androidx.fragment.app.DialogFragment

class AdsRewardDialogFragment : DialogFragment() {

    // private val safeArgs: AdsRewardDialogFragmentArgs by navArgs()
    //
    // override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    //     return activity?.let {
    //         // Use the Builder class for convenient dialog construction
    //         AlertDialog.Builder(it).run {
    //             setTitle(getString(R.string.dialog_ads_reward_title))
    //             setMessage(getString(R.string.dialog_ads_reward_message))
    //             setPositiveButton(getString(R.string.dialog_ads_reward_btn_show)) { _, _ ->
    //                 when (safeArgs.viewModel) {
    //                     is LicensesViewModel -> (safeArgs.viewModel as LicensesViewModel).showRewardedAd()
    //                     is PropertiesViewModel -> (safeArgs.viewModel as PropertiesViewModel)showRewardedAd()
    //                     is PurchasesViewModel -> (safeArgs.viewModel as PurchasesViewModel).showRewardedAd()
    //                     is CompetitionsViewModel -> (safeArgs.viewModel as CompetitionsViewModel).showRewardedAd()
    //                 }
    //             }
    //             setNeutralButton(getString(R.string.dialog_ads_reward_btn_cancel)) { _, _ ->
    //                 dismiss()
    //             }
    //             setNegativeButton(getString(R.string.dialog_ads_reward_btn_remove_max_limitation)) { _, _ ->
    //                 safeArgs.viewModel.removeMaxLimitation()
    //             }
    //             create()
    //         }
    //     } ?: throw IllegalStateException("Activity cannot be null")
    // }
}
