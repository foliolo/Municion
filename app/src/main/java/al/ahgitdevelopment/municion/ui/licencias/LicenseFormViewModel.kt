package al.ahgitdevelopment.municion.ui.licencias

import al.ahgitdevelopment.municion.SingleLiveEvent
import al.ahgitdevelopment.municion.ui.licencias.types.LicenseType
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class LicenseFormViewModel @Inject constructor() : ViewModel() {
    val licenseType = SingleLiveEvent<LicenseType>()

    fun changeLicenseType(licenseType: LicenseType) {
        this.licenseType.value = licenseType
    }
}
