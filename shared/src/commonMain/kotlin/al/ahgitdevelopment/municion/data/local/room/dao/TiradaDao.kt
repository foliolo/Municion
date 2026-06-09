package al.ahgitdevelopment.municion.data.local.room.dao

import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
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
interface TiradaDao {
    @Query("SELECT * FROM tiradas WHERE deleted = 0 ORDER BY fecha DESC")
    fun getAllTiradasFlow(): Flow<List<Tirada>>

    @Query("SELECT * FROM tiradas WHERE deleted = 0 ORDER BY fecha DESC")
    suspend fun getAllTiradas(): List<Tirada>

    @Query("SELECT * FROM tiradas ORDER BY fecha DESC")
    suspend fun getAllTiradasIncludingDeleted(): List<Tirada>

    @Query("SELECT * FROM tiradas WHERE id = :id")
    suspend fun getTiradaById(id: Int): Tirada?

    @Query("SELECT * FROM tiradas WHERE sync_id = :syncId")
    suspend fun getTiradaBySyncId(syncId: String): Tirada?

    @Query("SELECT * FROM tiradas WHERE puntuacion IS NOT NULL AND deleted = 0 ORDER BY fecha DESC")
    suspend fun getTiradasConPuntuacion(): List<Tirada>

    @Query("SELECT * FROM tiradas WHERE rango = :localizacion AND deleted = 0 ORDER BY fecha DESC")
    suspend fun getTiradasByLocalizacion(localizacion: String): List<Tirada>

    @Query("SELECT COUNT(*) FROM tiradas WHERE deleted = 0")
    suspend fun countTiradas(): Int

    @Query("SELECT COUNT(*) FROM tiradas WHERE deleted = 0")
    suspend fun getCount(): Int

    @Query("SELECT AVG(puntuacion) FROM tiradas WHERE puntuacion IS NOT NULL AND deleted = 0")
    suspend fun getPromedioPuntuacion(): Float?

    @Query("SELECT MAX(puntuacion) FROM tiradas WHERE puntuacion IS NOT NULL AND deleted = 0")
    suspend fun getMejorPuntuacion(): Float?

    @Query("SELECT sync_id, updated_at, deleted FROM tiradas")
    suspend fun getAllSyncMetadata(): List<TiradaSyncMeta>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tirada: Tirada): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tiradas: List<Tirada>)

    @Update
    suspend fun update(tirada: Tirada)

    @Delete
    suspend fun delete(tirada: Tirada)

    @Query("DELETE FROM tiradas WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM tiradas")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(tiradas: List<Tirada>) {
        deleteAll()
        insertAll(tiradas)
    }

    @Query("SELECT COUNT(*) FROM tiradas WHERE deleted = 0 AND data_quality != 'ok'")
    fun countNeedsAttentionFlow(): Flow<Int>

    @Query("UPDATE tiradas SET deleted = 1, deleted_at = :now, updated_at = :now WHERE sync_id = :syncId")
    suspend fun tombstoneBySyncId(
        syncId: String,
        now: Long,
    ): Int

    @Query("DELETE FROM tiradas WHERE deleted = 1 AND deleted_at IS NOT NULL AND deleted_at < :before")
    suspend fun purgeTombstonesBefore(before: Long): Int
}

data class TiradaSyncMeta(
    @ColumnInfo(name = "sync_id") val syncId: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted") val deleted: Boolean,
)
