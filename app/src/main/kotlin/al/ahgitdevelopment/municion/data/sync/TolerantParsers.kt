package al.ahgitdevelopment.municion.data.sync

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import android.util.Log

/**
 * Tolerant parsers for Firebase entity maps.
 *
 * Design contract:
 *
 *  - Never throw. Never return null for "missing required field" — return
 *    an entity with safe defaults and `dataQuality = "degraded"` instead.
 *
 *  - Return null only when the input is structurally unusable: not a Map,
 *    no usable syncId derivable from anywhere, or matches the legacy
 *    `{"stability": 0}` corruption pattern AND no field of the original
 *    entity survived.
 *
 *  - When a value is the legacy stability-corrupt placeholder, we still
 *    return an entity (so the user sees something to recover) with
 *    `dataQuality = "lost"` and all visible fields blanked. Tombstoning
 *    these is the user's choice, not ours.
 *
 *  - syncId resolution order:
 *      1. payload["syncId"] if a valid UUID
 *      2. firebaseKey if a valid UUID
 *      3. deterministic UUID derived from (entityType, payload["id"] or
 *         firebaseKey-as-int)
 *      4. give up (rare; logged)
 *
 * @since v3.3.0 (Sync redesign)
 */
object TolerantParsers {

    private const val TAG = "TolerantParsers"

    /**
     * The catastrophic "stability bug" pattern from v3.0.0-v3.1.1: a single
     * `stability` field is the only thing left of the entity.
     */
    private fun isStabilityCorrupt(map: Map<String, Any?>): Boolean {
        return map.keys.size <= 2 && "stability" in map.keys
    }

    private fun resolveSyncId(
        firebaseKey: String?,
        payload: Map<String, Any?>,
        entityType: String
    ): String? {
        // 1) payload["syncId"]
        (payload["syncId"] as? String)?.takeIf { SyncIdGenerator.isValid(it) }?.let { return it }

        // 2) firebaseKey as UUID
        if (firebaseKey != null && SyncIdGenerator.isValid(firebaseKey)) {
            return firebaseKey
        }

        // 3) deterministic from legacy id
        val legacyId = (payload["id"] as? Number)?.toInt()
            ?: firebaseKey?.toIntOrNull()
        if (legacyId != null && legacyId >= 0) {
            return SyncIdGenerator.deterministicSyncId(entityType, legacyId)
        }

        return null
    }

    private fun safeString(value: Any?, default: String = ""): String {
        return when (value) {
            is String -> value
            is Number -> value.toString()
            null -> default
            else -> default
        }
    }

    private fun nullableString(value: Any?): String? = (value as? String)?.takeIf { it.isNotBlank() }

    private fun safeInt(value: Any?, default: Int = 0): Int {
        return when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: default
            else -> default
        }
    }

    private fun safeLong(value: Any?, default: Long = 0L): Long {
        return when (value) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: default
            else -> default
        }
    }

    private fun safeDouble(value: Any?, default: Double = 0.0): Double {
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: default
            else -> default
        }
    }

    private fun safeBoolean(value: Any?, default: Boolean = false): Boolean {
        return when (value) {
            is Boolean -> value
            is Number -> value.toInt() != 0
            is String -> value.equals("true", ignoreCase = true) || value == "1"
            else -> default
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun parseLicencia(firebaseKey: String?, raw: Any?): Licencia? {
        val map = raw as? Map<String, Any?> ?: return null
        val syncId = resolveSyncId(firebaseKey, map, "Licencia") ?: run {
            Log.w(TAG, "Licencia[$firebaseKey] has no usable syncId; skipping")
            return null
        }

        val stabilityCorrupt = isStabilityCorrupt(map)

        val numLicencia = safeString(map["numLicencia"])
        val fechaExpedicion = safeString(map["fechaExpedicion"])
        val fechaCaducidad = safeString(map["fechaCaducidad"])
        val tipo = safeInt(map["tipo"], default = 0)
        val edad = safeInt(map["edad"], default = 18)

        val missingRequired = numLicencia.isBlank() ||
                fechaExpedicion.isBlank() ||
                fechaCaducidad.isBlank()

        val dataQuality = when {
            stabilityCorrupt -> "lost"
            missingRequired -> "degraded"
            else -> "ok"
        }

        return Licencia(
            id = safeInt(map["id"]),
            tipo = tipo,
            nombre = nullableString(map["nombre"]),
            tipoPermisoConduccion = safeInt(map["tipoPermisoConduccion"], default = -1),
            edad = edad,
            fechaExpedicion = fechaExpedicion,
            fechaCaducidad = fechaCaducidad,
            numLicencia = numLicencia,
            numAbonado = safeInt(map["numAbonado"], default = -1),
            numSeguro = nullableString(map["numSeguro"]),
            autonomia = safeInt(map["autonomia"], default = -1),
            escala = safeInt(map["escala"], default = -1),
            categoria = safeInt(map["categoria"], default = -1),
            fotoUrl = nullableString(map["fotoUrl"]),
            storagePath = nullableString(map["storagePath"]),
            updatedAt = safeLong(map["updatedAt"], default = 0L),
            syncId = syncId,
            deleted = safeBoolean(map["deleted"]),
            deletedAt = (map["deletedAt"] as? Number)?.toLong(),
            dataQuality = dataQuality
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun parseGuia(firebaseKey: String?, raw: Any?): Guia? {
        val map = raw as? Map<String, Any?> ?: return null
        val syncId = resolveSyncId(firebaseKey, map, "Guia") ?: run {
            Log.w(TAG, "Guia[$firebaseKey] has no usable syncId; skipping")
            return null
        }
        val stabilityCorrupt = isStabilityCorrupt(map)

        val tipoLicencia = safeInt(map["tipoLicencia"], default = 0)
        val marca = safeString(map["marca"])
        val modelo = safeString(map["modelo"])
        val apodo = safeString(map["apodo"])
        val calibre1 = safeString(map["calibre1"])
        val numGuia = safeString(map["numGuia"])
        val numArma = safeString(map["numArma"])
        val cupo = safeInt(map["cupo"], default = 1).coerceAtLeast(1)

        val missingRequired = marca.isBlank() || modelo.isBlank() || apodo.isBlank() ||
                calibre1.isBlank() || numGuia.isBlank() || numArma.isBlank()

        val dataQuality = when {
            stabilityCorrupt -> "lost"
            missingRequired -> "degraded"
            else -> "ok"
        }

        return Guia(
            id = safeInt(map["id"]),
            idCompra = safeInt(map["idCompra"], default = 0),
            tipoLicencia = tipoLicencia,
            marca = marca.ifBlank { "Sin marca" },
            modelo = modelo.ifBlank { "Sin modelo" },
            apodo = apodo.ifBlank { "Sin apodo" },
            tipoArma = safeInt(map["tipoArma"], default = 0),
            calibre1 = calibre1.ifBlank { "N/A" },
            calibre2 = nullableString(map["calibre2"]),
            numGuia = numGuia.ifBlank { "0000" },
            numArma = numArma.ifBlank { "N/A" },
            cupo = cupo,
            gastado = safeInt(map["gastado"], default = 0).coerceAtLeast(0),
            imagePath = nullableString(map["imagePath"]),
            fotoUrl = nullableString(map["fotoUrl"]),
            storagePath = nullableString(map["storagePath"]),
            updatedAt = safeLong(map["updatedAt"], default = 0L),
            syncId = syncId,
            deleted = safeBoolean(map["deleted"]),
            deletedAt = (map["deletedAt"] as? Number)?.toLong(),
            dataQuality = dataQuality
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun parseCompra(firebaseKey: String?, raw: Any?): Compra? {
        val map = raw as? Map<String, Any?> ?: return null
        val syncId = resolveSyncId(firebaseKey, map, "Compra") ?: run {
            Log.w(TAG, "Compra[$firebaseKey] has no usable syncId; skipping")
            return null
        }
        val stabilityCorrupt = isStabilityCorrupt(map)

        val calibre1 = safeString(map["calibre1"])
        val unidades = safeInt(map["unidades"], default = 1).coerceAtLeast(1)
        val precio = safeDouble(map["precio"], default = 0.0).coerceAtLeast(0.0)
        val fecha = safeString(map["fecha"])
        val tipo = safeString(map["tipo"])
        val peso = safeInt(map["peso"], default = 1).coerceAtLeast(1)
        val marca = safeString(map["marca"])

        val missingRequired = calibre1.isBlank() || fecha.isBlank() ||
                tipo.isBlank() || marca.isBlank()

        val dataQuality = when {
            stabilityCorrupt -> "lost"
            missingRequired -> "degraded"
            else -> "ok"
        }

        return Compra(
            id = safeInt(map["id"]),
            idPosGuia = safeInt(map["idPosGuia"], default = 0),
            calibre1 = calibre1.ifBlank { "N/A" },
            calibre2 = nullableString(map["calibre2"]),
            unidades = unidades,
            precio = precio,
            fecha = fecha.ifBlank { "01/01/2000" },
            tipo = tipo.ifBlank { "N/A" },
            peso = peso,
            marca = marca.ifBlank { "Sin marca" },
            tienda = nullableString(map["tienda"]),
            valoracion = (safeDouble(map["valoracion"], default = 0.0).toFloat()).coerceIn(0f, 5f),
            imagePath = nullableString(map["imagePath"]),
            fotoUrl = nullableString(map["fotoUrl"]),
            storagePath = nullableString(map["storagePath"]),
            updatedAt = safeLong(map["updatedAt"], default = 0L),
            syncId = syncId,
            guiaSyncId = nullableString(map["guiaSyncId"]),
            deleted = safeBoolean(map["deleted"]),
            deletedAt = (map["deletedAt"] as? Number)?.toLong(),
            dataQuality = dataQuality
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun parseTirada(firebaseKey: String?, raw: Any?): Tirada? {
        val map = raw as? Map<String, Any?> ?: return null
        val syncId = resolveSyncId(firebaseKey, map, "Tirada") ?: run {
            Log.w(TAG, "Tirada[$firebaseKey] has no usable syncId; skipping")
            return null
        }
        val stabilityCorrupt = isStabilityCorrupt(map)

        val descripcion = safeString(map["descripcion"])
        val fecha = safeString(map["fecha"])
        val modalidad = nullableString(map["modalidad"])
        val maxPuntuacion = Tirada.getMaxPuntuacion(modalidad)
        val puntuacion = safeInt(map["puntuacion"], default = 0).coerceIn(0, maxPuntuacion)

        val missingRequired = descripcion.isBlank() || fecha.isBlank()

        val dataQuality = when {
            stabilityCorrupt -> "lost"
            missingRequired -> "degraded"
            else -> "ok"
        }

        return Tirada(
            id = safeInt(map["id"]),
            descripcion = descripcion.ifBlank { "Sin descripción" },
            localizacion = nullableString(map["rango"]),
            categoria = nullableString(map["categoria"]),
            modalidad = modalidad,
            fecha = fecha.ifBlank { "01/01/2000" },
            puntuacion = puntuacion,
            updatedAt = safeLong(map["updatedAt"], default = 0L),
            syncId = syncId,
            deleted = safeBoolean(map["deleted"]),
            deletedAt = (map["deletedAt"] as? Number)?.toLong(),
            dataQuality = dataQuality
        )
    }
}
