package al.ahgitdevelopment.municion.ui.properties

import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.di.IoDispatcher
import al.ahgitdevelopment.municion.repository.RepositoryContract
import al.ahgitdevelopment.municion.ui.BaseViewModel
import al.ahgitdevelopment.municion.utils.Event
import al.ahgitdevelopment.municion.utils.SingleLiveEvent
import al.ahgitdevelopment.municion.utils.wrapEspressoIdlingResource
import android.view.View
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    val properties = repository.getProperties()
        .catch { error.postValue(it.message) }
        .asLiveData()

    private val _navigateToForm = MutableLiveData<Event<Unit>>()
    val navigateToForm: LiveData<Event<Unit>> = _navigateToForm

    val error = SingleLiveEvent<String>()

    fun fabClick(view: View?) {
        _navigateToForm.postValue(Event(Unit))
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
}
