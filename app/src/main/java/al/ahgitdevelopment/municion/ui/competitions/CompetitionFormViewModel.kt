package al.ahgitdevelopment.municion.ui.competitions

import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.repository.RepositoryContract
import al.ahgitdevelopment.municion.utils.SingleLiveEvent
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompetitionFormViewModel @Inject constructor(
    private val repository: RepositoryContract,
) : ViewModel() {

    private val _fabSaveCompetitionClicked = SingleLiveEvent<Unit>()
    val fabSaveCompetitionClicked: LiveData<Unit> = _fabSaveCompetitionClicked
    private val _date = SingleLiveEvent<Unit>()
    val date: LiveData<Unit> = _date
    private val _closeForm = SingleLiveEvent<Unit>()
    val closeForm: LiveData<Unit> = _closeForm

    fun fabSaveCompetitionClicked(view: View) {
        _fabSaveCompetitionClicked.call()
    }

    fun savePurchase(competition: Competition) {
        // TODO: perform field validations
        viewModelScope.launch {
            repository.saveCompetition(competition)
            _closeForm.call()
        }
    }

    fun selectDate(view: View) {
        _date.call()
    }
}
