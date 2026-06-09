package al.ahgitdevelopment.municion.data.local.room.entities

import al.ahgitdevelopment.municion.util.formatPriceEuro
import al.ahgitdevelopment.municion.util.nowMillis
import al.ahgitdevelopment.municion.util.parseDdMmYyyy
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Ammunition purchase linked to a [Guia].
 *
 * Field/column names are preserved from the Android schema for Room migration compatibility.
 */
@Serializable
@Entity(
    tableName = "compras",
    indices = [
        Index(value = ["id_pos_guia"]),
        Index(value = ["fecha"]),
        Index(value = ["sync_id"], unique = true),
        Index(value = ["guia_sync_id"]),
    ],
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
    val calibre2: String? = null,
    @ColumnInfo(name = "unidades")
    val unidades: Int,
    @ColumnInfo(name = "precio")
    val precio: Double,
    @ColumnInfo(name = "fecha")
    val fecha: String,
    @ColumnInfo(name = "tipo")
    val tipo: String,
    /** Bullet weight in grains (changed from TEXT to INTEGER in schema v23). */
    @ColumnInfo(name = "peso")
    val peso: Int,
    @ColumnInfo(name = "marca")
    val marca: String,
    @ColumnInfo(name = "tienda")
    val tienda: String? = null,
    @ColumnInfo(name = "valoracion")
    val valoracion: Float = 0f,
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
    /** Stable global identifier of the parent Guia (cross-device sync key). */
    @ColumnInfo(name = "guia_sync_id")
    val guiaSyncId: String? = null,
    @ColumnInfo(name = "deleted")
    val deleted: Boolean = false,
    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null,
    @ColumnInfo(name = "data_quality")
    val dataQuality: String = "ok",
) {
    fun fechaDate(): LocalDate? = parseDdMmYyyy(fecha)

    /** Price per unit. */
    fun precioUnitario(): Double = if (unidades > 0) precio / unidades else 0.0

    fun hasImage(): Boolean = !fotoUrl.isNullOrBlank() || !imagePath.isNullOrBlank()

    /** Preferred image url (Firebase over legacy local path). */
    fun getImageUrl(): String? = fotoUrl ?: imagePath

    fun formatoPrecio(): String = formatPriceEuro(precio)

    companion object {
        fun empty() =
            Compra(
                idPosGuia = 0,
                calibre1 = "9mm",
                unidades = 50,
                precio = 25.0,
                fecha = "01/01/2024",
                tipo = "FMJ",
                peso = 115,
                marca = "Winchester",
            )
    }
}
