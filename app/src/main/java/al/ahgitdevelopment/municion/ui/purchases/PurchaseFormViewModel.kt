package al.ahgitdevelopment.municion.ui.purchases

import al.ahgitdevelopment.municion.SingleLiveEvent
import al.ahgitdevelopment.municion.datamodel.Purchase
import al.ahgitdevelopment.municion.repository.Repository
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class PurchaseFormViewModel @Inject constructor(
    private val repository: Repository
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
