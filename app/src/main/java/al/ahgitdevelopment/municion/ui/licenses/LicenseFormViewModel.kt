package al.ahgitdevelopment.municion.ui.licenses

import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.di.IoDispatcher
import al.ahgitdevelopment.municion.repository.RepositoryContract
import al.ahgitdevelopment.municion.utils.Event
import al.ahgitdevelopment.municion.utils.SingleLiveEvent
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("UNUSED_PARAMETER")
@HiltViewModel
class LicenseFormViewModel @Inject constructor(
    private val repository: RepositoryContract,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _issueDate = SingleLiveEvent<Unit>()
    val issueDate: LiveData<Unit> = _issueDate

    private val _expiryDate = SingleLiveEvent<Unit>()
    val expiryDate: LiveData<Unit> = _expiryDate

    private val _fabSaveLicenseClicked = SingleLiveEvent<Unit>()
    val fabSaveLicenseClicked: LiveData<Unit> = _fabSaveLicenseClicked

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    private val _closeForm = MutableLiveData<Event<Unit>>()
    val closeForm: LiveData<Event<Unit>> = _closeForm

    fun selectIssueDate(view: View) {
        _issueDate.call()
    }

    fun selectExpiryDate(view: View) {
        _expiryDate.call()
    }

    fun fabSaveLicenseClicked(view: View?) {
        _fabSaveLicenseClicked.call()
    }

    fun saveLicense(license: License) = viewModelScope.launch(ioDispatcher) {
        // TODO: perform field validations
        // wrapEspressoIdlingResource {
        repository.saveLicense(license)
        _closeForm.postValue(Event(Unit))
        // }
    }
}
