package al.ahgitdevelopment.municion.ui.licenses

import al.ahgitdevelopment.municion.FakeRepository
import al.ahgitdevelopment.municion.FakeRepository.Companion.FAKE_LICENSE
import al.ahgitdevelopment.municion.FakeRepository.Companion.FAKE_LICENSES
import al.ahgitdevelopment.municion.ext.getOrAwaitValue
import al.ahgitdevelopment.municion.utils.SimpleCountingIdlingResource
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import androidx.test.espresso.IdlingRegistry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class LicensesViewModelTest {

    // System under test
    private lateinit var licensesViewModel: LicensesViewModel

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    // @get:Rule
    // var mainCoroutineRule = MainCoroutineRule()

    @Mock
    lateinit var idlingResource: SimpleCountingIdlingResource

    @Mock
    lateinit var savedStateHandle: SavedStateHandle

    private lateinit var repository: FakeRepository

    var ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(idlingResource)
        repository = FakeRepository()
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(idlingResource)
    }

    @Test
    fun `get licenses retrieves data`() {
        repository.shouldReturnError = false
        licensesViewModel = LicensesViewModel(repository, ioDispatcher, savedStateHandle)
        assertEquals(FAKE_LICENSES, licensesViewModel.licenses.getOrAwaitValue())
    }

    @Test
    fun `get licenses and retrieves data throw exception`() {
        // mainCoroutineRule.pauseDispatcher()
        repository.shouldReturnError = true
        repository.retrieveLocalData = false
        licensesViewModel = LicensesViewModel(repository, ioDispatcher, savedStateHandle)

        // licensesViewModel.error.observeForTesting{
        // mainCoroutineRule.resumeDispatcher()
        // assertEquals(FakeRepository.ERROR_MESSAGE, licensesViewModel.error.getOrAwaitValue() )
        // }
        assertEquals(FakeRepository.ERROR_MESSAGE, licensesViewModel.error.getOrAwaitValue())
    }

    @Test
    fun `get licenses and retrieves data from remote database`() {
        repository.shouldReturnError = false
        repository.retrieveLocalData = false
        licensesViewModel = LicensesViewModel(repository, ioDispatcher, savedStateHandle)

        assertEquals(FAKE_LICENSES, licensesViewModel.licenses.getOrAwaitValue())
    }

    @Test
    fun `get licenses and exception occur retrieving data from remote database`() {
        // mainCoroutineRule.pauseDispatcher()
        repository.shouldReturnError = true
        repository.retrieveLocalData = false
        licensesViewModel = LicensesViewModel(repository, ioDispatcher, savedStateHandle)

        licensesViewModel.error.observeForever {
            // mainCoroutineRule.resumeDispatcher()
            assertEquals(FakeRepository.ERROR_MESSAGE + "aaaa", it)
        }

        // assertEquals(FakeRepository.ERROR_MESSAGE, licensesViewModel.error.getOrAwaitValue() )
    }

    // @Test
    // fun loadAllTasksFromRepository_loadingTogglesAndDataLoaded() {
    //     // Pause dispatcher so we can verify initial values
    //     mainCoroutineRule.pauseDispatcher()
    //
    //     // Given an initialized TasksViewModel with initialized tasks
    //     // When loading of Tasks is requested
    //     tasksViewModel.setFiltering(TasksFilterType.ALL_TASKS)
    //
    //     // Trigger loading of tasks
    //     tasksViewModel.loadTasks(true)
    //     // Observe the items to keep LiveData emitting
    //     tasksViewModel.items.observeForTesting {
    //
    //         // Then progress indicator is shown
    //         assertThat(tasksViewModel.dataLoading.getOrAwaitValue()).isTrue()
    //
    //         // Execute pending coroutines actions
    //         mainCoroutineRule.resumeDispatcher()
    //
    //         // Then progress indicator is hidden
    //         assertThat(tasksViewModel.dataLoading.getOrAwaitValue()).isFalse()
    //
    //         // And data correctly loaded
    //         assertThat(tasksViewModel.items.getOrAwaitValue()).hasSize(3)
    //     }
    // }

    @Test
    fun fabClick() {
    }

    @Test
    fun deleteLicense() {
    }

    @Test
    fun `viewmodel add license into the repository`() {

        // When license is save
        licensesViewModel.addLicense(FAKE_LICENSE)

        // Verify repository is called ones
        assertEquals(FAKE_LICENSES, licensesViewModel.licenses.getOrAwaitValue())
    }
}