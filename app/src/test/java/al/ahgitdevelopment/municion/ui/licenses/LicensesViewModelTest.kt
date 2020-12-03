package al.ahgitdevelopment.municion.ui.licenses

import al.ahgitdevelopment.municion.FakeRepository.Companion.FAKE_LICENSE
import al.ahgitdevelopment.municion.FakeRepository.Companion.FAKE_LICENSES
import al.ahgitdevelopment.municion.MainCoroutineRule
import al.ahgitdevelopment.municion.ext.getOrAwaitValue
import al.ahgitdevelopment.municion.repository.RepositoryContract
import al.ahgitdevelopment.municion.utils.toFlow
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

// @ExperimentalCoroutinesApi
// @RunWith(MockitoJUnitRunner::class)
class LicensesViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // @Mock
    // lateinit var idlingResource: SimpleCountingIdlingResource

    @MockK
    lateinit var savedStateHandle: SavedStateHandle

    @MockK
    lateinit var repository: RepositoryContract

    var ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    @Before
    fun setUp() {
        // IdlingRegistry.getInstance().register(idlingResource)
        MockKAnnotations.init(this, relaxed = true)
    }

    // @After
    // fun unregisterIdlingResource() {
    //     IdlingRegistry.getInstance().unregister(idlingResource)
    // }

    @Test
    fun `get licenses retrieves data`() = runBlocking {
        // GIVEN
        every { repository.getLicenses(false) }.returns(FAKE_LICENSES.toFlow())
        SUT = LicensesViewModel(repository, ioDispatcher, savedStateHandle)
        // ACT
        val result = SUT.licenses.getOrAwaitValue(500, TimeUnit.MILLISECONDS)
        // VERIFY
        assertEquals(FAKE_LICENSES, result)
    }

    @Test
    fun fabClick_clickButton_navigateToForm() {
        // GIVEN
        SUT = LicensesViewModel(repository, ioDispatcher, savedStateHandle)
        // ACT
        SUT.fabClick(null)
        // VERIFY
        assertEquals(null, SUT.navigateToForm.getOrAwaitValue())
    }

    @Test
    fun deleteLicense_passingTheLicenseId_theIdIsPassedToTheRepository() {
        // GIVEN
        SUT = LicensesViewModel(repository, ioDispatcher, savedStateHandle)
        // ACT
        SUT.deleteLicense(FAKE_LICENSE.id)
        // VERIFY
        coVerify {
            repository.removeLicense(
                withArg { id ->
                    assertEquals(FAKE_LICENSE.id, id)
                }
            )
        }
    }

    @Test
    fun `viewmodel add license into the repository`() {
        // GIVEN
        SUT = LicensesViewModel(repository, ioDispatcher, savedStateHandle)
        // ACT
        SUT.addLicense(FAKE_LICENSE)
        // VERIFY
        coVerify {
            repository.saveLicense(
                withArg { license ->
                    assertEquals(FAKE_LICENSE, license)
                }
            )
        }
    }

    companion object {
        private lateinit var SUT: LicensesViewModel
    }
}
