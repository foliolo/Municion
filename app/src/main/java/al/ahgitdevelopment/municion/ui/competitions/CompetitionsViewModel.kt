package al.ahgitdevelopment.municion.ui.competitions

import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.di.IoDispatcher
import al.ahgitdevelopment.municion.repository.RepositoryContract
import al.ahgitdevelopment.municion.ui.BaseViewModel
import al.ahgitdevelopment.municion.utils.Event
import al.ahgitdevelopment.municion.utils.checkMaxFreeItems
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
        .catch { _exception.postValue(Event(it)) }
        .asLiveData()

    init {
        showProgressBar()
        loadRewardedAd()
    }

    fun fabClick(view: View?) {
        if (competitions.value!!.checkMaxFreeItems()) {
            _navigateToForm.postValue(Event(Unit))
        } else {
            showRewardedAdDialog()
        }
    }

    fun deleteCompetition(competitionId: String) = viewModelScope.launch(ioDispatcher) {
        wrapEspressoIdlingResource {
            repository.removeCompetition(competitionId)
        }
    }

    fun addCompetition(competition: Competition) = viewModelScope.launch(ioDispatcher) {
        wrapEspressoIdlingResource {
            repository.saveCompetition(competition)
        }
    }

    override fun navigateToForm() {
        _navigateToForm.postValue(Event(Unit))
    }

    override fun showRewardedAd() {
        _showRewardedAd.postValue(Event(Unit))
    }
}
