package al.ahgitdevelopment.municion.data.local.room.entities

import android.content.Context
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import al.ahgitdevelopment.municion.R
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Data model para Licencia de armas
 *
 * Representa una licencia de tenencia/uso de armas con fecha de caducidad.
 *
 * FASE 2: Migración de Java → Kotlin
 *
 * NAVEGACIÓN (v3.3.0+):
 * Esta clase implementa Parcelable + @Serializable para navegación type-safe.
 * Se pasa completa en LicenciaForm(licencia = ...) eliminando race conditions.
 * LicenciaNavType valida fechas durante deserialización.
 *
 * @see al.ahgitdevelopment.municion.ui.navigation.navtypes.LicenciaNavType
 * @see al.ahgitdevelopment.municion.ui.navigation.LicenciaForm
 * @since v3.0.0 (TRACK B Modernization)
 */
@Serializable
@Parcelize
@Entity(
    tableName = "licencias",
    indices = [
        Index(value = ["num_licencia"]),
        Index(value = ["fecha_caducidad"])
    ]
)
data class Licencia(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "tipo")
    val tipo: Int,  // Tipo de licencia (A, B, C, D, E, F)

    @ColumnInfo(name = "nombre")
    val nombre: String? = null,  // Nombre descriptivo del tipo

    @ColumnInfo(name = "tipo_permiso_conduccion")
    val tipoPermisoConduccion: Int = -1,

    @ColumnInfo(name = "edad")
    val edad: Int,

    @ColumnInfo(name = "fecha_expedicion")
    val fechaExpedicion: String,  // Format: "dd/MM/yyyy"

    @ColumnInfo(name = "fecha_caducidad")
    val fechaCaducidad: String,  // Format: "dd/MM/yyyy"

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
    val categoria: Int = -1
) : Parcelable {

    // NOTA: NO usar init{require()} aquí porque rompe la deserialización JSON
    // durante la navegación type-safe (Navigation Compose + Kotlinx Serialization).
    // Las validaciones se realizan en el formulario antes de guardar.

    /**
     * Parsea la fecha de caducidad a Date
     */
    fun getFechaCaducidadDate(): Date? {
        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechaCaducidad)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parsea la fecha de expedición a Date
     */
    fun getFechaExpedicionDate(): Date? {
        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechaExpedicion)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Días restantes hasta la caducidad
     * @return número de días, o null si la fecha es inválida
     */
    fun diasHastaCaducidad(): Long? {
        val fechaCad = getFechaCaducidadDate() ?: return null
        val hoy = Calendar.getInstance().time
        val diffMillis = fechaCad.time - hoy.time
        return diffMillis / (1000 * 60 * 60 * 24)
    }

    /**
     * Verifica si la licencia está caducada
     */
    fun estaCaducada(): Boolean {
        val dias = diasHastaCaducidad() ?: return true
        return dias < 0
    }

    /**
     * Verifica si la licencia caduca pronto (< 30 días)
     */
    fun caducaProxima(): Boolean {
        val dias = diasHastaCaducidad() ?: return false
        return dias in 0..30
    }

    /**
     * Verifica si la licencia está activa (no caducada)
     */
    fun estaActiva(): Boolean = !estaCaducada()

    /**
     * Descripción del estado de la licencia
     */
    fun estadoDescripcion(): String {
        return when {
            estaCaducada() -> "Caducada"
            caducaProxima() -> {
                val dias = diasHastaCaducidad() ?: 0
                "Caduca en $dias días"
            }
            else -> "Activa"
        }
    }

    /**
     * Obtiene el nombre localizado del tipo de licencia desde recursos
     * (Compatible con código Java original - usado por LicenciaArrayAdapter.java:108)
     *
     * @param context Context para acceder a recursos
     * @return Nombre localizado del tipo de licencia
     */
    fun getNombre(context: Context): String {
        return try {
            context.resources.getTextArray(R.array.tipo_licencias)[tipo].toString()
        } catch (e: Exception) {
            "Licencia Tipo $tipo"
        }
    }

    /**
     * Obtiene la descripción localizada de la escala desde recursos
     * (Compatible con código Java original - usado por LicenciaArrayAdapter.java:144)
     *
     * @param context Context para acceder a recursos
     * @return Descripción localizada de la escala
     */
    fun getStringEscala(context: Context): String {
        return try {
            if (escala >= 0) {
                context.resources.getTextArray(R.array.tipo_escala)[escala].toString()
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Genera descripción para eventos de calendario
     */
    fun getDescripcionCalendario(): String {
        val tipoNombre = nombre ?: "Licencia Tipo $tipo"
        return "$tipoNombre: $numLicencia"
    }

    companion object {
        /**
         * Factory para testing/preview
         */
        fun empty() = Licencia(
            tipo = 0,
            nombre = "Tipo B",
            edad = 30,
            fechaExpedicion = "01/01/2024",
            fechaCaducidad = "01/01/2029",
            numLicencia = "B-12345678"
        )

        /**
         * Formatea fecha para display
         */
        fun formatFecha(fecha: String): String {
            return try {
                val parser = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val date = parser.parse(fecha)
                date?.let { formatter.format(it) } ?: fecha
            } catch (e: Exception) {
                fecha
            }
        }
    }
}
