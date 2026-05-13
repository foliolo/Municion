package al.ahgitdevelopment.municion.data.sync

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("SyncOutboxConfig")
class SyncOutboxConfigTest {

    @Test
    @DisplayName("computeBackoffMs is 0 at retry 0, grows, and caps at the configured max")
    fun backoffCurve() {
        assertEquals(0L, SyncOutboxConfig.computeBackoffMs(0))
        assertEquals(SyncOutboxConfig.BACKOFF_BASE_MS, SyncOutboxConfig.computeBackoffMs(1))
        assertEquals(SyncOutboxConfig.BACKOFF_BASE_MS * 2, SyncOutboxConfig.computeBackoffMs(2))
        assertEquals(SyncOutboxConfig.BACKOFF_BASE_MS * 4, SyncOutboxConfig.computeBackoffMs(3))

        // At a high retry count the value must still be <= BACKOFF_MAX_MS,
        // not negative (overflow) and not zero.
        val highRetry = SyncOutboxConfig.computeBackoffMs(1000)
        assertTrue(highRetry > 0L)
        assertTrue(highRetry <= SyncOutboxConfig.BACKOFF_MAX_MS,
            "Backoff at 1000 retries should be capped at BACKOFF_MAX_MS; was $highRetry")
    }

    @Test
    @DisplayName("firebasePathFor maps each entity type")
    fun firebasePath() {
        assertEquals("licencias", SyncOutboxConfig.firebasePathFor("Licencia"))
        assertEquals("guias", SyncOutboxConfig.firebasePathFor("Guia"))
        assertEquals("compras", SyncOutboxConfig.firebasePathFor("Compra"))
        assertEquals("tiradas", SyncOutboxConfig.firebasePathFor("Tirada"))
    }

    @Test
    @DisplayName("firebasePathFor rejects unknown entity types")
    fun firebasePathUnknown() {
        assertThrows(IllegalArgumentException::class.java) {
            SyncOutboxConfig.firebasePathFor("Unknown")
        }
    }
}
