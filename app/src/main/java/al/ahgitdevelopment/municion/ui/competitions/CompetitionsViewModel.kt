package al.ahgitdevelopment.municion.ui.competitions

import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.di.IoDispatcher
import al.ahgitdevelopment.municion.repository.RepositoryContract
import al.ahgitdevelopment.municion.ui.BaseViewModel
import al.ahgitdevelopment.municion.utils.SingleLiveEvent
import al.ahgitdevelopment.municion.utils.wrapEspressoIdlingResource
import android.view.View
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class CompetitionsViewModel @ViewModelInject constructor(
    private val repository: RepositoryContract,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    val competitions = repository.getCompetitions()
        .catch { error.postValue(it) }
        .asLiveData()

    val navigateToForm = SingleLiveEvent<Unit>()

    val error = SingleLiveEvent<Throwable>()

    fun fabClick(view: View?) {
        navigateToForm.call()
    }

    fun deleteCompetition(competitionId: Long) = viewModelScope.launch(ioDispatcher) {
        wrapEspressoIdlingResource {
            repository.removeCompetition(competitionId)
        }
    }

    fun addCompetition(competition: Competition) = viewModelScope.launch(ioDispatcher) {
        wrapEspressoIdlingResource {
            repository.saveCompetition(competition)
        }
    }
}
