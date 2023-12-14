package al.ahgitdevelopment.municion.ui.competitions

import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.di.IoDispatcher
import al.ahgitdevelopment.municion.repository.RepositoryContract
import al.ahgitdevelopment.municion.ui.BaseViewModel
import al.ahgitdevelopment.municion.utils.Event
import al.ahgitdevelopment.municion.utils.checkMaxFreeItems
import android.view.View
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompetitionsViewModel @Inject constructor(
    private val repository: RepositoryContract,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
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
        repository.removeCompetition(competitionId)
    }

    fun addCompetition(competition: Competition) = viewModelScope.launch(ioDispatcher) {
        repository.saveCompetition(competition)
    }

    override fun navigateToForm() {
        _navigateToForm.postValue(Event(Unit))
    }

    override fun showRewardedAd() {
        _showRewardedAd.postValue(Event(Unit))
    }
}
