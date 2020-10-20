package al.ahgitdevelopment.municion.ui.purchases

import al.ahgitdevelopment.municion.SingleLiveEvent
import al.ahgitdevelopment.municion.datamodel.Purchase
import al.ahgitdevelopment.municion.repository.Repository
import al.ahgitdevelopment.municion.ui.BaseViewModel
import android.view.View
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch

@Suppress("UNUSED_PARAMETER")
class PurchasesViewModel @ViewModelInject constructor(
    private val repository: Repository,
    firebaseAnalytics: FirebaseAnalytics,
    firebaseCrashlytics: FirebaseCrashlytics,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel(firebaseAnalytics, firebaseCrashlytics) {

    lateinit var purchases: LiveData<List<Purchase>>

    val addPurchase = SingleLiveEvent<Unit>()

    init {
        getPurchases()
    }

    fun getPurchases() {
        viewModelScope.launch {
            purchases = repository.getPurchases()!!
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
        viewModelScope.launch {
            repository.savePurchase(purchase)
        }
    }
}
