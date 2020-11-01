package al.ahgitdevelopment.municion.ui.licenses

import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.repository.database.RepositoryInterface
import al.ahgitdevelopment.municion.ui.BaseViewModel
import al.ahgitdevelopment.municion.utils.SingleLiveEvent
import android.view.View
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@Suppress("UNUSED_PARAMETER")
class LicensesViewModel @ViewModelInject constructor(
    private val repository: RepositoryInterface,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    lateinit var licenses: LiveData<List<License>>

    val addLicense = SingleLiveEvent<Unit>()

    init {
        getLicenses()
    }

    fun getLicenses() {
        viewModelScope.launch {
            licenses = repository.getLicenses()!!
        }
    }

    fun fabClick(view: View) {
        addLicense.call()
    }

    fun deleteLicense(licenseId: Long) {
        viewModelScope.launch {
            repository.removeLicense(licenseId)
        }
    }

    fun addLicense(license: License) {
        viewModelScope.launch {
            repository.saveLicense(license)
        }
    }
}
