package al.ahgitdevelopment.municion.data.local.room.dao

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CompraDao {

    @Query("SELECT * FROM compras WHERE deleted = 0 ORDER BY fecha DESC")
    fun getAllComprasFlow(): Flow<List<Compra>>

    @Query("SELECT * FROM compras WHERE deleted = 0 ORDER BY fecha DESC")
    suspend fun getAllCompras(): List<Compra>

    @Query("SELECT * FROM compras ORDER BY fecha DESC")
    suspend fun getAllComprasIncludingDeleted(): List<Compra>

    @Query("SELECT * FROM compras WHERE id_pos_guia = :guiaId AND deleted = 0 ORDER BY fecha DESC")
    fun getComprasByGuiaFlow(guiaId: Int): Flow<List<Compra>>

    @Query("SELECT * FROM compras WHERE id_pos_guia = :guiaId AND deleted = 0 ORDER BY fecha DESC")
    suspend fun getComprasByGuia(guiaId: Int): List<Compra>

    @Query("SELECT * FROM compras WHERE guia_sync_id = :guiaSyncId AND deleted = 0 ORDER BY fecha DESC")
    suspend fun getComprasByGuiaSyncId(guiaSyncId: String): List<Compra>

    @Query("SELECT * FROM compras WHERE id = :id")
    suspend fun getCompraById(id: Int): Compra?

    @Query("SELECT * FROM compras WHERE sync_id = :syncId")
    suspend fun getCompraBySyncId(syncId: String): Compra?

    @Query("SELECT COUNT(*) FROM compras WHERE id_pos_guia = :guiaId AND deleted = 0")
    suspend fun countComprasByGuia(guiaId: Int): Int

    @Query("SELECT COUNT(*) FROM compras WHERE deleted = 0")
    suspend fun getCount(): Int

    @Query("SELECT SUM(unidades) FROM compras WHERE id_pos_guia = :guiaId AND deleted = 0")
    suspend fun getTotalUnidadesByGuia(guiaId: Int): Int?

    @Query("SELECT sync_id, updated_at, deleted FROM compras")
    suspend fun getAllSyncMetadata(): List<CompraSyncMeta>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(compra: Compra): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(compras: List<Compra>)

    @Update
    suspend fun update(compra: Compra)

    @Delete
    suspend fun delete(compra: Compra)

    @Query("DELETE FROM compras WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM compras")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(compras: List<Compra>) {
        deleteAll()
        insertAll(compras)
    }

    @Query("SELECT * FROM compras WHERE peso = 0")
    suspend fun getComprasWithCorruptedPeso(): List<Compra>

    @Query("SELECT COUNT(*) FROM compras WHERE deleted = 0 AND data_quality != 'ok'")
    fun countNeedsAttentionFlow(): Flow<Int>

    @Query("UPDATE compras SET deleted = 1, deleted_at = :now, updated_at = :now WHERE sync_id = :syncId")
    suspend fun tombstoneBySyncId(syncId: String, now: Long): Int

    @Query("DELETE FROM compras WHERE deleted = 1 AND deleted_at IS NOT NULL AND deleted_at < :before")
    suspend fun purgeTombstonesBefore(before: Long): Int
}

data class CompraSyncMeta(
    @ColumnInfo(name = "sync_id") val syncId: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted") val deleted: Boolean,
)
