package al.ahgitdevelopment.municion.data.sync

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID

@DisplayName("SyncIdGenerator")
class SyncIdGeneratorTest {

    @Test
    @DisplayName("newSyncId returns valid UUIDs that differ across calls")
    fun `newSyncId returns distinct UUIDs`() {
        val a = SyncIdGenerator.newSyncId()
        val b = SyncIdGenerator.newSyncId()
        assertNotEquals(a, b)
        UUID.fromString(a) // throws if invalid
        UUID.fromString(b)
    }

    @Test
    @DisplayName("deterministicSyncId is stable for the same (table, id)")
    fun `deterministicSyncId is stable`() {
        val a = SyncIdGenerator.deterministicSyncId("Licencia", 42)
        val b = SyncIdGenerator.deterministicSyncId("Licencia", 42)
        assertEquals(a, b)
    }

    @Test
    @DisplayName("deterministicSyncId differs across tables")
    fun `deterministicSyncId differs across tables`() {
        val licencia = SyncIdGenerator.deterministicSyncId("Licencia", 1)
        val guia = SyncIdGenerator.deterministicSyncId("Guia", 1)
        val compra = SyncIdGenerator.deterministicSyncId("Compra", 1)
        val tirada = SyncIdGenerator.deterministicSyncId("Tirada", 1)
        val all = setOf(licencia, guia, compra, tirada)
        assertEquals(4, all.size, "Same id across different tables must produce different syncIds")
    }

    @Test
    @DisplayName("deterministicSyncId differs across ids within the same table")
    fun `deterministicSyncId differs across ids`() {
        val a = SyncIdGenerator.deterministicSyncId("Guia", 1)
        val b = SyncIdGenerator.deterministicSyncId("Guia", 2)
        assertNotEquals(a, b)
    }

    @Test
    @DisplayName("isValid accepts well-formed UUIDs and rejects garbage")
    fun `isValid rejects garbage`() {
        assertTrue(SyncIdGenerator.isValid("550e8400-e29b-41d4-a716-446655440000"))
        assertTrue(SyncIdGenerator.isValid(SyncIdGenerator.newSyncId()))
        assertTrue(SyncIdGenerator.isValid(SyncIdGenerator.deterministicSyncId("X", 1)))

        assertFalse(SyncIdGenerator.isValid(null))
        assertFalse(SyncIdGenerator.isValid(""))
        assertFalse(SyncIdGenerator.isValid("not-a-uuid"))
        assertFalse(SyncIdGenerator.isValid("123"))
    }

    @Test
    @DisplayName("deterministicSyncId rejects blank table")
    fun `deterministicSyncId rejects blank table`() {
        try {
            SyncIdGenerator.deterministicSyncId("", 1)
            throw AssertionError("expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // ok
        }
    }
}
