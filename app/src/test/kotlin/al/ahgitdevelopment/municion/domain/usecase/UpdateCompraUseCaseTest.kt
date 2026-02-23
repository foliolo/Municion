package al.ahgitdevelopment.municion.domain.usecase

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
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

@DisplayName("UpdateCompraUseCase")
class UpdateCompraUseCaseTest {

    private lateinit var compraRepository: CompraRepository
    private lateinit var guiaRepository: GuiaRepository
    private lateinit var crashlytics: FirebaseCrashlytics
    private lateinit var useCase: UpdateCompraUseCase

    private val userId = "test-user-id"

    private val guia = Guia(
        id = 1,
        tipoLicencia = 0,
        marca = "Tikka",
        modelo = "T3x",
        apodo = "Tikka T3x",
        tipoArma = 0,
        calibre1 = ".308",
        numGuia = "G-002",
        numArma = "TK67890",
        cupo = 200,
        gastado = 100
    )

    private fun compra(
        id: Int = 1,
        tienda: String = "Tienda",
        unidades: Int = 50
    ) = Compra(
        id = id,
        idPosGuia = guia.id,
        calibre1 = ".308",
        unidades = unidades,
        precio = 150.0,
        fecha = "23/02/2026",
        tipo = "FMJ",
        peso = 150,
        marca = "Lapua",
        tienda = tienda
    )

    @BeforeEach
    fun setUp() {
        compraRepository = mockk()
        guiaRepository = mockk()
        crashlytics = mockk(relaxed = true)
        useCase = UpdateCompraUseCase(compraRepository, guiaRepository, crashlytics)

        coEvery { guiaRepository.getGuiaById(guia.id) } returns guia
        coEvery { compraRepository.updateCompra(any(), any()) } returns Result.success(Unit)
        coEvery { guiaRepository.incrementGastado(any(), any(), any()) } returns Result.success(Unit)
        coEvery { guiaRepository.decrementGastado(any(), any(), any()) } returns Result.success(Unit)
    }

    @Test
    @DisplayName("Tienda→Tienda: ajusta diferencia de unidades con userId")
    fun update_tiendaToTienda_adjustsDifferenceWithUserId() = runTest {
        val old = compra(tienda = "Tienda", unidades = 50)
        val new = compra(tienda = "Tienda", unidades = 80)

        val result = useCase(old, new, userId)

        assertTrue(result.isSuccess)
        // Diferencia = 80 - 50 = 30 → increment
        coVerify { guiaRepository.incrementGastado(guia.id, 30, userId) }
    }

    @Test
    @DisplayName("Campo→Campo: sin cambios en cupo, con userId")
    fun update_campoToCampo_noQuotaChangeWithUserId() = runTest {
        val old = compra(tienda = "Campo de tiro", unidades = 50)
        val new = compra(tienda = "Campo de tiro", unidades = 80)

        val result = useCase(old, new, userId)

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { guiaRepository.incrementGastado(any(), any(), any()) }
        coVerify(exactly = 0) { guiaRepository.decrementGastado(any(), any(), any()) }
    }

    @Test
    @DisplayName("Tienda→Campo: libera unidades antiguas con userId")
    fun update_tiendaToCampo_releasesQuotaWithUserId() = runTest {
        val old = compra(tienda = "Tienda", unidades = 50)
        val new = compra(tienda = "Campo de tiro", unidades = 80)

        val result = useCase(old, new, userId)

        assertTrue(result.isSuccess)
        // Libera 50 unidades antiguas
        coVerify { guiaRepository.decrementGastado(guia.id, 50, userId) }
    }

    @Test
    @DisplayName("Campo→Tienda: añade unidades nuevas con userId")
    fun update_campoToTienda_addsQuotaWithUserId() = runTest {
        val old = compra(tienda = "Campo de tiro", unidades = 50)
        val new = compra(tienda = "Tienda", unidades = 80)

        val result = useCase(old, new, userId)

        assertTrue(result.isSuccess)
        // Añade 80 unidades nuevas
        coVerify { guiaRepository.incrementGastado(guia.id, 80, userId) }
    }

    @Test
    @DisplayName("Campo→Tienda: excede cupo retorna error")
    fun update_campoToTienda_exceedsCupo_returnsFailure() = runTest {
        // guia.disponible() = 200 - 100 = 100, requesting 150
        val old = compra(tienda = "Campo de tiro", unidades = 50)
        val new = compra(tienda = "Tienda", unidades = 150)

        val result = useCase(old, new, userId)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { compraRepository.updateCompra(any(), any()) }
    }
}
