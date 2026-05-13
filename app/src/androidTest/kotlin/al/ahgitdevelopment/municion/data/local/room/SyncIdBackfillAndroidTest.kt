package al.ahgitdevelopment.municion.data.local.room

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import al.ahgitdevelopment.municion.data.sync.SyncIdBackfill
import al.ahgitdevelopment.municion.data.sync.SyncIdGenerator
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for [SyncIdBackfill].
 *
 * Sets up a Room v33 database in the same state the
 * [MunicionDatabase.MIGRATION_32_33] leaves it in (placeholder syncIds
 * derived from the legacy id), then runs the backfill and asserts:
 *
 *  - Every placeholder is replaced with the deterministic UUID v3
 *    expected by [SyncIdGenerator.deterministicSyncId].
 *
 *  - Compra rows have `guia_sync_id` filled from the parent Guia's
 *    sync_id (after the parent's own backfill).
 *
 *  - Running the backfill twice is a no-op (idempotency).
 *
 *  - Cross-table id collisions are still resolved to different syncIds
 *    (because the entityType prefix differs in the seed).
 *
 * @since v3.3.0 (Sync redesign)
 */
@RunWith(AndroidJUnit4::class)
class SyncIdBackfillAndroidTest {

    private lateinit var db: MunicionDatabase
    private lateinit var backfill: SyncIdBackfill

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MunicionDatabase::class.java
        ).allowMainThreadQueries().build()
        backfill = SyncIdBackfill(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun backfill_replacesPlaceholderSyncIdsWithDeterministicUuids() = runBlocking {
        val licId = db.licenciaDao().insert(licencia(syncId = placeholder(5))).toInt()
        val guiaId = db.guiaDao().insert(guia(syncId = placeholder(10))).toInt()
        val tirId = db.tiradaDao().insert(tirada(syncId = placeholder(20))).toInt()

        backfill.run()

        val licAfter = db.licenciaDao().getLicenciaById(licId)!!
        val guiaAfter = db.guiaDao().getGuiaById(guiaId)!!
        val tirAfter = db.tiradaDao().getTiradaById(tirId)!!

        assertEquals(
            SyncIdGenerator.deterministicSyncId("Licencia", licId),
            licAfter.syncId
        )
        assertEquals(
            SyncIdGenerator.deterministicSyncId("Guia", guiaId),
            guiaAfter.syncId
        )
        assertEquals(
            SyncIdGenerator.deterministicSyncId("Tirada", tirId),
            tirAfter.syncId
        )
    }

    @Test
    fun backfill_setsGuiaSyncIdOnCompras() = runBlocking {
        val guia = guia(syncId = placeholder(7))
        val guiaId = db.guiaDao().insert(guia).toInt()

        val compra = compra(syncId = placeholder(13), idPosGuia = guiaId)
        val compraId = db.compraDao().insert(compra).toInt()

        backfill.run()

        val guiaAfter = db.guiaDao().getGuiaById(guiaId)!!
        val compraAfter = db.compraDao().getCompraById(compraId)!!

        assertEquals(guiaAfter.syncId, compraAfter.guiaSyncId)
    }

    @Test
    fun backfill_isIdempotent() = runBlocking {
        val licId = db.licenciaDao().insert(licencia(syncId = placeholder(5))).toInt()

        backfill.run()
        val first = db.licenciaDao().getLicenciaById(licId)!!.syncId
        backfill.run()
        val second = db.licenciaDao().getLicenciaById(licId)!!.syncId

        assertEquals(first, second)
        assertNotEquals(placeholder(5), first)
    }

    @Test
    fun backfill_doesNotChangeAlreadyMigratedRows() = runBlocking {
        val realSyncId = "550e8400-e29b-41d4-a716-446655440000"
        val licId = db.licenciaDao().insert(licencia(syncId = realSyncId)).toInt()

        backfill.run()

        val after = db.licenciaDao().getLicenciaById(licId)!!.syncId
        assertEquals(realSyncId, after)
    }

    @Test
    fun backfill_distinguishesEntityTypesWithSameLegacyId() = runBlocking {
        val licId = db.licenciaDao().insert(licencia(syncId = placeholder(1))).toInt()
        val guiaId = db.guiaDao().insert(guia(syncId = placeholder(1))).toInt()

        backfill.run()

        val licAfter = db.licenciaDao().getLicenciaById(licId)!!.syncId
        val guiaAfter = db.guiaDao().getGuiaById(guiaId)!!.syncId

        assertNotEquals(
            "Same legacy id across different tables must yield different syncIds",
            licAfter, guiaAfter
        )
        assertTrue(SyncIdGenerator.isValid(licAfter))
        assertTrue(SyncIdGenerator.isValid(guiaAfter))
    }

    private fun placeholder(id: Int): String =
        "00000000-0000-0000-0000-${id.toString().padStart(12, '0')}"

    private fun licencia(syncId: String) = Licencia(
        id = 0,
        tipo = 1,
        edad = 30,
        fechaExpedicion = "01/01/2020",
        fechaCaducidad = "01/01/2030",
        numLicencia = "L-${System.nanoTime()}",
        syncId = syncId
    )

    private fun guia(syncId: String) = Guia(
        id = 0,
        tipoLicencia = 1,
        marca = "M",
        modelo = "X",
        apodo = "A",
        tipoArma = 0,
        calibre1 = "9mm",
        numGuia = "G-${System.nanoTime()}",
        numArma = "A-${System.nanoTime()}",
        cupo = 100,
        syncId = syncId
    )

    private fun compra(syncId: String, idPosGuia: Int) = Compra(
        id = 0,
        idPosGuia = idPosGuia,
        calibre1 = "9mm",
        unidades = 50,
        precio = 25.0,
        fecha = "01/01/2024",
        tipo = "FMJ",
        peso = 115,
        marca = "Brand",
        syncId = syncId
    )

    private fun tirada(syncId: String) = Tirada(
        id = 0,
        descripcion = "Test",
        fecha = "01/01/2024",
        syncId = syncId
    )
}
