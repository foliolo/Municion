package al.ahgitdevelopment.municion.data.local.room.entities

import al.ahgitdevelopment.municion.util.daysSince
import al.ahgitdevelopment.municion.util.formatDisplayDate
import al.ahgitdevelopment.municion.util.nowMillis
import al.ahgitdevelopment.municion.util.parseDdMmYyyy
import al.ahgitdevelopment.municion.util.today
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable

/**
 * Shooting session / competition record.
 *
 * Field/column names are preserved from the Android schema for Room migration compatibility
 * (note the `rango` column maps to [localizacion]).
 */
@Serializable
@Entity(
    tableName = "tiradas",
    indices = [
        Index(value = ["fecha"]),
        Index(value = ["sync_id"], unique = true),
    ],
)
data class Tirada(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "descripcion")
    val descripcion: String,

    /** Shooting range/location (legacy column name "rango"). */
    @ColumnInfo(name = "rango")
    val localizacion: String? = null,

    /** Category: Nacional, Autonómica, Local/Social. */
    @ColumnInfo(name = "categoria")
    val categoria: String? = null,

    /** Modality: Precisión (0-600 pts) or IPSC (0-100%). */
    @ColumnInfo(name = "modalidad")
    val modalidad: String? = null,

    @ColumnInfo(name = "fecha")
    val fecha: String,

    /** Score: 0-600 (Precisión) or 0-100 (IPSC). */
    @ColumnInfo(name = "puntuacion")
    val puntuacion: Int = 0,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = nowMillis(),

    @ColumnInfo(name = "sync_id")
    val syncId: String = "",

    @ColumnInfo(name = "deleted")
    val deleted: Boolean = false,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null,

    @ColumnInfo(name = "data_quality")
    val dataQuality: String = "ok",
) {
    fun fechaDate(): LocalDate? = parseDdMmYyyy(fecha)

    /** Days elapsed since the session, or null if the date is invalid. */
    fun diasDesde(): Long? = fechaDate()?.let { daysSince(it) }

    fun esReciente(dias: Int = 30): Boolean = (diasDesde() ?: return false) <= dias

    fun formatFecha(): String = formatDisplayDate(fecha)

    fun formatPuntuacion(): String {
        val suffix = if (modalidad == MODALIDAD_IPSC) "%" else "pts"
        return "$puntuacion $suffix"
    }

    fun tienePuntuacion(): Boolean = puntuacion > 0

    fun tieneLocalizacion(): Boolean = !localizacion.isNullOrBlank()

    fun descripcionCompleta(): String {
        val parts = mutableListOf(descripcion)
        if (tieneLocalizacion()) parts.add("en $localizacion")
        if (tienePuntuacion()) parts.add(formatPuntuacion())
        return parts.joinToString(" ")
    }

    companion object {
        const val MODALIDAD_PRECISION = "Precisión"
        const val MODALIDAD_IPSC = "IPSC"

        fun getMaxPuntuacion(modalidad: String?): Int = if (modalidad == MODALIDAD_IPSC) 100 else 600

        fun empty() = Tirada(
            descripcion = "Práctica semanal",
            localizacion = "Galería Municipal",
            modalidad = MODALIDAD_PRECISION,
            fecha = "01/01/2024",
            puntuacion = 85,
        )

        /** Milliseconds until the session "expires" (1 year after its date); 0 if invalid. */
        fun millisUntilExpiracy(tirada: Tirada): Long {
            val date = parseDdMmYyyy(tirada.fecha) ?: return 0L
            val expiry = date.plus(1, DateTimeUnit.YEAR)
            val days = expiry.toEpochDays() - today().toEpochDays()
            return days * MILLIS_PER_DAY
        }

        fun estaCaducada(tirada: Tirada): Boolean = millisUntilExpiracy(tirada) <= 0L

        private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
    }
}
