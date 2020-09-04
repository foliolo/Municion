package al.ahgitdevelopment.municion.ui.competitions

import al.ahgitdevelopment.municion.SingleLiveEvent
import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.repository.Repository
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class CompetitionFormViewModel @Inject constructor(
    private val repository: Repository
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
