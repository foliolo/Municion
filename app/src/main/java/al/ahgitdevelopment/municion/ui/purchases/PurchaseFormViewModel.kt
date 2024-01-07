package al.ahgitdevelopment.municion.ui.purchases

import al.ahgitdevelopment.municion.datamodel.Purchase
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
class PurchaseFormViewModel @Inject constructor(
    private val repository: RepositoryContract,
) : ViewModel() {

    private val _fabSavePurchaseClicked = SingleLiveEvent<Unit>()
    val fabSavePurchaseClicked:LiveData<Unit> = _fabSavePurchaseClicked

    private val _date = SingleLiveEvent<Unit>()
    val date: LiveData<Unit> = _date

    private val _closeForm = SingleLiveEvent<Unit>()
    val closeForm :LiveData<Unit> = _closeForm

    fun fabSavePurchaseClicked(view: View) {
        _fabSavePurchaseClicked.call()
    }

    fun savePurchase(purchase: Purchase) {
        // TODO: perform field validations
        viewModelScope.launch {
            repository.savePurchase(purchase)
            _closeForm.call()
        }
    }

    fun selectDate(view: View) {
        _date.call()
    }
}
