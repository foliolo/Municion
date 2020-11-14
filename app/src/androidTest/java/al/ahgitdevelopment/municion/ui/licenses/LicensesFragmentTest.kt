package al.ahgitdevelopment.municion.ui.licenses

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.launchFragmentInHiltContainer
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import javax.inject.Inject

@MediumTest
@HiltAndroidTest
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class LicensesFragmentTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var instantExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var context: Context

    lateinit var navController: TestNavHostController

    @Before
    fun setUp() {
        hiltRule.inject()
        navController = TestNavHostController(context)
    }

    @Test
    fun clickAddNewLicense_navigateToLicenseFormFragment() = runBlocking {
        // `when`(navController.navigate(anyInt())).then { R.id.licenseFormFragment }

        navController.setGraph(R.navigation.nav_graph)
        launchFragmentInHiltContainer<LicensesFragment> {
            Navigation.setViewNavController(requireView(), navController)
        }

        onView(withId(R.id.licenses_fab_add)).perform(click())

        verify(navController).navigate(
            LicensesFragmentDirections.actionLicensesFragmentToLicenseFormFragment()
        )
    }
}
