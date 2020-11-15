package al.ahgitdevelopment.municion.ui.competitions

import al.ahgitdevelopment.municion.NavigationActivity
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.FragmentFormCompetitionBinding
import al.ahgitdevelopment.municion.datamodel.Competition
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
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.android.synthetic.main.fragment_form_competition.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by Alberto on 24/05/2016.
 */
@AndroidEntryPoint
@ExperimentalCoroutinesApi
class CompetitionFormFragment : Fragment() {

    private val viewModel: CompetitionFormViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding: FragmentFormCompetitionBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_form_competition, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        activity?.toolbar?.subtitle = getString(R.string.competition_toolbar_subtitle_new)

        return binding.root
    }

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as NavigationActivity).actionBar?.title = getString(R.string.property_toolbar_subtitle_new)

        viewModel.fabSaveCompetitionClicked.observe(viewLifecycleOwner) {
            Competition(
                id = 0,
                description = form_competition_description.editText?.text.toString(),
                date = form_competition_date.editText?.text.toString(),
                ranking = form_competition_ranking.editText?.text.toString(),
                points = form_competition_points.editText?.text.toString().toIntOrNull() ?: 0,
                place = form_competition_place.editText?.text.toString(),
            ).run {
                viewModel.savePurchase(this)
            }
        }

        viewModel.date.observe(viewLifecycleOwner) {
            DatePickerFragment { _, year, month, dayOfMonth ->
                "$dayOfMonth/$month/$year".let { form_competition_date.editText?.setText(it) }
            }.show(parentFragmentManager, COMPETITION_DATE)
        }

        viewModel.closeForm.observe(viewLifecycleOwner) {
            parentFragmentManager.popBackStack()
        }
    }

    companion object {
        const val COMPETITION_DATE = "competition_date_picker"
    }
}
