package al.ahgitdevelopment.municion.ui.licenses

import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.di.IoDispatcher
import al.ahgitdevelopment.municion.repository.RepositoryContract
import al.ahgitdevelopment.municion.ui.BaseViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("UNUSED_PARAMETER")
@HiltViewModel
class LicensesViewModel @Inject constructor(
    private val repository: RepositoryContract,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : BaseViewModel() {

    var licenses = repository.getLicenses()
        .catch { _exception.postValue(it) }
        .asLiveData()

    init {
        showProgressBar()
    }

    fun fabClick() {
        navigateToForm()
    }

    fun deleteLicense(licenseId: String) = viewModelScope.launch(ioDispatcher) {
        repository.removeLicense(licenseId)
    }

    fun addLicense(license: License) = viewModelScope.launch(ioDispatcher) {
        repository.saveLicense(license)
    }

    override fun navigateToForm() {
        _navigateToForm.postValue(Unit)
    }
}
