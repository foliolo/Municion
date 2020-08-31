package al.ahgitdevelopment.municion.ui.licenses

import al.ahgitdevelopment.municion.SingleLiveEvent
import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.repository.Repository
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class LicenseFormViewModel @Inject constructor(
    private val repository: Repository
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
        // TODO: perform fields validations
        viewModelScope.launch {
            repository.saveLicense(license)
            closeForm.call()
        }
    }
}
