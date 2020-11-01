package al.ahgitdevelopment.municion.ui.licenses

import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.repository.database.Repository
import al.ahgitdevelopment.municion.utils.SingleLiveEvent
import android.view.View
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@Suppress("UNUSED_PARAMETER")
class LicenseFormViewModel @ViewModelInject constructor(
    private val repository: Repository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val issueDate = SingleLiveEvent<Unit>()
    val expiryDate = SingleLiveEvent<Unit>()
    val fabSaveLicenseClicked = SingleLiveEvent<Unit>()
    val closeForm = SingleLiveEvent<Unit>()

    fun selectIssueDate(view: View) {
        issueDate.call()
    }

    fun selectExpiryDate(view: View) {
        expiryDate.call()
    }

    fun fabSaveLicenseClicked(view: View) {
        fabSaveLicenseClicked.call()
    }

    fun saveLicense(license: License) {
        // TODO: perform field validations
        viewModelScope.launch {
            repository.saveLicense(license)
            closeForm.call()
        }
    }
}
