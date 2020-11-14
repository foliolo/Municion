package al.ahgitdevelopment.municion.ui.properties

import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.di.IoDispatcher
import al.ahgitdevelopment.municion.repository.RepositoryContract
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@Suppress("UNUSED_PARAMETER")
class PropertiesViewModel @ViewModelInject constructor(
    private val repository: RepositoryContract,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    lateinit var properties: LiveData<List<Property>>

    val addProperty = SingleLiveEvent<Unit>()

    val error = SingleLiveEvent<String>()

    init {
        viewModelScope.launch(ioDispatcher) {
            properties = repository.getProperties()
                .catch { error.postValue(it.message) }
                .asLiveData()
        }
    }

    fun fabClick(view: View) {
        addProperty.call()
    }

    fun deleteProperty(propertyId: Long) = viewModelScope.launch(ioDispatcher) {
        wrapEspressoIdlingResource {
            repository.removeProperty(propertyId)
        }
    }

    fun addProperty(property: Property) = viewModelScope.launch(ioDispatcher) {
        wrapEspressoIdlingResource {
            repository.saveProperty(property)
        }
    }
}
