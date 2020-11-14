package al.ahgitdevelopment.municion.ui.licenses

import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.di.IoDispatcher
import al.ahgitdevelopment.municion.repository.RepositoryContract
import al.ahgitdevelopment.municion.utils.Event
import al.ahgitdevelopment.municion.utils.SingleLiveEvent
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch

@Suppress("UNUSED_PARAMETER")
class LicenseFormViewModel @ViewModelInject constructor(
    private val repository: RepositoryContract,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val issueDate = SingleLiveEvent<Unit>()
    val expiryDate = SingleLiveEvent<Unit>()
    val fabSaveLicenseClicked = SingleLiveEvent<Unit>()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    private val _closeForm = MutableLiveData<Event<Unit>>()
    val closeForm: LiveData<Event<Unit>>
        get() = _closeForm

    fun selectIssueDate(view: View) {
        issueDate.call()
    }

    fun selectExpiryDate(view: View) {
        expiryDate.call()
    }

    fun fabSaveLicenseClicked(view: View) {
        fabSaveLicenseClicked.call()
    }

    fun saveLicense(license: License) = viewModelScope.launch(ioDispatcher) {
        // TODO: perform field validations
        // wrapEspressoIdlingResource {
        repository.saveLicense(license)
        _closeForm.postValue(Event(Unit))
        // }
    }
}
