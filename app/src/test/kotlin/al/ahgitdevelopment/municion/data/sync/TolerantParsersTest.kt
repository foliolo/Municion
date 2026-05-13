package al.ahgitdevelopment.municion.data.sync

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Locks in the central design guarantee: tolerant parsers NEVER discard
 * a parseable entity. Missing/invalid fields surface as `dataQuality`
 * rather than as silent data loss.
 */
@DisplayName("TolerantParsers")
class TolerantParsersTest {

    @Test
    @DisplayName("parseLicencia returns degraded entity when required fields are blank")
    fun licenciaDegraded() {
        val raw = mapOf<String, Any?>(
            "id" to 5,
            "syncId" to "550e8400-e29b-41d4-a716-446655440000",
            "tipo" to 0,
            "edad" to 25,
            "numLicencia" to "",          // blank
            "fechaExpedicion" to "",      // blank
            "fechaCaducidad" to "01/01/2030",
            "updatedAt" to 1_700_000_000_000L
        )
        val parsed = TolerantParsers.parseLicencia("550e8400-e29b-41d4-a716-446655440000", raw)
        assertNotNull(parsed)
        assertEquals("degraded", parsed!!.dataQuality)
        assertEquals("550e8400-e29b-41d4-a716-446655440000", parsed.syncId)
    }

    @Test
    @DisplayName("parseLicencia preserves valid entity as dataQuality=ok")
    fun licenciaOk() {
        val raw = mapOf<String, Any?>(
            "id" to 5,
            "syncId" to "550e8400-e29b-41d4-a716-446655440000",
            "tipo" to 1,
            "edad" to 30,
            "numLicencia" to "L-1234",
            "fechaExpedicion" to "01/01/2020",
            "fechaCaducidad" to "01/01/2030",
            "updatedAt" to 1_700_000_000_000L
        )
        val parsed = TolerantParsers.parseLicencia("550e8400-e29b-41d4-a716-446655440000", raw)
        assertEquals("ok", parsed?.dataQuality)
        assertEquals("L-1234", parsed?.numLicencia)
    }

    @Test
    @DisplayName("parseGuia detects {\"stability\": 0} as dataQuality=lost")
    fun guiaStabilityCorrupt() {
        val raw = mapOf<String, Any?>("stability" to 0)
        val parsed = TolerantParsers.parseGuia("19", raw)
        assertNotNull(parsed)
        assertEquals("lost", parsed!!.dataQuality)
        // Identity must still be derivable from the firebase key — otherwise
        // we couldn't tombstone or replace this corrupt row.
        assertEquals(SyncIdGenerator.deterministicSyncId("Guia", 19), parsed.syncId)
    }

    @Test
    @DisplayName("parseGuia survives missing required fields with safe defaults")
    fun guiaMissingFields() {
        val raw = mapOf<String, Any?>(
            "id" to 7,
            "tipoLicencia" to 1
            // every required string is missing
        )
        val parsed = TolerantParsers.parseGuia("7", raw)
        assertNotNull(parsed)
        assertEquals("degraded", parsed!!.dataQuality)
        assertEquals("Sin marca", parsed.marca)
        assertEquals("Sin modelo", parsed.modelo)
        assertEquals("N/A", parsed.calibre1)
        assertEquals("0000", parsed.numGuia)
        assertTrue(parsed.cupo >= 1)
    }

    @Test
    @DisplayName("parseCompra clamps invalid values without discarding")
    fun compraClamps() {
        val raw = mapOf<String, Any?>(
            "id" to 12,
            "syncId" to "550e8400-e29b-41d4-a716-446655440099",
            "idPosGuia" to 1,
            "calibre1" to "9mm",
            "unidades" to -5,        // invalid
            "precio" to -100.0,      // invalid
            "fecha" to "01/01/2020",
            "tipo" to "FMJ",
            "peso" to 0,             // invalid
            "marca" to "Winchester",
            "valoracion" to 99.0     // out of [0,5]
        )
        val parsed = TolerantParsers.parseCompra("550e8400-e29b-41d4-a716-446655440099", raw)
        assertNotNull(parsed)
        assertTrue(parsed!!.unidades >= 1, "unidades clamped to >= 1, was ${parsed.unidades}")
        assertTrue(parsed.precio >= 0.0)
        assertTrue(parsed.peso >= 1)
        assertTrue(parsed.valoracion in 0f..5f)
    }

    @Test
    @DisplayName("parseTirada survives missing fields with safe defaults")
    fun tiradaMissingFields() {
        val raw = mapOf<String, Any?>(
            "id" to 3,
            "puntuacion" to 5000      // far above max
        )
        val parsed = TolerantParsers.parseTirada("3", raw)
        assertNotNull(parsed)
        assertEquals("degraded", parsed!!.dataQuality)
        // puntuacion is clamped to the modalidad max (Precisión 600, IPSC 100)
        assertTrue(parsed.puntuacion <= 600)
    }

    @Test
    @DisplayName("returns null only when input is not a Map")
    fun nonMapReturnsNull() {
        assertNull(TolerantParsers.parseLicencia("k", "not a map"))
        assertNull(TolerantParsers.parseGuia("k", 42))
        assertNull(TolerantParsers.parseCompra("k", listOf("x")))
        assertNull(TolerantParsers.parseTirada("k", null))
    }

    @Test
    @DisplayName("resolves syncId from payload, key, or deterministic fallback")
    fun resolvesSyncId() {
        // From payload syncId
        val a = TolerantParsers.parseLicencia(
            firebaseKey = null,
            raw = mapOf(
                "syncId" to "550e8400-e29b-41d4-a716-446655440001",
                "id" to 1,
                "tipo" to 0,
                "edad" to 18,
                "numLicencia" to "X",
                "fechaExpedicion" to "01/01/2020",
                "fechaCaducidad" to "01/01/2030"
            )
        )
        assertEquals("550e8400-e29b-41d4-a716-446655440001", a?.syncId)

        // From firebaseKey when payload lacks syncId
        val keyUuid = "550e8400-e29b-41d4-a716-446655440002"
        val b = TolerantParsers.parseLicencia(
            firebaseKey = keyUuid,
            raw = mapOf("id" to 1, "tipo" to 0, "edad" to 18, "numLicencia" to "X",
                "fechaExpedicion" to "01/01/2020", "fechaCaducidad" to "01/01/2030")
        )
        assertEquals(keyUuid, b?.syncId)

        // From deterministic derivation (legacy int key, no syncId)
        val c = TolerantParsers.parseLicencia(
            firebaseKey = "5",
            raw = mapOf("id" to 5, "tipo" to 0, "edad" to 18, "numLicencia" to "X",
                "fechaExpedicion" to "01/01/2020", "fechaCaducidad" to "01/01/2030")
        )
        assertEquals(SyncIdGenerator.deterministicSyncId("Licencia", 5), c?.syncId)

        // Different legacy ids must produce different syncIds
        val d = TolerantParsers.parseLicencia(
            firebaseKey = "6",
            raw = mapOf("id" to 6, "tipo" to 0, "edad" to 18, "numLicencia" to "Y",
                "fechaExpedicion" to "01/01/2020", "fechaCaducidad" to "01/01/2030")
        )
        assertNotEquals(c?.syncId, d?.syncId)
    }
}
