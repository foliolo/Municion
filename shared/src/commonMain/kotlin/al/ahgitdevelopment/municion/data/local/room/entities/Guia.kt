package al.ahgitdevelopment.municion.data.local.room.entities

import al.ahgitdevelopment.municion.util.nowMillis
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Firearm permit ("guía") associated with a [Licencia], with an annual ammunition quota (cupo).
 *
 * Field/column names are preserved from the Android schema for Room migration compatibility.
 */
@Serializable
@Entity(
    tableName = "guias",
    indices = [
        Index(value = ["tipo_licencia"]),
        Index(value = ["num_guia"], unique = true),
        Index(value = ["sync_id"], unique = true),
    ],
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

    /** Allowed annual ammunition quota. */
    @ColumnInfo(name = "cupo")
    val cupo: Int,

    /** Ammunition already spent this year. */
    @ColumnInfo(name = "gastado")
    val gastado: Int = 0,

    @ColumnInfo(name = "image_path")
    val imagePath: String? = null,

    @ColumnInfo(name = "foto_url")
    val fotoUrl: String? = null,

    @ColumnInfo(name = "storage_path")
    val storagePath: String? = null,

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
    /** Remaining available ammunition. */
    fun disponible(): Int = cupo - gastado

    /** Percentage of quota used (0-100). */
    fun porcentajeUsado(): Float = if (cupo > 0) (gastado.toFloat() / cupo.toFloat()) * 100f else 0f

    fun cupoAgotado(): Boolean = gastado >= cupo

    fun tieneCupoSuficiente(cantidad: Int): Boolean = disponible() >= cantidad

    /** Returns a copy with [cantidad] consumed from the quota. */
    fun consumirCupo(cantidad: Int): Guia {
        require(tieneCupoSuficiente(cantidad)) {
            "Cupo insuficiente: disponible=${disponible()}, requested=$cantidad"
        }
        return copy(gastado = gastado + cantidad)
    }

    /** Returns a copy with [cantidad] released back to the quota (for rollback). */
    fun liberarCupo(cantidad: Int): Guia = copy(gastado = (gastado - cantidad).coerceAtLeast(0))

    fun hasImage(): Boolean = !imagePath.isNullOrBlank() || !fotoUrl.isNullOrBlank()

    companion object {
        fun empty() = Guia(
            tipoLicencia = 0,
            marca = "Glock",
            modelo = "17",
            apodo = "Mi Glock",
            tipoArma = 0,
            calibre1 = "9mm",
            numGuia = "12345",
            numArma = "ABC123",
            cupo = 1000,
        )
    }
}
