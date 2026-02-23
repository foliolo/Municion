package al.ahgitdevelopment.municion.domain.usecase

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.repository.CompraRepository
import al.ahgitdevelopment.municion.data.repository.GuiaRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("CreateCompraUseCase")
class CreateCompraUseCaseTest {

    private lateinit var compraRepository: CompraRepository
    private lateinit var guiaRepository: GuiaRepository
    private lateinit var crashlytics: FirebaseCrashlytics
    private lateinit var useCase: CreateCompraUseCase

    private val userId = "test-user-id"

    private val guia = Guia(
        id = 1,
        tipoLicencia = 0,
        marca = "Smith & Wesson",
        modelo = "Model 29",
        apodo = "S&W 29",
        tipoArma = 0,
        calibre1 = ".44 Magnum",
        numGuia = "G-001",
        numArma = "SW12345",
        cupo = 200,
        gastado = 50
    )

    private fun compra(tienda: String = "Tienda", unidades: Int = 75) = Compra(
        idPosGuia = guia.id,
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
        useCase = CreateCompraUseCase(compraRepository, guiaRepository, crashlytics)

        coEvery { guiaRepository.getGuiaById(guia.id) } returns guia
        coEvery { compraRepository.saveCompra(any(), any()) } returns Result.success(1L)
        coEvery { guiaRepository.incrementGastado(any(), any(), any()) } returns Result.success(Unit)
    }

    @Test
    @DisplayName("Compra en tienda incrementa gastado con userId para sync a Firebase")
    fun createCompra_inTienda_incrementsGastadoWithUserId() = runTest {
        val result = useCase(compra(tienda = "Tienda"), userId)

        assertTrue(result.isSuccess)
        coVerify { guiaRepository.incrementGastado(guia.id, 75, userId) }
    }

    @Test
    @DisplayName("Compra en campo de tiro NO incrementa gastado")
    fun createCompra_inCampoDeTiro_doesNotIncrementGastado() = runTest {
        val result = useCase(compra(tienda = "Campo de tiro"), userId)

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { guiaRepository.incrementGastado(any(), any(), any()) }
    }

    @Test
    @DisplayName("Compra en shooting range (EN) NO incrementa gastado")
    fun createCompra_inShootingRange_doesNotIncrementGastado() = runTest {
        val result = useCase(compra(tienda = "Shooting range"), userId)

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { guiaRepository.incrementGastado(any(), any(), any()) }
    }

    @Test
    @DisplayName("Compra que excede cupo retorna error")
    fun createCompra_exceedsCupo_returnsFailure() = runTest {
        // guia.disponible() = 200 - 50 = 150, requesting 200
        val result = useCase(compra(unidades = 200), userId)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { compraRepository.saveCompra(any(), any()) }
    }

    @Test
    @DisplayName("Guarda compra ANTES de incrementar gastado")
    fun createCompra_savesCompraBeforeIncrementingGastado() = runTest {
        useCase(compra(tienda = "Tienda"), userId)

        coVerifyOrder {
            compraRepository.saveCompra(any(), userId)
            guiaRepository.incrementGastado(guia.id, 75, userId)
        }
    }
}
