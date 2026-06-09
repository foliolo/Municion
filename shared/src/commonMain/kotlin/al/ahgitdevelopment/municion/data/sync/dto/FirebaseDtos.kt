package al.ahgitdevelopment.municion.data.sync.dto

import kotlinx.serialization.Serializable

/**
 * Tolerant Firebase DTOs.
 *
 * Every field is nullable so a partially-corrupt remote record still deserializes (GitLive's
 * typed decoder also bridges platform number types correctly — reading the raw `DataSnapshot.value`
 * as a `Map<String, Any?>` would surface `NSNumber` on iOS and silently drop numeric fields).
 * [stability] captures the legacy "{stability: n}" corruption pattern. Keys mirror the values
 * written by [al.ahgitdevelopment.municion.data.sync.toFirebaseMap]/typed entity serialization.
 */

@Serializable
data class LicenciaDto(
    val id: Int? = null,
    val syncId: String? = null,
    val tipo: Int? = null,
    val nombre: String? = null,
    val tipoPermisoConduccion: Int? = null,
    val edad: Int? = null,
    val fechaExpedicion: String? = null,
    val fechaCaducidad: String? = null,
    val numLicencia: String? = null,
    val numAbonado: Int? = null,
    val numSeguro: String? = null,
    val autonomia: Int? = null,
    val escala: Int? = null,
    val categoria: Int? = null,
    val fotoUrl: String? = null,
    val storagePath: String? = null,
    val updatedAt: Long? = null,
    val deleted: Boolean? = null,
    val deletedAt: Long? = null,
    val dataQuality: String? = null,
    val stability: Int? = null,
)

@Serializable
data class GuiaDto(
    val id: Int? = null,
    val syncId: String? = null,
    val idCompra: Int? = null,
    val tipoLicencia: Int? = null,
    val marca: String? = null,
    val modelo: String? = null,
    val apodo: String? = null,
    val tipoArma: Int? = null,
    val calibre1: String? = null,
    val calibre2: String? = null,
    val numGuia: String? = null,
    val numArma: String? = null,
    val cupo: Int? = null,
    val gastado: Int? = null,
    val imagePath: String? = null,
    val fotoUrl: String? = null,
    val storagePath: String? = null,
    val updatedAt: Long? = null,
    val deleted: Boolean? = null,
    val deletedAt: Long? = null,
    val dataQuality: String? = null,
    val stability: Int? = null,
)

@Serializable
data class CompraDto(
    val id: Int? = null,
    val syncId: String? = null,
    val guiaSyncId: String? = null,
    val idPosGuia: Int? = null,
    val calibre1: String? = null,
    val calibre2: String? = null,
    val unidades: Int? = null,
    val precio: Double? = null,
    val fecha: String? = null,
    val tipo: String? = null,
    val peso: Int? = null,
    val marca: String? = null,
    val tienda: String? = null,
    val valoracion: Double? = null,
    val imagePath: String? = null,
    val fotoUrl: String? = null,
    val storagePath: String? = null,
    val updatedAt: Long? = null,
    val deleted: Boolean? = null,
    val deletedAt: Long? = null,
    val dataQuality: String? = null,
    val stability: Int? = null,
)

@Serializable
data class TiradaDto(
    val id: Int? = null,
    val syncId: String? = null,
    val descripcion: String? = null,
    val rango: String? = null,
    val categoria: String? = null,
    val modalidad: String? = null,
    val fecha: String? = null,
    val puntuacion: Int? = null,
    val updatedAt: Long? = null,
    val deleted: Boolean? = null,
    val deletedAt: Long? = null,
    val dataQuality: String? = null,
    val stability: Int? = null,
)
