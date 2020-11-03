package al.ahgitdevelopment.municion.ui.competitions

import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.di.IoDispatcher
import al.ahgitdevelopment.municion.repository.database.Repository
import al.ahgitdevelopment.municion.ui.BaseViewModel
import al.ahgitdevelopment.municion.utils.SingleLiveEvent
import al.ahgitdevelopment.municion.utils.wrapEspressoIdlingResource
import android.view.View
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch

class CompetitionsViewModel @ViewModelInject constructor(
    private val repository: Repository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    val competitions: LiveData<List<Competition>> = repository.competitions.asLiveData()

    val addCompetition = SingleLiveEvent<Unit>()

    fun fabClick(view: View) {
        addCompetition.call()
    }

    fun deletePurchase(competitionId: Long) = viewModelScope.launch(ioDispatcher) {
        wrapEspressoIdlingResource {
            repository.removeCompetition(competitionId)
        }
    }

    fun addPurchase(competition: Competition) = viewModelScope.launch(ioDispatcher) {
        wrapEspressoIdlingResource {
            repository.saveCompetition(competition)
        }
    }
}
