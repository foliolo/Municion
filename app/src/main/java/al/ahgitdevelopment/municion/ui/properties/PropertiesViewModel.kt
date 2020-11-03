package al.ahgitdevelopment.municion.ui.properties

import al.ahgitdevelopment.municion.datamodel.Property
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
class PropertiesViewModel @ViewModelInject constructor(
    private val repository: Repository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    val properties: LiveData<List<Property>> = repository.properties.asLiveData()

    val addProperty = SingleLiveEvent<Unit>()

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
