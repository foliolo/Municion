package al.ahgitdevelopment.municion.ui.licenses

import al.ahgitdevelopment.municion.datamodel.License
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
class LicensesViewModel @ViewModelInject constructor(
    private val repository: Repository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    val licenses: LiveData<List<License>> = repository.licenses.asLiveData()

    val addLicense = SingleLiveEvent<Unit>()

    fun fabClick(view: View) {
        addLicense.call()
    }

    fun deleteLicense(licenseId: Long) = viewModelScope.launch(ioDispatcher) {
        wrapEspressoIdlingResource {
            repository.removeLicense(licenseId)
        }
    }

    fun addLicense(license: License) = viewModelScope.launch(ioDispatcher) {
        wrapEspressoIdlingResource {
            repository.saveLicense(license)
        }
    }
}
