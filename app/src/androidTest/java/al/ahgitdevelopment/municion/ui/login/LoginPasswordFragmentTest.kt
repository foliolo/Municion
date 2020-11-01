package al.ahgitdevelopment.municion.ui.login

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.di.SharedPrefsModule
import al.ahgitdevelopment.municion.ext.hasTextInputLayoutErrorText
import al.ahgitdevelopment.municion.ext.hasTextInputLayoutHintText
import al.ahgitdevelopment.municion.launchFragmentInHiltContainer
import al.ahgitdevelopment.municion.repository.preferences.SharedPreferencesContract
import al.ahgitdevelopment.municion.utils.SimpleCountingIdlingResource
import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import javax.inject.Inject

@UninstallModules(SharedPrefsModule::class)
@LargeTest
@ExperimentalCoroutinesApi
@HiltAndroidTest
@RunWith(MockitoJUnitRunner::class)
class LoginPasswordFragmentTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    lateinit var context: Context

    @Inject
    lateinit var prefs: MockSharedPrefs

    @Inject
    lateinit var idlingResource: SimpleCountingIdlingResource

    @Before
    fun init() {
        hiltRule.inject()
        MockitoAnnotations.openMocks(this)

        // launchFragmentInHiltContainer<LoginPasswordFragment> {
        //     idlingResource = (this as LoginPasswordFragment).idlingResource
        //     // To prove that the test fails, omit this call:
        //     this@LoginPasswordFragmentTest.context = requireContext()
        //     IdlingRegistry.getInstance().register(idlingResource)
        // }
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(idlingResource)
    }

    @Test
    fun new_user_create_pin_password_correctly_then_button_appears() {
        // Arrange
        val password = "password"
        // prefs.setPassword(password)
        prefs.userType = MockSharedPrefs.USER_TYPE.NEW_USER
        prefs.passwordType = MockSharedPrefs.PASSWORD_TYPE.CORRECT

        launchFragmentInHiltContainer<LoginPasswordFragment> {
            idlingResource = (this as LoginPasswordFragment).idlingResource
            // To prove that the test fails, omit this call:
            this@LoginPasswordFragmentTest.context = requireContext()
            IdlingRegistry.getInstance().register(idlingResource)

        }

        // setStateToNewUser()

        // Assert
        onView(withId(R.id.login_button)).check(matches(not(isDisplayed())))

        onView(withId(R.id.login_password_1)).check(matches(isDisplayed()))
        onView(withHint(R.string.login_text_info)).perform(replaceText(password), closeSoftKeyboard())

        onView(withId(R.id.login_password_2)).check(matches(isDisplayed()))
        onView(withHint(R.string.lbl2_insert_password)).perform(replaceText(password), closeSoftKeyboard())

        onView(withId(R.id.login_password_1)).check(
            matches(
                hasTextInputLayoutHintText(
                    context.getString(R.string.login_text_info)
                )
            )
        )
        onView(withId(R.id.login_password_2)).check(
            matches(
                hasTextInputLayoutHintText(
                    context.getString(R.string.lbl2_insert_password)
                )
            )
        )

        onView(withId(R.id.login_button)).check(matches(isDisplayed()))
    }

    @Test
    fun new_user_create_pin_password_different_then_error_appear() {
        // Arrange
        val password1 = "password1"
        val password2 = "password2"

        // Assert
        onView(withHint(R.string.login_text_info)).perform(replaceText(password1), closeSoftKeyboard())
        onView(withHint(R.string.lbl2_insert_password)).perform(replaceText(password2), closeSoftKeyboard())

        onView(withId(R.id.login_password_2)).check(
            matches(
                hasTextInputLayoutHintText(
                    context.getString(R.string.lbl2_insert_password)
                )
            )
        )

        onView(withId(R.id.login_password_2)).check(
            matches(
                hasTextInputLayoutErrorText(
                    context.getString(R.string.login_not_matching_password_error)
                )
            )
        )
    }

    @Test
    fun existing_user_insert_wrong_pin_password_then_error_appear() {
        // Arrange
        val password1 = "password1"
        val password2 = "password2"

        // `when`(prefs.existUser()).then { true }

        // Assert
        onView(withHint(R.string.login_text_info)).perform(replaceText(password1), closeSoftKeyboard())
        onView(withHint(R.string.lbl2_insert_password)).perform(replaceText(password2), closeSoftKeyboard())

        onView(withId(R.id.login_password_1)).check(
            matches(
                hasTextInputLayoutHintText(
                    context.getString(R.string.login_text_info)
                )
            )
        )

        onView(withId(R.id.login_password_2)).check(
            matches(
                hasTextInputLayoutErrorText(
                    context.getString(R.string.login_not_matching_password_error)
                )
            )
        )
    }

    // Needs to be an inner class
    @Module
    @InstallIn(ApplicationComponent::class)
    class MockSharedPreferenceModule {

        // @Provides
        // fun provideMockSharedPrefs(@ApplicationContext appContext: Context): MockSharedPrefs =
        //     MockSharedPrefs(appContext)

        @Provides
        fun provideMockSharedPrefs(): MockSharedPrefs = MockSharedPrefs()
    }
}

// https://developer.android.com/studio/test#create_instrumented_test_for_a_build_variant
// https://developer.android.com/studio/test#use_separate_test_modules_for_instrumented_tests

// onView(withId(R.id.textInputLayout)).check
// (matches(hasTextInputLayoutErrorText(mRule.getActivity().getString(R.string
// .app_name))));

// class MockSharedPrefs @Inject constructor(context: Context) : SharedPreferencesManager(context) {
class MockSharedPrefs : SharedPreferencesContract {
    var userType = USER_TYPE.NEW_USER
    var passwordType = PASSWORD_TYPE.CORRECT

    override fun existUser(): Boolean {
        return userType == USER_TYPE.ACTIVE_USER
    }

    override fun getPassword(): String {
        TODO("Not yet implemented")
    }

    // override fun getPassword(): String {
    //     return super.getPassword()
    // }

    override fun setPassword(password: String) {
        when (passwordType) {
            PASSWORD_TYPE.CORRECT -> "password"
            PASSWORD_TYPE.SHORT -> "pass"
        }
    }

    override fun getShowTutorial(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setShowTutorial(show: Boolean) {
        TODO("Not yet implemented")
    }

    // override fun getShowTutorial(): Boolean {
    //     return super.getShowTutorial()
    // }

    // override fun setShowTutorial(show: Boolean) {
    //     super.setShowTutorial(show)
    // }

    enum class USER_TYPE { NEW_USER, ACTIVE_USER }
    enum class PASSWORD_TYPE { CORRECT, SHORT }
}
