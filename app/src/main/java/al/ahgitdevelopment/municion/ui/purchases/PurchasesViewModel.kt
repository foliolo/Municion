package al.ahgitdevelopment.municion.ui.purchases

import al.ahgitdevelopment.municion.datamodel.Purchase
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

@Suppress("UNUSED_PARAMETER")
class PurchasesViewModel @ViewModelInject constructor(
    private val repository: Repository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    val purchases: LiveData<List<Purchase>> = repository.purchases.asLiveData()

    val addPurchase = SingleLiveEvent<Unit>()

    fun fabClick(view: View) {
        addPurchase.call()
    }

    fun deletePurchase(purchaseId: Long) = viewModelScope.launch(ioDispatcher) {
        wrapEspressoIdlingResource {
            repository.removePurchase(purchaseId)
        }
    }

    fun addPurchase(purchase: Purchase) = viewModelScope.launch(ioDispatcher) {
        wrapEspressoIdlingResource {
            repository.savePurchase(purchase)
        }
    }
}
