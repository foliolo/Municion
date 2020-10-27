package al.ahgitdevelopment.municion.ui.competitions

import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.repository.Repository
import al.ahgitdevelopment.municion.ui.BaseViewModel
import al.ahgitdevelopment.municion.utils.SingleLiveEvent
import android.view.View
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CompetitionsViewModel @ViewModelInject constructor(
    private val repository: Repository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    lateinit var competitions: LiveData<List<Competition>>

    val addCompetition = SingleLiveEvent<Unit>()

    init {
        getPurchases()
    }

    fun getPurchases() {
        viewModelScope.launch {
            competitions = repository.getCompetition()!!
        }
    }

    fun fabClick(view: View) {
        addCompetition.call()
    }

    fun deletePurchase(competitionId: Long) {
        viewModelScope.launch {
            repository.removeCompetition(competitionId)
        }
    }

    fun addPurchase(competition: Competition) {
        viewModelScope.launch {
            repository.saveCompetition(competition)
        }
    }
}
