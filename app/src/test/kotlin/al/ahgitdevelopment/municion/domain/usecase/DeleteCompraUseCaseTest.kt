package al.ahgitdevelopment.municion.domain.usecase

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.repository.CompraRepository
import al.ahgitdevelopment.municion.data.repository.GuiaRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("DeleteCompraUseCase")
class DeleteCompraUseCaseTest {

    private lateinit var compraRepository: CompraRepository
    private lateinit var guiaRepository: GuiaRepository
    private lateinit var crashlytics: FirebaseCrashlytics
    private lateinit var useCase: DeleteCompraUseCase

    private val userId = "test-user-id"

    private fun compra(tienda: String = "Tienda", unidades: Int = 75) = Compra(
        id = 1,
        idPosGuia = 1,
        calibre1 = ".44 Magnum",
        unidades = unidades,
        precio = 150.0,
        fecha = "23/02/2026",
        tipo = "FMJ",
        peso = 240,
        marca = "Winchester",
        tienda = tienda
    )

    @BeforeEach
    fun setUp() {
        compraRepository = mockk()
        guiaRepository = mockk()
        crashlytics = mockk(relaxed = true)
        useCase = DeleteCompraUseCase(compraRepository, guiaRepository, crashlytics)

        coEvery { compraRepository.deleteCompra(any(), any()) } returns Result.success(Unit)
        coEvery { guiaRepository.decrementGastado(any(), any(), any()) } returns Result.success(Unit)
    }

    @Test
    @DisplayName("Borrar compra de tienda decrementa gastado con userId para sync")
    fun delete_tiendaCompra_decrementsGastadoWithUserId() = runTest {
        val result = useCase(compra(tienda = "Tienda"), userId)

        assertTrue(result.isSuccess)
        coVerify { guiaRepository.decrementGastado(1, 75, userId) }
    }

    @Test
    @DisplayName("Borrar compra de campo de tiro NO decrementa gastado")
    fun delete_campoCompra_doesNotDecrementGastado() = runTest {
        val result = useCase(compra(tienda = "Campo de tiro"), userId)

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { guiaRepository.decrementGastado(any(), any(), any()) }
    }
}
