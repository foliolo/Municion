package al.ahgitdevelopment.municion.ui.properties

import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.di.IoDispatcher
import al.ahgitdevelopment.municion.repository.RepositoryContract
import al.ahgitdevelopment.municion.repository.firebase.RemoteStorageDataSourceContract
import al.ahgitdevelopment.municion.ui.BaseViewModel
import al.ahgitdevelopment.municion.utils.Event
import al.ahgitdevelopment.municion.utils.SingleLiveEvent
import al.ahgitdevelopment.municion.utils.checkMaxFreeItems
import al.ahgitdevelopment.municion.utils.wrapEspressoIdlingResource
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
class PropertiesViewModel @Inject constructor(
    private val repository: RepositoryContract,
    private val storageRepository: RemoteStorageDataSourceContract,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : BaseViewModel() {

    val properties = repository.getProperties()
        .catch { _exception.postValue(Event(it)) }
        .asLiveData()

    val error = SingleLiveEvent<String>()

    init {
        showProgressBar()
        _loadRewardedAd.postValue(Event(Unit))
    }

    fun fabClick(view: View?) {
        if (properties.value!!.checkMaxFreeItems()) {
            navigateToForm()
        } else {
            showRewardedAdDialog()
        }
    }

    fun deleteProperty(propertyId: String) = viewModelScope.launch(ioDispatcher) {
        wrapEspressoIdlingResource {
            repository.removeProperty(propertyId)
        }
    }

    fun addProperty(property: Property) = viewModelScope.launch(ioDispatcher) {
        wrapEspressoIdlingResource {
            repository.saveProperty(property)
        }
    }

    fun savePicture(bitmap: Bitmap?, property: Property) {
        wrapEspressoIdlingResource {
            showProgressBar()
            // Upload de image to firebase store and get the link
            storageRepository.saveItemImage(bitmap, property.id).continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        Timber.e(it, "Error uploading property image to firebase storage")
                    }
                }
                storageRepository.getReference(task.result?.metadata?.path).downloadUrl
            }.addOnSuccessListener { imageUrl ->
                // Update item with the link
                repository.savePropertyImageItem(property.id, imageUrl.toString())
                hideProgressBar()
            }
        }
    }

    override fun navigateToForm() {
        _navigateToForm.postValue(Event(Unit))
    }

    override fun showRewardedAd() {
        _showRewardedAd.postValue(Event(Unit))
    }
}
