package al.ahgitdevelopment.municion

import al.ahgitdevelopment.municion.utils.Event
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NavigationActivityViewModel : ViewModel() {

    private val _showAdDialog = MutableLiveData<Event<Unit>>()
    val showAdDialog: LiveData<Event<Unit>> = _showAdDialog

    private val _paymentSupportDeveloper = MutableLiveData<Event<Unit>>()
    val paymentSupportDeveloper: LiveData<Event<Unit>> = _paymentSupportDeveloper

    fun paymentSupportDeveloper() {
        _paymentSupportDeveloper.postValue(Event(Unit))
    }
}
