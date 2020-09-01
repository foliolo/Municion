package al.ahgitdevelopment.municion.ui.properties

import al.ahgitdevelopment.municion.SingleLiveEvent
import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.repository.Repository
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("UNUSED_PARAMETER")
class PropertyFormViewModel @Inject constructor(
    private val repository: Repository
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
