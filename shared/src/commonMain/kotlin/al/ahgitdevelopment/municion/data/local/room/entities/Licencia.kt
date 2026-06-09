package al.ahgitdevelopment.municion.data.local.room.entities

import al.ahgitdevelopment.municion.util.daysFromTodayTo
import al.ahgitdevelopment.municion.util.formatDisplayDate
import al.ahgitdevelopment.municion.util.nowMillis
import al.ahgitdevelopment.municion.util.parseDdMmYyyy
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Firearms license with an expiration date.
 *
 * `@Serializable` is used both for Realtime Database sync payloads and type-safe navigation.
 * (Parcelable was dropped in the KMP migration; navigation passes the entity id and the
 * form loads it from Room — see migration plan phase 5.)
 *
 * Field/column names are preserved from the Android schema for Room migration compatibility.
 */
@Serializable
@Entity(
    tableName = "licencias",
    indices = [
        Index(value = ["num_licencia"]),
        Index(value = ["fecha_caducidad"]),
        Index(value = ["sync_id"], unique = true),
    ],
)
data class Licencia(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "tipo")
    val tipo: Int,

    @ColumnInfo(name = "nombre")
    val nombre: String? = null,

    @ColumnInfo(name = "tipo_permiso_conduccion")
    val tipoPermisoConduccion: Int = -1,

    @ColumnInfo(name = "edad")
    val edad: Int,

    @ColumnInfo(name = "fecha_expedicion")
    val fechaExpedicion: String,

    @ColumnInfo(name = "fecha_caducidad")
    val fechaCaducidad: String,

    @ColumnInfo(name = "num_licencia")
    val numLicencia: String,

    @ColumnInfo(name = "num_abonado")
    val numAbonado: Int = -1,

    @ColumnInfo(name = "num_seguro")
    val numSeguro: String? = null,

    @ColumnInfo(name = "autonomia")
    val autonomia: Int = -1,

    @ColumnInfo(name = "escala")
    val escala: Int = -1,

    @ColumnInfo(name = "categoria")
    val categoria: Int = -1,

    /** Firebase Storage download URL for the license photo. */
    @ColumnInfo(name = "foto_url")
    val fotoUrl: String? = null,

    /** Firebase Storage path (for deletion). */
    @ColumnInfo(name = "storage_path")
    val storagePath: String? = null,

    /** Last-modification timestamp (sync diff). */
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = nowMillis(),

    /** Stable global identifier for cross-device sync (UUID, generated client-side). */
    @ColumnInfo(name = "sync_id")
    val syncId: String = "",

    /** Soft-delete flag. UI queries filter `deleted = 0`. */
    @ColumnInfo(name = "deleted")
    val deleted: Boolean = false,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null,

    /** "ok" | "degraded" | "lost" — set by tolerant sync parsing. */
    @ColumnInfo(name = "data_quality")
    val dataQuality: String = "ok",
) {
    fun fechaCaducidadDate(): LocalDate? = parseDdMmYyyy(fechaCaducidad)

    fun fechaExpedicionDate(): LocalDate? = parseDdMmYyyy(fechaExpedicion)

    /** Days remaining until expiration, or null if the date is invalid. */
    fun diasHastaCaducidad(): Long? = fechaCaducidadDate()?.let { daysFromTodayTo(it) }

    fun estaCaducada(): Boolean = (diasHastaCaducidad() ?: return true) < 0

    fun caducaProxima(): Boolean = (diasHastaCaducidad() ?: return false) in 0..30

    fun estaActiva(): Boolean = !estaCaducada()

    fun estadoDescripcion(): String = when {
        estaCaducada() -> "Caducada"
        caducaProxima() -> "Caduca en ${diasHastaCaducidad() ?: 0} días"
        else -> "Activa"
    }

    /** Description used for calendar expiration events. */
    fun getDescripcionCalendario(): String = "${nombre ?: "Licencia Tipo $tipo"}: $numLicencia"

    companion object {
        fun empty() = Licencia(
            tipo = 0,
            nombre = "Tipo B",
            edad = 30,
            fechaExpedicion = "01/01/2024",
            fechaCaducidad = "01/01/2029",
            numLicencia = "B-12345678",
        )

        fun formatFecha(fecha: String): String = formatDisplayDate(fecha)
    }
}
