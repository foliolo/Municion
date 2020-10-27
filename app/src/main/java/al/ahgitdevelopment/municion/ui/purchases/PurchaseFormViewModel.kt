package al.ahgitdevelopment.municion.ui.purchases

import al.ahgitdevelopment.municion.datamodel.Purchase
import al.ahgitdevelopment.municion.repository.Repository
import al.ahgitdevelopment.municion.utils.SingleLiveEvent
import android.view.View
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PurchaseFormViewModel @ViewModelInject constructor(
    private val repository: Repository,
    @Assisted private val savedStateHandle: SavedStateHandle
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
