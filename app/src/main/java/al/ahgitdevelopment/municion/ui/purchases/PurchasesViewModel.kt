package al.ahgitdevelopment.municion.ui.purchases

import al.ahgitdevelopment.municion.datamodel.Purchase
import al.ahgitdevelopment.municion.repository.database.Repository
import al.ahgitdevelopment.municion.ui.BaseViewModel
import al.ahgitdevelopment.municion.utils.SingleLiveEvent
import al.ahgitdevelopment.municion.utils.wrapEspressoIdlingResource
import android.view.View
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@Suppress("UNUSED_PARAMETER")
class PurchasesViewModel @ViewModelInject constructor(
    private val repository: Repository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val _purchases = MutableLiveData<List<Purchase>>()
    val purchases: LiveData<List<Purchase>> = _purchases

    val addPurchase = SingleLiveEvent<Unit>()

    init {
        getPurchases()
    }

    private fun getPurchases() {
        viewModelScope.launch {
            _purchases.postValue(repository.getPurchases())
        }
    }

    fun fabClick(view: View) {
        addPurchase.call()
    }

    fun deletePurchase(purchaseId: Long) {
        viewModelScope.launch {
            repository.removePurchase(purchaseId)
        }
    }

    fun addPurchase(purchase: Purchase) {
        wrapEspressoIdlingResource {
            viewModelScope.launch {
                repository.savePurchase(purchase)
                getPurchases()
            }
        }
    }
}
