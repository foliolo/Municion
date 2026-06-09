package al.ahgitdevelopment.municion.data.sync

import al.ahgitdevelopment.municion.data.sync.dto.GuiaDto
import al.ahgitdevelopment.municion.data.sync.dto.LicenciaDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TolerantParsersTest {

    private val validSyncId = "0d02e861-2e34-351b-b27f-311b6d60c54a"

    @Test
    fun parseLicencia_ok_whenAllRequiredPresent() {
        val dto = LicenciaDto(
            syncId = validSyncId,
            tipo = 1,
            numLicencia = "B-123",
            fechaExpedicion = "01/01/2020",
            fechaCaducidad = "01/01/2030",
        )
        val l = TolerantParsers.parseLicencia("k", dto)
        assertNotNull(l)
        assertEquals("ok", l.dataQuality)
        assertEquals(validSyncId, l.syncId)
        assertEquals("B-123", l.numLicencia)
    }

    @Test
    fun parseLicencia_degraded_whenRequiredMissing() {
        val dto = LicenciaDto(syncId = validSyncId, tipo = 1) // missing numLicencia + fechas
        val l = TolerantParsers.parseLicencia("k", dto)
        assertNotNull(l)
        assertEquals("degraded", l.dataQuality)
    }

    @Test
    fun parseLicencia_lost_whenStabilityCorrupt() {
        val dto = LicenciaDto(syncId = validSyncId, stability = 0) // only stability + syncId
        val l = TolerantParsers.parseLicencia("k", dto)
        assertNotNull(l)
        assertEquals("lost", l.dataQuality)
    }

    @Test
    fun parseLicencia_null_whenNoUsableSyncId() {
        assertNull(TolerantParsers.parseLicencia(null, LicenciaDto()))
        assertNull(TolerantParsers.parseLicencia(null, null))
    }

    @Test
    fun parseLicencia_derivesSyncIdFromFirebaseKey() {
        val l = TolerantParsers.parseLicencia(validSyncId, LicenciaDto(numLicencia = "X", fechaExpedicion = "01/01/2020", fechaCaducidad = "01/01/2030"))
        assertNotNull(l)
        assertEquals(validSyncId, l.syncId)
    }

    @Test
    fun parseGuia_fillsSafeDefaultsAndFlagsDegraded() {
        val g = TolerantParsers.parseGuia("k", GuiaDto(syncId = validSyncId))
        assertNotNull(g)
        assertEquals("degraded", g.dataQuality)
        assertEquals("Sin marca", g.marca)
        assertEquals(1, g.cupo) // coerced to >= 1
    }
}
