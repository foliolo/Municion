package al.ahgitdevelopment.municion.ui.properties

import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.repository.RepositoryContract
import al.ahgitdevelopment.municion.utils.SingleLiveEvent
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("UNUSED_PARAMETER")
@HiltViewModel
class PropertyFormViewModel @Inject constructor(
    private val repository: RepositoryContract,
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
