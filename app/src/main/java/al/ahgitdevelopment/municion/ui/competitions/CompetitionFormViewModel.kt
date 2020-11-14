package al.ahgitdevelopment.municion.ui.competitions

import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.repository.RepositoryContract
import al.ahgitdevelopment.municion.utils.SingleLiveEvent
import android.view.View
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CompetitionFormViewModel @ViewModelInject constructor(
    private val repository: RepositoryContract,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val fabSaveCompetitionClicked = SingleLiveEvent<Unit>()
    val date = SingleLiveEvent<Unit>()
    val closeForm = SingleLiveEvent<Unit>()

    fun fabSaveCompetitionClicked(view: View) {
        fabSaveCompetitionClicked.call()
    }

    fun savePurchase(competition: Competition) {
        // TODO: perform field validations
        viewModelScope.launch {
            repository.saveCompetition(competition)
            closeForm.call()
        }
    }

    fun selectDate(view: View) {
        date.call()
    }
}
