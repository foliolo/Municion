package al.ahgitdevelopment.municion.ui.properties

import al.ahgitdevelopment.municion.FakeRepository.Companion.FAKE_PROPERTIES
import al.ahgitdevelopment.municion.FakeRepository.Companion.FAKE_PROPERTY
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

class PropertiesViewModelTest {
    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

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
    fun init_getProperties_success() {
        // GIVEN
        every { repository.getProperties() }.returns(FAKE_PROPERTIES.toFlow())
        SUT = PropertiesViewModel(repository, ioDispatcher, savedStateHandle)
        // ACT
        val result = SUT.properties.getOrAwaitValue()
        // VERIFY
        assertEquals(FAKE_PROPERTIES, result)
    }

    @Test
    fun fabClick_navigateToForm_success() {
        // GIVEN
        SUT = PropertiesViewModel(repository, ioDispatcher, savedStateHandle)
        // ACT
        SUT.fabClick(null)
        // VERIFY
        assertEquals(null, SUT.navigateToForm.getOrAwaitValue())
    }

    @Test
    fun deleteProperty_validatePropertyIdPassedToTheRepository_success() {
        // GIVEN
        SUT = PropertiesViewModel(repository, ioDispatcher, savedStateHandle)
        // ACT
        SUT.deleteProperty(FAKE_PROPERTY.id)
        // VERIFY
        coVerify {
            repository.removeProperty(
                withArg { propertyId ->
                    assertEquals(FAKE_PROPERTY.id, propertyId)
                }
            )
        }
    }

    @Test
    fun addProperty_validatePropertyPassedToRepository() {
        // GIVEN
        SUT = PropertiesViewModel(repository, ioDispatcher, savedStateHandle)
        // ACT
        SUT.addProperty(FAKE_PROPERTY)
        // VERIFY
        coVerify {
            repository.saveProperty(
                withArg { property ->
                    assertEquals(FAKE_PROPERTY, property)
                }
            )
        }
    }

    companion object {
        private lateinit var SUT: PropertiesViewModel
    }
}
