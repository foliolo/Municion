package al.ahgitdevelopment.municion.ui.competitions

import al.ahgitdevelopment.municion.FakeRepository.Companion.FAKE_COMPETITION
import al.ahgitdevelopment.municion.FakeRepository.Companion.FAKE_COMPETITIONS
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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// @ExperimentalCoroutinesApi
// @RunWith(MockitoJUnitRunner::class)
class CompetitionsViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    // @get:Rule
    // var mainCoroutineRule = MainCoroutineRule()

    @MockK
    lateinit var savedStateHandle: SavedStateHandle

    @MockK
    lateinit var repository: RepositoryContract

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun init_getCompetitions_success() {
        // GIVEN
        every { repository.getCompetitions() }.returns(FAKE_COMPETITIONS.toFlow())
        SUT = CompetitionsViewModel(repository, ioDispatcher, savedStateHandle)
        // ACT
        val result = SUT.competitions.getOrAwaitValue()
        // VERIFY
        assertEquals(FAKE_COMPETITIONS, result)
    }

    // @Test
    // fun `init_getCompetitions_throwsException`() {
    //     // GIVEN
    //     coEvery { repository.getCompetitions() }.throws(Exception("Test exception"))
    //
    //     SUT = CompetitionsViewModel(repository, ioDispatcher, savedStateHandle)
    //
    //     // ACT
    //     val result = SUT.competitions.getOrAwaitValue()
    //
    //     // VERIFY
    //     verify(atLeast = 1) { SUT.error }
    // }

    @Test
    fun addCompetition_competitionArgumentPassedToRepository_Success() {
        // GIVEN
        SUT = CompetitionsViewModel(repository, ioDispatcher, savedStateHandle)

        // ACT
        SUT.addCompetition(FAKE_COMPETITION)

        // VERIFY
        coVerify {
            repository.saveCompetition(
                withArg {
                    assertEquals(FAKE_COMPETITION, it)
                }
            )
        }
    }

    @Test
    fun deleteCompetition_competitionIdPassedToRepository_success() {
        // GIVEN
        SUT = CompetitionsViewModel(repository, ioDispatcher, savedStateHandle)
        // ACT
        SUT.deleteCompetition(FAKE_COMPETITION.id)
        // VERIFY
        coVerify {
            repository.removeCompetition(
                withArg {
                    assertEquals(FAKE_COMPETITION.id, it)
                }
            )
        }
    }

    @Test
    fun fabClick_clickButton_navigateToForm() {
        // GIVEN
        SUT = CompetitionsViewModel(repository, ioDispatcher, savedStateHandle)
        // ACT
        SUT.fabClick(null)
        // VERIFY
        assertEquals(null, SUT.navigateToForm.getOrAwaitValue())
    }

    companion object {
        private lateinit var SUT: CompetitionsViewModel
        private const val IDLE_RESOURCE_NAME = "TEST"
    }
}
