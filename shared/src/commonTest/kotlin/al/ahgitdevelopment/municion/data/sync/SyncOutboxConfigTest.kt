package al.ahgitdevelopment.municion.data.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SyncOutboxConfigTest {

    @Test
    fun firebasePathFor_mapsEntityTypes() {
        assertEquals("licencias", SyncOutboxConfig.firebasePathFor("Licencia"))
        assertEquals("guias", SyncOutboxConfig.firebasePathFor("Guia"))
        assertEquals("compras", SyncOutboxConfig.firebasePathFor("Compra"))
        assertEquals("tiradas", SyncOutboxConfig.firebasePathFor("Tirada"))
    }

    @Test
    fun firebasePathFor_unknownThrows() {
        assertFailsWith<IllegalArgumentException> { SyncOutboxConfig.firebasePathFor("Unknown") }
    }

    @Test
    fun computeBackoff_zeroForNoRetries() {
        assertEquals(0L, SyncOutboxConfig.computeBackoffMs(0))
        assertEquals(0L, SyncOutboxConfig.computeBackoffMs(-1))
    }

    @Test
    fun computeBackoff_isExponential() {
        assertEquals(SyncOutboxConfig.BACKOFF_BASE_MS, SyncOutboxConfig.computeBackoffMs(1))
        assertEquals(SyncOutboxConfig.BACKOFF_BASE_MS * 2, SyncOutboxConfig.computeBackoffMs(2))
        assertEquals(SyncOutboxConfig.BACKOFF_BASE_MS * 4, SyncOutboxConfig.computeBackoffMs(3))
    }

    @Test
    fun computeBackoff_cappedAtMax() {
        assertEquals(SyncOutboxConfig.BACKOFF_MAX_MS, SyncOutboxConfig.computeBackoffMs(50))
    }
}
