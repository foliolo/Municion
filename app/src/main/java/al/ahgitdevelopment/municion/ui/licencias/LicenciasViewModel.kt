package al.ahgitdevelopment.municion.ui.licencias

import al.ahgitdevelopment.municion.SingleLiveEvent
import android.view.View
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class LicenciasViewModel @Inject constructor() : ViewModel() {

    val addLicense = SingleLiveEvent<Unit>()

    fun onCreatedView() {
    }

    fun fabClick(aux: View) {
        addLicense.call()
    }
}
