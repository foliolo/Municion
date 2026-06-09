package al.ahgitdevelopment.municion.data.sync

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import al.ahgitdevelopment.municion.data.sync.dto.CompraDto
import al.ahgitdevelopment.municion.data.sync.dto.GuiaDto
import al.ahgitdevelopment.municion.data.sync.dto.LicenciaDto
import al.ahgitdevelopment.municion.data.sync.dto.TiradaDto

/**
 * Tolerant converters from Firebase DTOs to Room entities.
 *
 * Contract (unchanged from the Android sync redesign):
 *  - Never throw. Return an entity with safe defaults and `dataQuality = "degraded"` when a
 *    required field is missing; `"lost"` for the legacy `{stability: n}` corruption pattern.
 *  - Return null only when no usable syncId can be derived (the row is then reported as a
 *    ParseError by the repository and skipped).
 *
 * syncId resolution order: dto.syncId (valid UUID) → firebaseKey (valid UUID) →
 * deterministic UUID from (entityType, dto.id or firebaseKey-as-int).
 */
object TolerantParsers {
    private fun String?.nb(): String? = this?.takeIf { it.isNotBlank() }

    private fun resolveSyncId(
        firebaseKey: String?,
        dtoSyncId: String?,
        legacyId: Int?,
        entityType: String,
    ): String? {
        dtoSyncId?.takeIf { SyncIdGenerator.isValid(it) }?.let { return it }
        if (firebaseKey != null && SyncIdGenerator.isValid(firebaseKey)) return firebaseKey
        val id = legacyId ?: firebaseKey?.toIntOrNull()
        if (id != null && id >= 0) return SyncIdGenerator.deterministicSyncId(entityType, id)
        return null
    }

    fun parseLicencia(
        firebaseKey: String?,
        dto: LicenciaDto?,
    ): Licencia? {
        if (dto == null) return null
        val syncId = resolveSyncId(firebaseKey, dto.syncId, dto.id, "Licencia") ?: return null

        val numLicencia = dto.numLicencia.orEmpty()
        val fechaExpedicion = dto.fechaExpedicion.orEmpty()
        val fechaCaducidad = dto.fechaCaducidad.orEmpty()
        val stabilityCorrupt =
            dto.stability != null &&
                numLicencia.isBlank() &&
                fechaExpedicion.isBlank() &&
                fechaCaducidad.isBlank()
        val missingRequired = numLicencia.isBlank() || fechaExpedicion.isBlank() || fechaCaducidad.isBlank()

        return Licencia(
            id = dto.id ?: 0,
            tipo = dto.tipo ?: 0,
            nombre = dto.nombre.nb(),
            tipoPermisoConduccion = dto.tipoPermisoConduccion ?: -1,
            edad = dto.edad ?: 18,
            fechaExpedicion = fechaExpedicion,
            fechaCaducidad = fechaCaducidad,
            numLicencia = numLicencia,
            numAbonado = dto.numAbonado ?: -1,
            numSeguro = dto.numSeguro.nb(),
            autonomia = dto.autonomia ?: -1,
            escala = dto.escala ?: -1,
            categoria = dto.categoria ?: -1,
            fotoUrl = dto.fotoUrl.nb(),
            storagePath = dto.storagePath.nb(),
            updatedAt = dto.updatedAt ?: 0L,
            syncId = syncId,
            deleted = dto.deleted ?: false,
            deletedAt = dto.deletedAt,
            dataQuality = quality(stabilityCorrupt, missingRequired),
        )
    }

    fun parseGuia(
        firebaseKey: String?,
        dto: GuiaDto?,
    ): Guia? {
        if (dto == null) return null
        val syncId = resolveSyncId(firebaseKey, dto.syncId, dto.id, "Guia") ?: return null

        val marca = dto.marca.orEmpty()
        val modelo = dto.modelo.orEmpty()
        val apodo = dto.apodo.orEmpty()
        val calibre1 = dto.calibre1.orEmpty()
        val numGuia = dto.numGuia.orEmpty()
        val numArma = dto.numArma.orEmpty()
        val stabilityCorrupt =
            dto.stability != null &&
                marca.isBlank() &&
                modelo.isBlank() &&
                apodo.isBlank() &&
                numGuia.isBlank()
        val missingRequired =
            marca.isBlank() ||
                modelo.isBlank() ||
                apodo.isBlank() ||
                calibre1.isBlank() ||
                numGuia.isBlank() ||
                numArma.isBlank()

        return Guia(
            id = dto.id ?: 0,
            idCompra = dto.idCompra ?: 0,
            tipoLicencia = dto.tipoLicencia ?: 0,
            marca = marca.ifBlank { "Sin marca" },
            modelo = modelo.ifBlank { "Sin modelo" },
            apodo = apodo.ifBlank { "Sin apodo" },
            tipoArma = dto.tipoArma ?: 0,
            calibre1 = calibre1.ifBlank { "N/A" },
            calibre2 = dto.calibre2.nb(),
            numGuia = numGuia.ifBlank { "0000" },
            numArma = numArma.ifBlank { "N/A" },
            cupo = (dto.cupo ?: 1).coerceAtLeast(1),
            gastado = (dto.gastado ?: 0).coerceAtLeast(0),
            imagePath = dto.imagePath.nb(),
            fotoUrl = dto.fotoUrl.nb(),
            storagePath = dto.storagePath.nb(),
            updatedAt = dto.updatedAt ?: 0L,
            syncId = syncId,
            deleted = dto.deleted ?: false,
            deletedAt = dto.deletedAt,
            dataQuality = quality(stabilityCorrupt, missingRequired),
        )
    }

    fun parseCompra(
        firebaseKey: String?,
        dto: CompraDto?,
    ): Compra? {
        if (dto == null) return null
        val syncId = resolveSyncId(firebaseKey, dto.syncId, dto.id, "Compra") ?: return null

        val calibre1 = dto.calibre1.orEmpty()
        val fecha = dto.fecha.orEmpty()
        val tipo = dto.tipo.orEmpty()
        val marca = dto.marca.orEmpty()
        val stabilityCorrupt =
            dto.stability != null &&
                calibre1.isBlank() &&
                fecha.isBlank() &&
                tipo.isBlank() &&
                marca.isBlank()
        val missingRequired = calibre1.isBlank() || fecha.isBlank() || tipo.isBlank() || marca.isBlank()

        return Compra(
            id = dto.id ?: 0,
            idPosGuia = dto.idPosGuia ?: 0,
            calibre1 = calibre1.ifBlank { "N/A" },
            calibre2 = dto.calibre2.nb(),
            unidades = (dto.unidades ?: 1).coerceAtLeast(1),
            precio = (dto.precio ?: 0.0).coerceAtLeast(0.0),
            fecha = fecha.ifBlank { "01/01/2000" },
            tipo = tipo.ifBlank { "N/A" },
            peso = (dto.peso ?: 1).coerceAtLeast(1),
            marca = marca.ifBlank { "Sin marca" },
            tienda = dto.tienda.nb(),
            valoracion = (dto.valoracion ?: 0.0).toFloat().coerceIn(0f, 5f),
            imagePath = dto.imagePath.nb(),
            fotoUrl = dto.fotoUrl.nb(),
            storagePath = dto.storagePath.nb(),
            updatedAt = dto.updatedAt ?: 0L,
            syncId = syncId,
            guiaSyncId = dto.guiaSyncId.nb(),
            deleted = dto.deleted ?: false,
            deletedAt = dto.deletedAt,
            dataQuality = quality(stabilityCorrupt, missingRequired),
        )
    }

    fun parseTirada(
        firebaseKey: String?,
        dto: TiradaDto?,
    ): Tirada? {
        if (dto == null) return null
        val syncId = resolveSyncId(firebaseKey, dto.syncId, dto.id, "Tirada") ?: return null

        val descripcion = dto.descripcion.orEmpty()
        val fecha = dto.fecha.orEmpty()
        val modalidad = dto.modalidad.nb()
        val maxPuntuacion = Tirada.getMaxPuntuacion(modalidad)
        val stabilityCorrupt = dto.stability != null && descripcion.isBlank() && fecha.isBlank()
        val missingRequired = descripcion.isBlank() || fecha.isBlank()

        return Tirada(
            id = dto.id ?: 0,
            descripcion = descripcion.ifBlank { "Sin descripción" },
            localizacion = dto.rango.nb(),
            categoria = dto.categoria.nb(),
            modalidad = modalidad,
            fecha = fecha.ifBlank { "01/01/2000" },
            puntuacion = (dto.puntuacion ?: 0).coerceIn(0, maxPuntuacion),
            updatedAt = dto.updatedAt ?: 0L,
            syncId = syncId,
            deleted = dto.deleted ?: false,
            deletedAt = dto.deletedAt,
            dataQuality = quality(stabilityCorrupt, missingRequired),
        )
    }

    private fun quality(
        stabilityCorrupt: Boolean,
        missingRequired: Boolean,
    ): String =
        when {
            stabilityCorrupt -> "lost"
            missingRequired -> "degraded"
            else -> "ok"
        }
}
