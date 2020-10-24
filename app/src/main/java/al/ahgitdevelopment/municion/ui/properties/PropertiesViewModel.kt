package al.ahgitdevelopment.municion.ui.properties

import al.ahgitdevelopment.municion.SingleLiveEvent
import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.repository.Repository
import al.ahgitdevelopment.municion.ui.BaseViewModel
import android.view.View
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@Suppress("UNUSED_PARAMETER")
class PropertiesViewModel @ViewModelInject constructor(
    private val repository: Repository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    lateinit var properties: LiveData<List<Property>>

    val addProperty = SingleLiveEvent<Unit>()

    init {
        getProperties()
    }

    fun getProperties() {
        viewModelScope.launch {
            properties = repository.getProperties()!!
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
        viewModelScope.launch {
            repository.saveProperty(property)
        }
    }
}
