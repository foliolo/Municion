package al.ahgitdevelopment.municion.ui.login

import al.ahgitdevelopment.municion.ext.getOrAwaitValue
import al.ahgitdevelopment.municion.ui.login.LoginViewModel.UserState.ACTIVE_USER
import al.ahgitdevelopment.municion.ui.login.LoginViewModel.UserState.NEW_USER
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LoginViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // System under test
    private lateinit var loginViewModel: LoginViewModel

    @Mock(lenient = true)
    lateinit var prefs: SharedPreferences

    @Mock(lenient = true)
    lateinit var editor: SharedPreferences.Editor

    @Mock
    lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setUp() {
        // MockitoAnnotations.initMocks(this)
        loginViewModel = LoginViewModel(prefs, savedStateHandle)
    }

    @Test
    fun `onCreateView login with user logged`() {
        // Arrange
        `when`(prefs.contains(anyString())).then { true }
        `when`(prefs.getString(anyString(), anyString())).then { "user" }
        // Act
        loginViewModel.onCreateView()
        // Assert
        assertEquals(ACTIVE_USER, loginViewModel.userState.getOrAwaitValue())
    }

    @Test
    fun `onCreateView login with new user`() {
        // Arrange
        `when`(prefs.contains(anyString())).then { false }
        `when`(prefs.getString(anyString(), anyString())).then { "user" }
        // Act
        loginViewModel.onCreateView()
        // Assert
        assertEquals(NEW_USER, loginViewModel.userState.getOrAwaitValue())
    }

    @Test
    fun `onCreateView login with user empty in shared prefs`() {
        // Arrange
        `when`(prefs.contains(anyString())).then { false }
        `when`(prefs.getString(anyString(), anyString())).then { anyString() }
        // Act
        loginViewModel.onCreateView()
        // Assert
        assertEquals(NEW_USER, loginViewModel.userState.getOrAwaitValue())
    }

    @Test
    fun `onPassword1TextChanged() empty password`() {
        // Arrange
        `when`(prefs.contains(anyString())).then { true }
        val password = ""
        // Act
        loginViewModel.onPassword1TextChanged(password, 0, 0, 0)
        // Assert
        assertEquals(LoginViewModel.ErrorMessages.NONE, loginViewModel.password1Error.getOrAwaitValue())
        assertEquals(LoginViewModel.PasswordState.INVALID, loginViewModel.passwordState.getOrAwaitValue())
    }

    @Test
    fun `onPassword1TextChanged() wrong password store and new user`() {
        // Arrange
        loginViewModel._userState.postValue(NEW_USER)
        val password = "pass"
        // Act
        loginViewModel.onPassword1TextChanged(password, 0, 0, 0)
        // Assert
        assertEquals(LoginViewModel.PasswordState.INVALID, loginViewModel.passwordState.getOrAwaitValue())
    }

    @Test
    fun `onPassword1TextChanged() wrong password and active user`() {
        // Arrange
        loginViewModel._userState.postValue(ACTIVE_USER)
        `when`(prefs.getString(anyString(), anyString())).then { "pass" }
        val password = "password"
        // Act
        loginViewModel.onPassword1TextChanged(password, 0, 0, 0)
        // Assert
        assertEquals(
            LoginViewModel.ErrorMessages.NOT_MATCHING_PASSWORD,
            loginViewModel.password1Error.getOrAwaitValue()
        )
        assertEquals(LoginViewModel.PasswordState.INVALID, loginViewModel.passwordState.getOrAwaitValue())
    }

    @Test
    fun `onPassword1TextChanged() correct password and active user`() {
        // Arrange
        loginViewModel._userState.postValue(ACTIVE_USER)
        `when`(prefs.getString(anyString(), anyString())).then { "password" }
        val password = "password"
        // Act
        loginViewModel.onPassword1TextChanged(password, 0, 0, 0)
        // Assert
        assertEquals(LoginViewModel.ErrorMessages.NONE, loginViewModel.password1Error.getOrAwaitValue())
        assertEquals(LoginViewModel.PasswordState.VALID, loginViewModel.passwordState.getOrAwaitValue())
    }

    @Test
    fun `onPassword2TextChanged() password1 == password2`() {
        // Arrange
        val password = "password"
        loginViewModel.onPassword1TextChanged(password, 0, 0, 0)
        // Act
        loginViewModel.onPassword2TextChanged(password, 0, 0, 0)
        // Assert
        assertEquals(LoginViewModel.ErrorMessages.NONE, loginViewModel.password2Error.getOrAwaitValue())
        assertEquals(LoginViewModel.PasswordState.VALID, loginViewModel.passwordState.getOrAwaitValue())
    }

    @Test
    fun `onPassword2TextChanged() password1 != password2`() {
        // Arrange
        val password1 = "password1"
        val password2 = "password2"
        loginViewModel.onPassword1TextChanged(password1, 0, 0, 0)
        // Act
        loginViewModel.onPassword2TextChanged(password2, 0, 0, 0)
        // Assert
        assertEquals(
            LoginViewModel.ErrorMessages.NOT_MATCHING_PASSWORD,
            loginViewModel.password2Error.getOrAwaitValue()
        )
        assertEquals(LoginViewModel.PasswordState.INVALID, loginViewModel.passwordState.getOrAwaitValue())
    }

    @Test
    fun `onButtonClick() password1 too short`() {
        // Arrange
        val password1 = "pass"
        loginViewModel.onPassword1TextChanged(password1, 0, 0, 0)
        // Act
        loginViewModel.onButtonClick(null)
        // Assert
        assertEquals(
            LoginViewModel.ErrorMessages.SHORT_PASSWORD,
            loginViewModel.password1Error.getOrAwaitValue()
        )
        assertEquals(
            LoginViewModel.ErrorMessages.SHORT_PASSWORD,
            loginViewModel.password2Error.getOrAwaitValue()
        )
        assertEquals(LoginViewModel.PasswordState.INVALID, loginViewModel.passwordState.getOrAwaitValue())
    }

    @Test
    fun `onButtonClick() password1 long enough`() {
        // Arrange
        val password1 = "password"
        `when`(editor.putString(anyString(), anyString())).then { editor }
        `when`(prefs.edit()).thenReturn(editor)
        loginViewModel.onPassword1TextChanged(password1, 0, 0, 0)
        // Act
        loginViewModel.onButtonClick(null)
        // Assert
        assertEquals(LoginViewModel.ErrorMessages.NONE, loginViewModel.password1Error.getOrAwaitValue())
        assertEquals(LoginViewModel.ErrorMessages.NONE, loginViewModel.password2Error.getOrAwaitValue())
    }

    @Test
    fun `storePassword() when clicking button and passwords are correct`() {
        // Arrange
        val password = "password"
        `when`(prefs.getString(anyString(), anyString())).then { password }
        `when`(prefs.edit()).thenReturn(editor)
        loginViewModel.onPassword1TextChanged(password, 0, 0, 0)
        // Act
        loginViewModel.onButtonClick(null)
        // Assert
        assertEquals(password, prefs.getString(anyString(), anyString()))
    }

    @Test
    fun `showTutorialOrApp() if opening the first time then show tutorial and update tutorial flag`() {
        // Arrange
        loginViewModel.navigateIntoTutorial.observeForever { }
        `when`(prefs.getBoolean(anyString(), anyBoolean())).then { true }
        `when`(editor.putBoolean(anyString(), anyBoolean())).then { editor }
        `when`(prefs.edit()).thenReturn(editor)
        loginViewModel.navigateIntoTutorial.observeForever { }
        // Act
        loginViewModel.showTutorialOrApp()
        // Assert
        assertEquals(Unit, loginViewModel.navigateIntoTutorial.getOrAwaitValue().getContentIfNotHandled())
    }

    @Test
    fun `showTutorialOrApp() if app already launched then don't show tutorial and launch app`() {
        // Arrange
        loginViewModel.navigateIntoTutorial.observeForever { }
        `when`(prefs.getBoolean(anyString(), anyBoolean())).then { false }
        `when`(editor.putBoolean(anyString(), anyBoolean())).then { editor }
        `when`(prefs.edit()).thenReturn(editor)
        loginViewModel.navigateIntoTutorial.observeForever { }
        // Act
        loginViewModel.showTutorialOrApp()
        // Assert
        assertEquals(Unit, loginViewModel.navigateIntoApp.getOrAwaitValue().getContentIfNotHandled())
    }
}
