package al.ahgitdevelopment.municion.ui.competitions

import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.repository.RepositoryContract
import al.ahgitdevelopment.municion.utils.SingleLiveEvent
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompetitionFormViewModel @Inject constructor(
    private val repository: RepositoryContract,
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
