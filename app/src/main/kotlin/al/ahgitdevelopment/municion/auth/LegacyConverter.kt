package al.ahgitdevelopment.municion.auth

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
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
     * Convierte Guia legacy a Room entity
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
                imagePath = legacy.imagePath?.takeIf { it.isNotBlank() }
            )
        } catch (e: Exception) {
            Log.e("LegacyConverter", "Error converting Guia ${legacy.id}", e)
            null
        }
    }

    /**
     * Convierte Compra legacy a Room entity
     * Nota: La Compra legacy usa getCalibre1() para el calibre principal
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
                imagePath = legacy.imagePath?.takeIf { it.isNotBlank() }
            )
        } catch (e: Exception) {
            Log.e("LegacyConverter", "Error converting Compra ${legacy.id}", e)
            null
        }
    }

    /**
     * Convierte Licencia legacy a Room entity
     * Nota: La Licencia Room no tiene numDni ni ccaa, usa numSeguro y autonomia
     */
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
                categoria = legacy.categoria
            )
        } catch (e: Exception) {
            Log.e("LegacyConverter", "Error converting Licencia ${legacy.id}", e)
            null
        }
    }

    /**
     * Convierte Tirada legacy a Room entity
     * Nota: La Tirada legacy no tiene campo ID
     */
    fun convertTirada(legacy: LegacyTirada): Tirada? {
        return try {
            Tirada(
                id = 0, // Auto-generated by Room
                descripcion = legacy.descripcion?.takeIf { it.isNotBlank() } ?: "Sin descripción",
                localizacion = legacy.localizacion?.takeIf { it.isNotBlank() },
                fecha = legacy.fecha?.takeIf { it.isNotBlank() } ?: "01/01/2000",
                puntuacion = legacy.puntuacion.coerceIn(0, 600)
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
