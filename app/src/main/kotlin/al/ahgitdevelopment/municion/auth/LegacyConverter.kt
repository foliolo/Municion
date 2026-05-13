package al.ahgitdevelopment.municion.auth

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import al.ahgitdevelopment.municion.data.sync.SyncIdGenerator
import android.util.Log
import al.ahgitdevelopment.municion.datamodel.Guia as LegacyGuia
import al.ahgitdevelopment.municion.datamodel.Compra as LegacyCompra
import al.ahgitdevelopment.municion.datamodel.Licencia as LegacyLicencia
import al.ahgitdevelopment.municion.datamodel.Tirada as LegacyTirada

/**
 * LegacyConverter - Convierte objetos de datamodel legacy a Room entities
 *
 * FASE 4: Legacy Migration
 * - Convierte datamodel.Guia → room.entities.Guia
 * - Convierte datamodel.Compra → room.entities.Compra
 * - Convierte datamodel.Licencia → room.entities.Licencia
 * - Convierte datamodel.Tirada → room.entities.Tirada
 * - Maneja valores null/empty con defaults seguros
 *
 * @since v3.0.0 (TRACK B - Auth Modernization)
 */
object LegacyConverter {

    /**
     * Convierte Guia legacy a Room entity.
     *
     * Asigna [syncId] deterministicamente desde el legacy.id, igual que hace
     * [al.ahgitdevelopment.municion.data.local.room.MunicionDatabase.Companion.MIGRATION_32_33]
     * para usuarios que ya estaban en v3.x. De esta forma:
     *   - Si la misma Guía existe en Firebase porque otro device la subió,
     *     ambos convergen al mismo syncId y la sincronización no duplica.
     *   - El UNIQUE INDEX sobre sync_id en Room no rompe por strings vacíos
     *     duplicados al hacer batch insert de las legacy.
     */
    fun convertGuia(legacy: LegacyGuia): Guia? {
        return try {
            Guia(
                id = legacy.id,
                idCompra = legacy.idCompra,
                tipoLicencia = legacy.tipoLicencia,
                marca = legacy.marca?.takeIf { it.isNotBlank() } ?: "Sin marca",
                modelo = legacy.modelo?.takeIf { it.isNotBlank() } ?: "Sin modelo",
                apodo = legacy.apodo?.takeIf { it.isNotBlank() } ?: "Sin apodo",
                tipoArma = legacy.tipoArma,
                calibre1 = legacy.calibre1?.takeIf { it.isNotBlank() } ?: "N/A",
                calibre2 = legacy.calibre2?.takeIf { it.isNotBlank() },
                numGuia = legacy.numGuia?.takeIf { it.isNotBlank() } ?: "0000",
                numArma = legacy.numArma?.takeIf { it.isNotBlank() } ?: "N/A",
                cupo = legacy.cupo.coerceAtLeast(1),
                gastado = legacy.gastado.coerceAtLeast(0),
                imagePath = legacy.imagePath?.takeIf { it.isNotBlank() },
                syncId = SyncIdGenerator.deterministicSyncId("Guia", legacy.id)
            )
        } catch (e: Exception) {
            Log.e("LegacyConverter", "Error converting Guia ${legacy.id}", e)
            null
        }
    }

    /**
     * Convierte Compra legacy a Room entity. El [Compra.guiaSyncId] se
     * resuelve via [SyncIdGenerator.deterministicSyncId] sobre el
     * [LegacyCompra.idPosGuia] — equivalente a lo que hace
     * MIGRATION_32_33 cuando linka compras a guias por id.
     */
    fun convertCompra(legacy: LegacyCompra): Compra? {
        return try {
            Compra(
                id = legacy.id,
                idPosGuia = legacy.idPosGuia,
                calibre1 = legacy.calibre1?.takeIf { it.isNotBlank() } ?: "N/A",
                calibre2 = legacy.calibre2?.takeIf { it.isNotBlank() },
                unidades = legacy.unidades.coerceAtLeast(1),
                precio = legacy.precio.coerceAtLeast(0.0),
                fecha = legacy.fecha?.takeIf { it.isNotBlank() } ?: "01/01/2000",
                tipo = legacy.tipo?.takeIf { it.isNotBlank() } ?: "N/A",
                peso = legacy.peso.coerceAtLeast(1),
                marca = legacy.marca?.takeIf { it.isNotBlank() } ?: "Sin marca",
                tienda = legacy.tienda?.takeIf { it.isNotBlank() },
                valoracion = legacy.valoracion.coerceIn(0f, 5f),
                imagePath = legacy.imagePath?.takeIf { it.isNotBlank() },
                syncId = SyncIdGenerator.deterministicSyncId("Compra", legacy.id),
                guiaSyncId = SyncIdGenerator.deterministicSyncId("Guia", legacy.idPosGuia)
            )
        } catch (e: Exception) {
            Log.e("LegacyConverter", "Error converting Compra ${legacy.id}", e)
            null
        }
    }

    fun convertLicencia(legacy: LegacyLicencia): Licencia? {
        return try {
            Licencia(
                id = legacy.id,
                tipo = legacy.tipo,
                nombre = legacy.nombre?.takeIf { it.isNotBlank() },
                tipoPermisoConduccion = legacy.tipoPermisoConduccion,
                edad = legacy.edad.coerceAtLeast(18),
                numLicencia = legacy.numLicencia?.takeIf { it.isNotBlank() } ?: "0000",
                fechaExpedicion = legacy.fechaExpedicion?.takeIf { it.isNotBlank() } ?: "01/01/2000",
                fechaCaducidad = legacy.fechaCaducidad?.takeIf { it.isNotBlank() } ?: "01/01/2100",
                numAbonado = legacy.numAbonado,
                numSeguro = legacy.numSeguro?.takeIf { it.isNotBlank() },
                autonomia = legacy.autonomia,
                escala = legacy.escala,
                categoria = legacy.categoria,
                syncId = SyncIdGenerator.deterministicSyncId("Licencia", legacy.id)
            )
        } catch (e: Exception) {
            Log.e("LegacyConverter", "Error converting Licencia ${legacy.id}", e)
            null
        }
    }

    /**
     * Convierte Tirada legacy a Room entity.
     *
     * La Tirada legacy NO tiene id, así que id queda 0 (Room lo auto-genera).
     * Para syncId, generamos un UUID random — significa que si el mismo
     * usuario tiene la app en dos devices, la migración legacy crearía dos
     * syncIds distintos para la misma Tirada. La pérdida es aceptable
     * porque los Tiradas legacy son raros (solo se usan en v3.x) y v2.x
     * no tenía sync de Tiradas (SQLite era local-only).
     */
    fun convertTirada(legacy: LegacyTirada): Tirada? {
        return try {
            val modalidad = legacy.modalidad?.takeIf { it.isNotBlank() }
            val maxPuntuacion = Tirada.getMaxPuntuacion(modalidad)
            Tirada(
                id = 0, // Auto-generated by Room
                descripcion = legacy.descripcion?.takeIf { it.isNotBlank() } ?: "Sin descripción",
                localizacion = legacy.localizacion?.takeIf { it.isNotBlank() },
                modalidad = modalidad,
                fecha = legacy.fecha?.takeIf { it.isNotBlank() } ?: "01/01/2000",
                puntuacion = legacy.puntuacion.coerceIn(0, maxPuntuacion),
                syncId = SyncIdGenerator.newSyncId()
            )
        } catch (e: Exception) {
            Log.e("LegacyConverter", "Error converting Tirada", e)
            null
        }
    }

    /**
     * Convierte lista de Guias legacy a Room entities
     */
    fun convertGuias(legacyList: List<LegacyGuia>): List<Guia> {
        return legacyList.mapNotNull { convertGuia(it) }
    }

    /**
     * Convierte lista de Compras legacy a Room entities
     */
    fun convertCompras(legacyList: List<LegacyCompra>): List<Compra> {
        return legacyList.mapNotNull { convertCompra(it) }
    }

    /**
     * Convierte lista de Licencias legacy a Room entities
     */
    fun convertLicencias(legacyList: List<LegacyLicencia>): List<Licencia> {
        return legacyList.mapNotNull { convertLicencia(it) }
    }

    /**
     * Convierte lista de Tiradas legacy a Room entities
     */
    fun convertTiradas(legacyList: List<LegacyTirada>): List<Tirada> {
        return legacyList.mapNotNull { convertTirada(it) }
    }
}
