package al.ahgitdevelopment.municion.ui.licenses

import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.di.IoDispatcher
import al.ahgitdevelopment.municion.repository.RepositoryContract
import al.ahgitdevelopment.municion.repository.preferences.SharedPreferencesManager
import al.ahgitdevelopment.municion.ui.BaseViewModel
import al.ahgitdevelopment.municion.utils.Event
import al.ahgitdevelopment.municion.utils.SingleLiveEvent
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
    private val prefs: SharedPreferencesManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    var licenses = repository.getLicenses()
        .catch { error.postValue(it.message) }
        .asLiveData()

    val navigateToForm = SingleLiveEvent<Unit>()
    val error = SingleLiveEvent<String>()

    init {
        showProgressBar()
        loadRewardedAd()
    }

    fun fabClick(view: View?) {
        if (licenses.value!!.checkMaxFreeItems()) {
            navigateToForm.call()
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

    override fun loadRewardedAd() {
        _loadRewardedAd.postValue(Event(Unit))
    }

    override fun showRewardedAdDialog() {
        _showRewardedAdDialog.postValue(Event(Unit))
    }

    override fun showRewardedAd() {
        _showRewardedAd.postValue(Event(Unit))
    }

    override fun rewardObtain() {
        navigateToForm.call()
    }

    override fun rewardCancel() {
        error.value = "Watch the full video to add a new item"
    }
}
