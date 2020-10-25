package al.ahgitdevelopment.municion.ui.licenses

import al.ahgitdevelopment.municion.FakeRepository
import al.ahgitdevelopment.municion.MainCoroutineRule
import al.ahgitdevelopment.municion.datamodel.License
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock

@ExperimentalCoroutinesApi
class LicensesViewModelTest {

    // Subject under test
    private lateinit var licensesViewModel: LicensesViewModel

    // Use a fake repository to be injected into the viewmodel
    // private lateinit var repository: FakeRepository
    @Mock
    lateinit var repository: FakeRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        repository = FakeRepository()
        licensesViewModel = LicensesViewModel(repository, SavedStateHandle())
    }

    @Test
    fun getLicenses() {
    }

    @Test
    fun fabClick() {
    }

    @Test
    fun deleteLicense() {
    }

    @Test
    fun `viewmodel add license into the repository`() = mainCoroutineRule.runBlockingTest {
        val fakeLicense = License(
            1,
            "License1",
            "12345",
            "10/10/2014",
            "15/15/2020",
            "98765"
        )

        // When license is save
        licensesViewModel.addLicense(fakeLicense)

        // Verify repository is called ones
        assertEquals(repository.licenses[0], fakeLicense)
    }
}
