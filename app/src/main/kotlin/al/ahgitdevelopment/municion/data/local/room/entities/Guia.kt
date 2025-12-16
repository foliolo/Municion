package al.ahgitdevelopment.municion.data.local.room.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Data model para Guía de arma (permiso de compra de munición)
 *
 * Una Guía está asociada a una Licencia y tiene un cupo anual de munición.
 *
 * FASE 2: Migración de Java → Kotlin
 *
 * NAVEGACIÓN (v3.3.0+):
 * Esta clase implementa Parcelable + @Serializable para navegación type-safe.
 * Se pasa completa en GuiaForm(guia = ...) eliminando race conditions.
 * GuiaNavType valida cupo vs gastado durante deserialización.
 *
 * @see al.ahgitdevelopment.municion.ui.navigation.navtypes.GuiaNavType
 * @see al.ahgitdevelopment.municion.ui.navigation.GuiaForm
 * @since v3.0.0 (TRACK B Modernization)
 */
@Serializable
@Parcelize
@Entity(
    tableName = "guias",
    indices = [
        Index(value = ["tipo_licencia"]),
        Index(value = ["num_guia"], unique = true)  // Número de guía único
    ]
)
data class Guia(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "id_compra")
    val idCompra: Int = 0,

    @ColumnInfo(name = "tipo_licencia")
    val tipoLicencia: Int,

    @ColumnInfo(name = "marca")
    val marca: String,

    @ColumnInfo(name = "modelo")
    val modelo: String,

    @ColumnInfo(name = "apodo")
    val apodo: String,

    @ColumnInfo(name = "tipo_arma")
    val tipoArma: Int,

    @ColumnInfo(name = "calibre1")
    val calibre1: String,

    @ColumnInfo(name = "calibre2")
    val calibre2: String? = null,

    @ColumnInfo(name = "num_guia")
    val numGuia: String,

    @ColumnInfo(name = "num_arma")
    val numArma: String,

    @ColumnInfo(name = "cupo")
    val cupo: Int,  // Cupo anual de munición permitido

    @ColumnInfo(name = "gastado")
    val gastado: Int = 0,  // Munición ya gastada este año

    @ColumnInfo(name = "image_path")
    val imagePath: String? = null,

    /** URL pública de la foto en Firebase Storage */
    @ColumnInfo(name = "foto_url")
    val fotoUrl: String? = null,

    /** Ruta en Firebase Storage para facilitar el borrado */
    @ColumnInfo(name = "storage_path")
    val storagePath: String? = null
) : Parcelable {

    init {
        require(marca.isNotBlank()) { "Marca cannot be blank" }
        require(modelo.isNotBlank()) { "Modelo cannot be blank" }
        require(apodo.isNotBlank()) { "Apodo cannot be blank" }
        require(calibre1.isNotBlank()) { "Calibre1 cannot be blank" }
        require(numGuia.isNotBlank()) { "NumGuia cannot be blank" }
        require(numArma.isNotBlank()) { "NumArma cannot be blank" }
        require(cupo > 0) { "Cupo must be > 0, got: $cupo" }
        require(gastado >= 0) { "Gastado must be >= 0, got: $gastado" }
    }

    /**
     * Munición restante disponible
     */
    fun disponible(): Int = cupo - gastado

    /**
     * Porcentaje de cupo utilizado (0-100)
     */
    fun porcentajeUsado(): Float {
        return if (cupo > 0) (gastado.toFloat() / cupo.toFloat()) * 100f else 0f
    }

    /**
     * Verifica si el cupo está agotado
     */
    fun cupoAgotado(): Boolean = gastado >= cupo

    /**
     * Verifica si hay suficiente cupo para la cantidad especificada
     */
    fun tieneCupoSuficiente(cantidad: Int): Boolean = disponible() >= cantidad

    /**
     * Crea una copia incrementando el gastado
     */
    fun consumirCupo(cantidad: Int): Guia {
        require(tieneCupoSuficiente(cantidad)) {
            "Cupo insuficiente: disponible=${disponible()}, requested=$cantidad"
        }
        return copy(gastado = gastado + cantidad)
    }

    /**
     * Crea una copia decrementando el gastado (para rollback)
     */
    fun liberarCupo(cantidad: Int): Guia {
        val nuevoGastado = (gastado - cantidad).coerceAtLeast(0)
        return copy(gastado = nuevoGastado)
    }

    /**
     * Verifica si tiene imagen (local o en Storage)
     */
    fun hasImage(): Boolean = !imagePath.isNullOrBlank() || !fotoUrl.isNullOrBlank()

    companion object {
        /**
         * Factory para testing/preview
         */
        fun empty() = Guia(
            tipoLicencia = 0,
            marca = "Glock",
            modelo = "17",
            apodo = "Mi Glock",
            tipoArma = 0,
            calibre1 = "9mm",
            numGuia = "12345",
            numArma = "ABC123",
            cupo = 1000
        )
    }
}
