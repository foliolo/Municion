package al.ahgitdevelopment.municion.ui.purchases

import al.ahgitdevelopment.municion.datamodel.Purchase
import al.ahgitdevelopment.municion.di.IoDispatcher
import al.ahgitdevelopment.municion.repository.RepositoryContract
import al.ahgitdevelopment.municion.repository.firebase.RemoteStorageDataSourceContract
import al.ahgitdevelopment.municion.ui.BaseViewModel
import al.ahgitdevelopment.municion.utils.Event
import al.ahgitdevelopment.municion.utils.checkMaxFreeItems
import android.graphics.Bitmap
import android.view.View
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@Suppress("UNUSED_PARAMETER")
@HiltViewModel
class PurchasesViewModel @Inject constructor(
    private val repository: RepositoryContract,
    private val storageRepository: RemoteStorageDataSourceContract,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : BaseViewModel() {

    val purchases = repository.getPurchases()
        .catch { _exception.postValue(Event(it)) }
        .asLiveData()

    init {
        showProgressBar()
        _loadRewardedAd.postValue(Event(Unit))
    }

    fun fabClick(view: View?) {
        if (purchases.value!!.checkMaxFreeItems()) {
            _navigateToForm.postValue(Event(Unit))
        } else {
            showRewardedAdDialog()
        }
    }

    fun deletePurchase(purchaseId: String) = viewModelScope.launch(ioDispatcher) {
        repository.removePurchase(purchaseId)
    }

    fun addPurchase(purchase: Purchase) = viewModelScope.launch(ioDispatcher) {
        repository.savePurchase(purchase)
    }

    fun savePicture(bitmap: Bitmap?, purchase: Purchase) {
        showProgressBar()
        // Upload de image to firebase store and get the link
        storageRepository.saveItemImage(bitmap, purchase.id)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        Timber.e(it, "Error uploading purchase image to firebase storage")
                    }
                }
                storageRepository.getReference(task.result?.metadata?.path).downloadUrl
            }
            .addOnSuccessListener { imageUrl ->
                // Update item with the link
                repository.savePurchaseImageItem(purchase.id, imageUrl.toString())
                hideProgressBar()
            }
    }

    override fun navigateToForm() {
        _navigateToForm.postValue(Event(Unit))
    }

    override fun showRewardedAd() {
        _showRewardedAd.postValue(Event(Unit))
    }
}
