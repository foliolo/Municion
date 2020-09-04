package al.ahgitdevelopment.municion.ui.competitions

import al.ahgitdevelopment.municion.SingleLiveEvent
import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.repository.Repository
import al.ahgitdevelopment.municion.ui.BaseViewModel
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch
import javax.inject.Inject

class CompetitionsViewModel @Inject constructor(
    private val repository: Repository,
    firebaseAnalytics: FirebaseAnalytics,
    firebaseCrashlytics: FirebaseCrashlytics
) : BaseViewModel(firebaseAnalytics, firebaseCrashlytics) {

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
