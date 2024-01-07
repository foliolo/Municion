package al.ahgitdevelopment.municion

import al.ahgitdevelopment.municion.utils.SingleLiveEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class NavigationActivityViewModel : ViewModel() {

    private val _paymentSupportDeveloper = SingleLiveEvent<Unit>()
    val paymentSupportDeveloper: LiveData<Unit> = _paymentSupportDeveloper

    fun paymentSupportDeveloper() {
        _paymentSupportDeveloper.call()
    }
}
