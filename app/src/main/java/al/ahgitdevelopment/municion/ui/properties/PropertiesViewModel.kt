package al.ahgitdevelopment.municion.ui.properties

import al.ahgitdevelopment.municion.datamodel.Property
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
class PropertiesViewModel @ViewModelInject constructor(
    private val repository: Repository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val _properties = MutableLiveData<List<Property>>()
    val properties: LiveData<List<Property>> = _properties

    val addProperty = SingleLiveEvent<Unit>()

    init {
        getProperties()
    }

    fun getProperties() {
        viewModelScope.launch {
            _properties.postValue(repository.getProperties())
        }
    }

    fun fabClick(view: View) {
        addProperty.call()
    }

    fun deleteProperty(propertyId: Long) {
        viewModelScope.launch {
            repository.removeProperty(propertyId)
        }
    }

    fun addProperty(property: Property) {
        wrapEspressoIdlingResource {
            viewModelScope.launch {
                repository.saveProperty(property)
                getProperties()
            }
        }
    }
}
