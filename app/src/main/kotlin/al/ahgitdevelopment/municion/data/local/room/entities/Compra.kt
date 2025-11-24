package al.ahgitdevelopment.municion.data.local.room.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Data model para Compra de munición
 *
 * FASE 2: Migración de Java → Kotlin
 * - Convertido a data class con null safety
 * - Agregadas annotations de Room
 * - Parcelable con @Parcelize
 * - Validación en init block
 * - Inmutabilidad (val instead of var)
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@Parcelize
@Entity(
    tableName = "compras",
    indices = [
        Index(value = ["id_pos_guia"]),  // Índice para queries por guía
        Index(value = ["fecha"])          // Índice para ordenar por fecha
    ]
)
data class Compra(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "id_pos_guia")
    val idPosGuia: Int,

    @ColumnInfo(name = "calibre1")
    val calibre1: String,

    @ColumnInfo(name = "calibre2")
    val calibre2: String? = null,  // Nullable - campo opcional

    @ColumnInfo(name = "unidades")
    val unidades: Int,

    @ColumnInfo(name = "precio")
    val precio: Double,

    @ColumnInfo(name = "fecha")
    val fecha: String,  // Format: "dd/MM/yyyy"

    @ColumnInfo(name = "tipo")
    val tipo: String,

    @ColumnInfo(name = "peso")
    val peso: Int,  // CRITICAL: v23 cambió de TEXT a INTEGER

    @ColumnInfo(name = "marca")
    val marca: String,

    @ColumnInfo(name = "tienda")
    val tienda: String? = null,  // Nullable - campo opcional

    @ColumnInfo(name = "valoracion")
    val valoracion: Float = 0f,

    @ColumnInfo(name = "image_path")
    val imagePath: String? = null  // Nullable - campo opcional
) : Parcelable {

    init {
        // Validación de datos (fail-fast)
        require(unidades > 0) {
            "Unidades must be > 0, got: $unidades"
        }
        require(precio >= 0) {
            "Precio must be >= 0, got: $precio"
        }
        require(peso > 0) {
            "Peso must be > 0, got: $peso"
        }
        require(valoracion in 0f..5f) {
            "Valoracion must be between 0.0 and 5.0, got: $valoracion"
        }
        require(calibre1.isNotBlank()) {
            "Calibre1 cannot be blank"
        }
        require(tipo.isNotBlank()) {
            "Tipo cannot be blank"
        }
        require(marca.isNotBlank()) {
            "Marca cannot be blank"
        }
        require(fecha.isNotBlank()) {
            "Fecha cannot be blank"
        }
    }

    /**
     * Calcula el precio por unidad
     */
    fun precioUnitario(): Double {
        return if (unidades > 0) precio / unidades else 0.0
    }

    /**
     * Verifica si la compra tiene imagen
     */
    fun hasImage(): Boolean = !imagePath.isNullOrBlank()

    /**
     * Formatea el precio para mostrar
     */
    fun formatoPrecio(): String = String.format("%.2f€", precio)

    companion object {
        /**
         * Crea una Compra vacía para preview/testing
         */
        fun empty() = Compra(
            idPosGuia = 0,
            calibre1 = "9mm",
            unidades = 50,
            precio = 25.0,
            fecha = "01/01/2024",
            tipo = "FMJ",
            peso = 115,
            marca = "Winchester"
        )
    }
}
