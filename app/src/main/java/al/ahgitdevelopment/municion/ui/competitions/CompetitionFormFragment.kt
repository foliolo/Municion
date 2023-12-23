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
class CompetitionFormFragment : Fragment() {

    private lateinit var binding: FragmentFormCompetitionBinding
    private val viewModel: CompetitionFormViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFormCompetitionBinding.inflate(layoutInflater, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        requireActivity().title = getString(R.string.competition_toolbar_subtitle_new)

        return binding.root
    }

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as NavigationActivity).actionBar?.title = getString(R.string.property_toolbar_subtitle_new)

        viewModel.fabSaveCompetitionClicked.observe(viewLifecycleOwner) {
            Competition(
                description = binding.formCompetitionDescription.editText?.text.toString(),
                date = binding.formCompetitionDate.editText?.text.toString(),
                ranking = binding.formCompetitionRanking.editText?.text.toString(),
                points = binding.formCompetitionPoints.editText?.text.toString().toIntOrNull() ?: 0,
                place = binding.formCompetitionPlace.editText?.text.toString(),
            ).run {
                viewModel.savePurchase(this)
            }
        }

        viewModel.date.observe(viewLifecycleOwner) {
            DatePickerFragment { _, year, month, dayOfMonth ->
                "$dayOfMonth/$month/$year".let { binding.formCompetitionDate.editText?.setText(it) }
            }.show(parentFragmentManager, COMPETITION_DATE)
        }

        viewModel.closeForm.observe(viewLifecycleOwner) {
            findNavController().navigate(
                CompetitionFormFragmentDirections.actionCompetitionFormFragmentToCompetitionsFragment(),
            )
        }
    }

    companion object {
        const val COMPETITION_DATE = "competition_date_picker"
    }
}
