package al.ahgitdevelopment.municion.ui.licenses

import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.di.IoDispatcher
import al.ahgitdevelopment.municion.repository.RepositoryContract
import al.ahgitdevelopment.municion.ui.BaseViewModel
import al.ahgitdevelopment.municion.utils.Event
import al.ahgitdevelopment.municion.utils.checkMaxFreeItems
import al.ahgitdevelopment.municion.utils.wrapEspressoIdlingResource
import android.view.View
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@Suppress("UNUSED_PARAMETER")
class LicensesViewModel @ViewModelInject constructor(
    private val repository: RepositoryContract,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    var licenses = repository.getLicenses()
        .catch { _exception.postValue(Event(it)) }
        .asLiveData()

    init {
        showProgressBar()
        loadRewardedAd()
    }

    fun fabClick(view: View?) {
        if (licenses.value!!.checkMaxFreeItems()) {
            navigateToForm()
        } else {
            showRewardedAdDialog()
        }
    }

    fun deleteLicense(licenseId: String) = viewModelScope.launch(ioDispatcher) {
        wrapEspressoIdlingResource {
            repository.removeLicense(licenseId)
        }
    }

    fun addLicense(license: License) = viewModelScope.launch(ioDispatcher) {
        wrapEspressoIdlingResource {
            repository.saveLicense(license)
        }
    }

    override fun navigateToForm() {
        _navigateToForm.postValue(Event(Unit))
    }

    override fun showRewardedAd() {
        _showRewardedAd.postValue(Event(Unit))
    }
}
