package al.ahgitdevelopment.municion.ui.competitions

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.CompetitionsFragmentBinding
import al.ahgitdevelopment.municion.di.AppComponent
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject

class CompetitionFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewViewModel: CompetitionViewModel by viewModels {
        viewModelFactory
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        AppComponent.create(requireContext()).inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding: CompetitionsFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.competitions_fragment, container, false)

        binding.viewModel = this.viewViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewViewModel.onCreatedView()
    }
}
