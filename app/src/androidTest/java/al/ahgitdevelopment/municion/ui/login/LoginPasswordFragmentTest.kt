package al.ahgitdevelopment.municion.ui.login

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.ext.hasTextInputLayoutErrorText
import al.ahgitdevelopment.municion.ext.hasTextInputLayoutHintText
import al.ahgitdevelopment.municion.launchFragmentInHiltContainer
import al.ahgitdevelopment.municion.repository.preferences.SharedPreferencesManager
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
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import javax.inject.Inject

// @UninstallModules(SharedPrefsModule::class)
@LargeTest
@ExperimentalCoroutinesApi
@HiltAndroidTest
@RunWith(MockitoJUnitRunner::class)
class LoginPasswordFragmentTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var prefs: SharedPreferencesManager

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
        val password = "123456"
        // prefs.setPassword(password)
        // prefs.userType = MockSharedPrefs.USER_TYPE.NEW_USER
        // prefs.passwordType = MockSharedPrefs.PASSWORD_TYPE.CORRECT

        // val prefs = mock(SharedPreferencesManager::class.java)
        `when`(prefs.existUser()).then { LoginViewModel.UserState.NEW_USER }

        launchFragmentInHiltContainer<LoginPasswordFragment> {
            idlingResource = (this as LoginPasswordFragment).idlingResource
            IdlingRegistry.getInstance().register(idlingResource)

            // To prove that the test fails, omit this call:
            // this@LoginPasswordFragmentTest.context = requireContext()

            viewModel.prefs = prefs
        }

        // setStateToNewUser()

        // Assert
        onView(withId(R.id.login_button)).check(matches(not(isDisplayed())))

        onView(withId(R.id.login_password_1)).check(matches(isDisplayed()))
        onView(withHint(R.string.login_text_info)).perform(
            replaceText(password),
            closeSoftKeyboard()
        )

        onView(withId(R.id.login_password_2)).check(matches(isDisplayed()))
        onView(withHint(R.string.lbl2_insert_password)).perform(
            replaceText(password),
            closeSoftKeyboard()
        )

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
        val password1 = "123456"
        val password2 = "123123"

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
        val password1 = "123456"
        val password2 = "123123"

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

    // // Needs to be an inner class
    // @Module
    // @InstallIn(SingletonComponent::class)
    // class MockSharedPreferenceModule {
    //
    //     // @Provides
    //     // fun provideMockSharedPrefs(@ApplicationContext appContext: Context): MockSharedPrefs =
    //     //     MockSharedPrefs(appContext)
    //
    //     @Provides
    //     fun provideMockSharedPrefs(): MockSharedPrefs = MockSharedPrefs()
    // }
}

// https://developer.android.com/studio/test#create_instrumented_test_for_a_build_variant
// https://developer.android.com/studio/test#use_separate_test_modules_for_instrumented_tests

// onView(withId(R.id.textInputLayout)).check
// (matches(hasTextInputLayoutErrorText(mRule.getActivity().getString(R.string
// .app_name))));

// class MockSharedPrefs @Inject constructor(context: Context) : SharedPreferencesManager(context) {
// class MockSharedPrefs : SharedPreferencesContract {
//     var userType = USER_TYPE.NEW_USER
//     var passwordType = PASSWORD_TYPE.CORRECT
//
//     override fun existUser(): Boolean {
//         return userType == USER_TYPE.ACTIVE_USER
//     }
//
//     override fun getPassword(): String =
//         when (passwordType) {
//             PASSWORD_TYPE.CORRECT -> CORRECT_PASSWORD
//             PASSWORD_TYPE.WRONG -> WRONG_PASSWORD
//             PASSWORD_TYPE.SHORT -> SHORT_PASSWORD
//         }
//
//     override fun setPassword(password: String) {
//         TODO("Not yet implemented")
//     }
//
//     override fun getShowTutorial(): Boolean {
//         TODO("Not yet implemented")
//     }
//
//     override fun setShowTutorial(show: Boolean) {
//         TODO("Not yet implemented")
//     }
//
//     enum class USER_TYPE { NEW_USER, ACTIVE_USER }
//     enum class PASSWORD_TYPE { CORRECT, WRONG, SHORT }
//
//     companion object {
//
//         const val CORRECT_PASSWORD = "123456"
//         const val WRONG_PASSWORD = "123123"
//         const val SHORT_PASSWORD = "1234"
//     }
// }
