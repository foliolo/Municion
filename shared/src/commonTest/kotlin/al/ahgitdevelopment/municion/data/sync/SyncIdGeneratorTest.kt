package al.ahgitdevelopment.municion.data.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SyncIdGeneratorTest {
    /**
     * Golden vectors generated from `java.util.UUID.nameUUIDFromBytes` (the Android
     * implementation). The KMP MD5/v3 path MUST reproduce them byte-for-byte, otherwise
     * cross-device dedup breaks and legacy data won't converge.
     */
    @Test
    fun deterministicSyncId_matchesJavaNameUuidFromBytes() {
        assertEquals("0d02e861-2e34-351b-b27f-311b6d60c54a", SyncIdGenerator.deterministicSyncId("Licencia", 1))
        assertEquals("f052a6d9-711b-3f7b-a800-6c3213286324", SyncIdGenerator.deterministicSyncId("Guia", 1))
        assertEquals("d3675bb8-fd40-3472-a9e8-3655e7926b03", SyncIdGenerator.deterministicSyncId("Compra", 42))
        assertEquals("beb9a2df-37a3-3377-88bc-27603d262a42", SyncIdGenerator.deterministicSyncId("Tirada", 7))
    }

    @Test
    fun deterministicSyncId_isStable() {
        assertEquals(
            SyncIdGenerator.deterministicSyncId("Guia", 99),
            SyncIdGenerator.deterministicSyncId("Guia", 99),
        )
    }

    @Test
    fun newSyncId_isValidV4() {
        val id = SyncIdGenerator.newSyncId()
        assertTrue(SyncIdGenerator.isValid(id))
        assertEquals('4', id[14]) // version nibble of a v4 UUID
    }

    @Test
    fun isValid_rejectsGarbage() {
        assertFalse(SyncIdGenerator.isValid("not-a-uuid"))
        assertFalse(SyncIdGenerator.isValid(""))
        assertFalse(SyncIdGenerator.isValid(null))
    }
}
