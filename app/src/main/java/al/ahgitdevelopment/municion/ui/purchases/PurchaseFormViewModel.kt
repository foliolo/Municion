package al.ahgitdevelopment.municion.ui.purchases

import al.ahgitdevelopment.municion.datamodel.Purchase
import al.ahgitdevelopment.municion.repository.RepositoryContract
import al.ahgitdevelopment.municion.utils.SingleLiveEvent
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PurchaseFormViewModel @Inject constructor(
    private val repository: RepositoryContract,
) : ViewModel() {

    val fabSavePurchaseClicked = SingleLiveEvent<Unit>()
    val date = SingleLiveEvent<Unit>()
    val closeForm = SingleLiveEvent<Unit>()

    fun fabSavePurchaseClicked(view: View) {
        fabSavePurchaseClicked.call()
    }

    fun savePurchase(purchase: Purchase) {
        // TODO: perform field validations
        viewModelScope.launch {
            repository.savePurchase(purchase)
            closeForm.call()
        }
    }

    fun selectDate(view: View) {
        date.call()
    }
}
