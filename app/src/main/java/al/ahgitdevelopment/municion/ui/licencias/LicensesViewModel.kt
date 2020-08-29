package al.ahgitdevelopment.municion.ui.licencias

import al.ahgitdevelopment.municion.SingleLiveEvent
import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.di.FirebaseModule.Companion.EVENT_LOGOUT
import al.ahgitdevelopment.municion.repository.Repository
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch
import javax.inject.Inject

class LicensesViewModel @Inject constructor(
    private val repository: Repository,
    private val firebaseAnalytics: FirebaseAnalytics,
    private val firebaseCrashlytics: FirebaseCrashlytics
) : ViewModel() {

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

    fun recordLogoutEvent() {
        firebaseAnalytics.logEvent(EVENT_LOGOUT, null)
    }

    fun clearUserData() {
        firebaseAnalytics.setUserId(null)
        firebaseCrashlytics.setUserId("")
    }
}
