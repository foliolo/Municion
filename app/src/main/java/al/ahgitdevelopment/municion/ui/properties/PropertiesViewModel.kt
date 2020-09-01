package al.ahgitdevelopment.municion.ui.properties

import al.ahgitdevelopment.municion.SingleLiveEvent
import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.repository.Repository
import al.ahgitdevelopment.municion.ui.BaseViewModel
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("UNUSED_PARAMETER")
class PropertiesViewModel @Inject constructor(
    private val repository: Repository,
    firebaseAnalytics: FirebaseAnalytics,
    firebaseCrashlytics: FirebaseCrashlytics
) : BaseViewModel(firebaseAnalytics, firebaseCrashlytics) {

    lateinit var properties: LiveData<List<Property>>

    val addProperty = SingleLiveEvent<Unit>()

    init {
        getProperties()
    }

    fun getProperties() {
        viewModelScope.launch {
            properties = repository.getProperties()!!
        }
    }

    fun fabClick(view: View) {
        addProperty.call()
    }

    fun deleteProperty(propertyId: Long) {
        viewModelScope.launch {
            repository.removeProperty(propertyId)
        }
    }

    fun addProperty(property: Property) {
        viewModelScope.launch {
            repository.saveProperty(property)
        }
    }
}
