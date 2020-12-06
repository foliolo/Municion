package al.ahgitdevelopment.municion.ui.purchases

import al.ahgitdevelopment.municion.FakeRepository.Companion.FAKE_PURCHASE
import al.ahgitdevelopment.municion.FakeRepository.Companion.FAKE_PURCHASES
import al.ahgitdevelopment.municion.ext.getOrAwaitValue
import al.ahgitdevelopment.municion.repository.RepositoryContract
import al.ahgitdevelopment.municion.repository.firebase.RemoteStorageDataSourceContract
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

class PurchasesViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @MockK
    lateinit var savedStateHandle: SavedStateHandle

    @MockK
    lateinit var repository: RepositoryContract

    @MockK
    lateinit var storageRepository: RemoteStorageDataSourceContract

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun init_getPurchases_success() {
        // GIVEN
        every { repository.getPurchases() }.returns(FAKE_PURCHASES.toFlow())
        SUT = PurchasesViewModel(repository, storageRepository, ioDispatcher, savedStateHandle)
        // ACT
        val result = SUT.purchases.getOrAwaitValue()
        // VERIFY
        assertEquals(FAKE_PURCHASES, result)
    }

    @Test
    fun addPurchase_validatePurchasePassedToRepository() {
        // GIVEN
        SUT = PurchasesViewModel(repository, storageRepository, ioDispatcher, savedStateHandle)
        // ACT
        SUT.addPurchase(FAKE_PURCHASE)
        // VERIFY
        coVerify {
            repository.savePurchase(
                withArg { purchase ->
                    assertEquals(FAKE_PURCHASE, purchase)
                }
            )
        }
    }

    // @Test
    // fun getError() {
    //     // GIVEN
    //
    //     // ACT
    //
    //     // VERIFY
    // }

    @Test
    fun fabClick_navigateToForm_success() {
        // GIVEN
        SUT = PurchasesViewModel(repository, storageRepository, ioDispatcher, savedStateHandle)
        // ACT
        SUT.fabClick(null)
        // VERIFY
        assertEquals(null, SUT.navigateToForm.getOrAwaitValue())
    }

    @Test
    fun deletePurchase_validatePurchaseIdPassedToTheRepository_success() {
        // GIVEN
        SUT = PurchasesViewModel(repository, storageRepository, ioDispatcher, savedStateHandle)
        // ACT
        SUT.deletePurchase(FAKE_PURCHASE.id)
        // VERIFY
        coVerify {
            repository.removePurchase(
                withArg { purchaseId ->
                    assertEquals(FAKE_PURCHASE.id, purchaseId)
                }
            )
        }
    }

    @Test
    fun addPurchase_validatePurchasePassedToRepository_success() {
        // GIVEN
        SUT = PurchasesViewModel(repository, storageRepository, ioDispatcher, savedStateHandle)
        // ACT
        SUT.addPurchase(FAKE_PURCHASE)
        // VERIFY
        coVerify {
            repository.savePurchase(
                withArg { purchase ->
                    assertEquals(FAKE_PURCHASE, purchase)
                }
            )
        }
    }

    companion object {
        private lateinit var SUT: PurchasesViewModel
    }
}
