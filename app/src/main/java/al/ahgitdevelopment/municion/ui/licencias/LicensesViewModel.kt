package al.ahgitdevelopment.municion.ui.licencias

import al.ahgitdevelopment.municion.SingleLiveEvent
import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.repository.Repository
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class LicensesViewModel @Inject constructor(
    val repository: Repository
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
}
