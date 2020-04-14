package al.ahgitdevelopment.municion.login

import al.ahgitdevelopment.municion.SingleLiveEvent
import al.ahgitdevelopment.municion.di.SharedPrefsModule.Companion.PREFS_PASSWORD
import al.ahgitdevelopment.municion.di.SharedPrefsModule.Companion.PREFS_SHOW_ADS
import android.content.SharedPreferences
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class LoginViewModel @Inject constructor(private val prefs: SharedPreferences) : ViewModel() {

    val navigateIntoApp = SingleLiveEvent<Void>()

    val showAds = SingleLiveEvent<Boolean>()

    private val _userState = MutableLiveData<UserState>()
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
        showAds.postValue(prefs.getBoolean(PREFS_SHOW_ADS, true))
        _userState.postValue(isUserLogged())
    }

    private fun isUserLogged(): UserState {
        return if (prefs.contains(PREFS_PASSWORD) && prefs.getString(PREFS_PASSWORD, "")!!.isNotBlank()) {
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

            if (s.toString() != prefs.getString(PREFS_PASSWORD, "") &&
                    _userState.value == UserState.ACTIVE_USER) {
                _password1Error.postValue(ErrorMessages.NOT_MATCHING_PASSWORD)
                _passwordState.postValue(PasswordState.INVALID)
            } else if (_userState.value == UserState.NEW_USER) {
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
    fun onButtonClick(view: View) {
        if (password1.length < MIN_PASS_LENGTH) {
            _password1Error.postValue(ErrorMessages.SHORT_PASSWORD)
            _password2Error.postValue(ErrorMessages.SHORT_PASSWORD)
            _passwordState.postValue(PasswordState.INVALID)

        } else {
            _password1Error.postValue(ErrorMessages.NONE)
            _password2Error.postValue(ErrorMessages.NONE)

            storePassword()
            navigateIntoApp.call()
        }
    }

    private fun storePassword() {
        prefs.edit().apply {
            putString(PREFS_PASSWORD, password1)
        }.apply()
    }

    enum class UserState { NEW_USER, ACTIVE_USER }
    enum class PasswordState { VALID, INVALID }
    enum class ErrorMessages { SHORT_PASSWORD, NOT_MATCHING_PASSWORD, NONE }

    companion object {
        private const val MIN_PASS_LENGTH = 6
    }
}