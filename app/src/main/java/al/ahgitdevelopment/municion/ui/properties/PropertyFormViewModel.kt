package al.ahgitdevelopment.municion.ui.properties

import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.repository.RepositoryContract
import al.ahgitdevelopment.municion.utils.SingleLiveEvent
import android.view.View
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@Suppress("UNUSED_PARAMETER")
class PropertyFormViewModel @ViewModelInject constructor(
    private val repository: RepositoryContract,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val fabSavePropertyClicked = SingleLiveEvent<Unit>()
    val closeForm = SingleLiveEvent<Unit>()

    fun fabSavePropertyClicked(view: View) {
        fabSavePropertyClicked.call()
    }

    fun saveProperty(property: Property) {
        // TODO: perform field validations
        viewModelScope.launch {
            repository.saveProperty(property)
            closeForm.call()
        }
    }
}
