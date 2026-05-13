package al.ahgitdevelopment.municion.data.sync

import al.ahgitdevelopment.municion.data.local.room.dao.SyncOperationDao
import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.local.room.entities.SyncOperation
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("SyncOutboxEnqueuer")
class SyncOutboxEnqueuerTest {

    private val dao: SyncOperationDao = mockk(relaxed = true)
    private val enqueuer = SyncOutboxEnqueuer(dao)

    private val userId = "user-1"

    @Test
    @DisplayName("Skips enqueue when userId is null (anonymous flow)")
    fun skipsWhenUserNull() = runTest {
        enqueuer.enqueueUpsert(licenciaWithSyncId(), userId = null)
        coVerify(exactly = 0) { dao.enqueueCoalescing(any()) }
    }

    @Test
    @DisplayName("Rejects entity with blank syncId")
    fun rejectsBlankSyncId() = runTest {
        val noSyncId = licenciaWithSyncId().copy(syncId = "")
        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking { enqueuer.enqueueUpsert(noSyncId, userId) }
        }
    }

    @Test
    @DisplayName("Builds correct UPSERT operation for Licencia")
    fun upsertLicencia() = runTest {
        val captured = slot<SyncOperation>()
        coEvery { dao.enqueueCoalescing(capture(captured)) } returns 1L

        val licencia = licenciaWithSyncId()
        enqueuer.enqueueUpsert(licencia, userId)

        val op = captured.captured
        assertEquals("Licencia", op.entityType)
        assertEquals(licencia.syncId, op.entitySyncId)
        assertEquals(SyncOperation.Operation.UPSERT, op.operation)
        assertEquals(userId, op.userId)
        assertEquals(SyncOperation.Status.PENDING, op.status)
        assertNotNull(op.payloadJson)
        assertEquals(0L, op.lastAttemptAt ?: 0L)
        assertEquals(0, op.retryCount)
    }

    @Test
    @DisplayName("Builds correct entity type label for Guia, Compra, Tirada")
    fun differentEntityTypes() = runTest {
        val captured = mutableListOf<SyncOperation>()
        coEvery { dao.enqueueCoalescing(capture(captured)) } returns 1L

        enqueuer.enqueueUpsert(guiaWithSyncId(), userId)
        enqueuer.enqueueUpsert(compraWithSyncId(), userId)
        enqueuer.enqueueUpsert(tiradaWithSyncId(), userId)

        assertEquals(listOf("Guia", "Compra", "Tirada"), captured.map { it.entityType })
    }

    @Test
    @DisplayName("enqueueDelete builds DELETE operation")
    fun deleteOp() = runTest {
        val captured = slot<SyncOperation>()
        coEvery { dao.enqueueCoalescing(capture(captured)) } returns 1L

        enqueuer.enqueueDelete("Guia", "abc-def", "{}", userId)

        assertEquals(SyncOperation.Operation.DELETE, captured.captured.operation)
        assertEquals("Guia", captured.captured.entityType)
        assertEquals("abc-def", captured.captured.entitySyncId)
    }

    private fun licenciaWithSyncId() = Licencia(
        id = 1,
        tipo = 1,
        edad = 30,
        fechaExpedicion = "01/01/2020",
        fechaCaducidad = "01/01/2030",
        numLicencia = "L-1234",
        syncId = "550e8400-e29b-41d4-a716-446655440000"
    )

    private fun guiaWithSyncId() = Guia(
        id = 1,
        tipoLicencia = 1,
        marca = "Marca",
        modelo = "Modelo",
        apodo = "Apodo",
        tipoArma = 0,
        calibre1 = "9mm",
        numGuia = "G-1",
        numArma = "A-1",
        cupo = 100,
        syncId = "550e8400-e29b-41d4-a716-446655440001"
    )

    private fun compraWithSyncId() = Compra(
        id = 1,
        idPosGuia = 1,
        calibre1 = "9mm",
        unidades = 50,
        precio = 25.0,
        fecha = "01/01/2024",
        tipo = "FMJ",
        peso = 115,
        marca = "Brand",
        syncId = "550e8400-e29b-41d4-a716-446655440002"
    )

    private fun tiradaWithSyncId() = Tirada(
        id = 1,
        descripcion = "Test",
        fecha = "01/01/2024",
        syncId = "550e8400-e29b-41d4-a716-446655440003"
    )
}
