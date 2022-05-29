package al.ahgitdevelopment.municion.ui.login

import al.ahgitdevelopment.municion.repository.preferences.SharedPreferencesManager
import al.ahgitdevelopment.municion.utils.Event
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    var prefs: SharedPreferencesManager,
) : ViewModel() {

    @VisibleForTesting(otherwise = PRIVATE)
    private val _navigateIntoApp = MutableLiveData<Event<Unit>>()
    val navigateIntoApp: LiveData<Event<Unit>>
        get() = _navigateIntoApp

    @VisibleForTesting(otherwise = PRIVATE)
    private val _navigateIntoTutorial = MutableLiveData<Event<Unit>>()
    val navigateIntoTutorial: LiveData<Event<Unit>>
        get() = _navigateIntoTutorial

    // val showAds = SingleLiveEvent<Boolean>()

    @VisibleForTesting(otherwise = PRIVATE)
    val _userState = MutableLiveData<UserState>()
    val userState: LiveData<UserState> = _userState

    private val _passwordState = MutableLiveData<PasswordState>()
    val passwordState: LiveData<PasswordState> = _passwordState

    private val _password1Error = MutableLiveData<ErrorMessages>()
    val password1Error: LiveData<ErrorMessages> = _password1Error

    private val _password2Error = MutableLiveData<ErrorMessages>()
    val password2Error: LiveData<ErrorMessages> = _password2Error

    private val _version = MutableLiveData<String>()
    val version: LiveData<String> = _version

    private var password1 = ""

    fun onCreateView() {
        // showAds.postValue(prefs.getBoolean(PREFS_SHOW_ADS, true))
        _userState.postValue(isUserLogged())
    }

    private fun isUserLogged(): UserState {
        return if (prefs.existUser()) {
            UserState.ACTIVE_USER
        } else {
            UserState.NEW_USER
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onPassword1TextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.isEmpty()) {
            _password1Error.postValue(ErrorMessages.NONE)
            _passwordState.postValue(PasswordState.INVALID)
        } else {
            if (_userState.value == UserState.NEW_USER) {
                _passwordState.postValue(PasswordState.INVALID)
            } else if (s.toString() != prefs.getPassword() && _userState.value == UserState.ACTIVE_USER) {
                _password1Error.postValue(ErrorMessages.NOT_MATCHING_PASSWORD)
                _passwordState.postValue(PasswordState.INVALID)
            } else {
                _password1Error.postValue(ErrorMessages.NONE)
                _passwordState.postValue(PasswordState.VALID)
            }
        }
        password1 = s.toString()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onPassword2TextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.toString() == password1) {
            _password2Error.postValue(ErrorMessages.NONE)
            _passwordState.postValue(PasswordState.VALID)
        } else {
            _password2Error.postValue(ErrorMessages.NOT_MATCHING_PASSWORD)
            _passwordState.postValue(PasswordState.INVALID)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonClick(view: View?) {
        if (password1.length < MIN_PASS_LENGTH) {
            _password1Error.postValue(ErrorMessages.SHORT_PASSWORD)
            _password2Error.postValue(ErrorMessages.SHORT_PASSWORD)
            _passwordState.postValue(PasswordState.INVALID)
        } else {
            _password1Error.postValue(ErrorMessages.NONE)
            _password2Error.postValue(ErrorMessages.NONE)

            storePassword()
            showTutorialOrApp()
        }
    }

    private fun storePassword() {
        prefs.setPassword(password1)
    }

    @VisibleForTesting(otherwise = PRIVATE)
    fun showTutorialOrApp() =
        prefs.getShowTutorial().let { showTutorial ->
            if (showTutorial) {
                prefs.setShowTutorial(!showTutorial)
                _navigateIntoTutorial.postValue(Event(Unit))
            } else {
                _navigateIntoApp.postValue(Event(Unit))
            }
        }

    enum class UserState { NEW_USER, ACTIVE_USER }
    enum class PasswordState { VALID, INVALID }
    enum class ErrorMessages { SHORT_PASSWORD, NOT_MATCHING_PASSWORD, NONE }

    companion object {
        private const val MIN_PASS_LENGTH = 6
    }
}
