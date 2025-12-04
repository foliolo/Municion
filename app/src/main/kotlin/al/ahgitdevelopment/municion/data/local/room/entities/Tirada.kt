package al.ahgitdevelopment.municion.data.local.room.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Data model para Tirada (ejercicio de tiro/competición)
 *
 * Representa una sesión de tiro con descripción, puntuación y fecha.
 *
 * FASE 2: Migración de Java → Kotlin
 *
 * NAVEGACIÓN (v3.3.0+):
 * Esta clase implementa Parcelable + @Serializable para navegación type-safe.
 * Se pasa completa en TiradaForm(tirada = ...) eliminando race conditions.
 * TiradaNavType valida fechas y puntuación durante deserialización.
 *
 * @see al.ahgitdevelopment.municion.ui.navigation.navtypes.TiradaNavType
 * @see al.ahgitdevelopment.municion.ui.navigation.TiradaForm
 * @since v3.0.0 (TRACK B Modernization)
 */
@Serializable
@Parcelize
@Entity(
    tableName = "tiradas",
    indices = [
        Index(value = ["fecha"])
    ]
)
data class Tirada(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "descripcion")
    val descripcion: String,

    @ColumnInfo(name = "rango")
    val localizacion: String? = null,  // Lugar/galería de tiro (columna "rango" por compatibilidad)

    @ColumnInfo(name = "categoria")
    val categoria: String? = null,  // Categoría: Nacional, Autonómica, Local/Social

    @ColumnInfo(name = "modalidad")
    val modalidad: String? = null,  // Modalidad: Precisión (0-600 pts) o IPSC (0-100%)

    @ColumnInfo(name = "fecha")
    val fecha: String,  // Format: "dd/MM/yyyy"

    @ColumnInfo(name = "puntuacion")
    val puntuacion: Int = 0  // Puntuación: 0-600 (Precisión) o 0-100 (IPSC)
) : Parcelable {

    init {
        require(descripcion.isNotBlank()) { "Descripcion cannot be blank" }
        require(fecha.isNotBlank()) { "Fecha cannot be blank" }
        require(puntuacion >= 0) { "Puntuacion must be >= 0, got: $puntuacion" }
        // Validación de puntuación según modalidad
        val maxPuntuacion = getMaxPuntuacion(modalidad)
        require(puntuacion <= maxPuntuacion) { "Puntuacion must be <= $maxPuntuacion for modalidad $modalidad, got: $puntuacion" }
    }

    /**
     * Parsea la fecha a Date
     */
    fun getFechaDate(): Date? {
        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fecha)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Días desde la tirada
     */
    fun diasDesde(): Long? {
        val fechaTirada = getFechaDate() ?: return null
        val hoy = Calendar.getInstance().time
        val diffMillis = hoy.time - fechaTirada.time
        return diffMillis / (1000 * 60 * 60 * 24)
    }

    /**
     * Verifica si fue en los últimos N días
     */
    fun esReciente(dias: Int = 30): Boolean {
        val diasDesde = diasDesde() ?: return false
        return diasDesde <= dias
    }

    /**
     * Formatea la fecha para display
     */
    fun formatFecha(): String {
        return try {
            val parser = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = parser.parse(fecha)
            date?.let { formatter.format(it) } ?: fecha
        } catch (e: Exception) {
            fecha
        }
    }

    /**
     * Formatea la puntuación para display
     */
    fun formatPuntuacion(): String {
        val suffix = if (modalidad == MODALIDAD_IPSC) "%" else "pts"
        return if (puntuacion > 0) "$puntuacion $suffix" else "0 $suffix"
    }

    /**
     * Verifica si tiene puntuación registrada (mayor a 0)
     */
    fun tienePuntuacion(): Boolean = puntuacion > 0

    /**
     * Verifica si tiene localización/lugar
     */
    fun tieneLocalizacion(): Boolean = !localizacion.isNullOrBlank()

    /**
     * Descripción completa para display
     */
    fun descripcionCompleta(): String {
        val parts = mutableListOf(descripcion)
        if (tieneLocalizacion()) parts.add("en $localizacion")
        if (tienePuntuacion()) parts.add(formatPuntuacion())
        return parts.joinToString(" ")
    }

    companion object {
        const val MODALIDAD_PRECISION = "Precisión"
        const val MODALIDAD_IPSC = "IPSC"

        /**
         * Obtiene la puntuación máxima según la modalidad
         */
        fun getMaxPuntuacion(modalidad: String?): Int {
            return if (modalidad == MODALIDAD_IPSC) 100 else 600
        }

        /**
         * Factory para testing/preview
         */
        fun empty() = Tirada(
            descripcion = "Práctica semanal",
            localizacion = "Galería Municipal",
            modalidad = MODALIDAD_PRECISION,
            fecha = "01/01/2024",
            puntuacion = 85
        )

        /**
         * Calcula milisegundos hasta la fecha de caducidad de una tirada
         * (para countdown timer)
         */
        fun millisUntilExpiracy(tirada: Tirada): Long {
            val fechaTirada = tirada.getFechaDate() ?: return 0L
            val calendar = Calendar.getInstance().apply {
                time = fechaTirada
                // Las tiradas caducan 1 año después
                add(Calendar.YEAR, 1)
            }
            val hoy = Calendar.getInstance()
            return calendar.timeInMillis - hoy.timeInMillis
        }

        /**
         * Verifica si una tirada ha caducado (> 1 año)
         */
        fun estaCaducada(tirada: Tirada): Boolean {
            return millisUntilExpiracy(tirada) <= 0
        }
    }
}
