package al.ahgitdevelopment.municion

import al.ahgitdevelopment.municion.utils.Event
import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NavigationActivityViewModel : ViewModel() {

    private val _snackBar = MutableLiveData<Event<Unit>>()
    val snackBar: LiveData<Event<Unit>> = _snackBar

    init {

        val handler = Handler()
        val runnableCode: Runnable = object : Runnable {
            override fun run() {
                _snackBar.postValue(Event(Unit))
                handler.postDelayed(this, 3 * 60 * 1000)
            }
        }
        handler.post(runnableCode)
    }
}
