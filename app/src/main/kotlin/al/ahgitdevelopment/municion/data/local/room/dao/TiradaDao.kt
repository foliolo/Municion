package al.ahgitdevelopment.municion.data.local.room.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para Tirada
 *
 * FASE 2.3: DAOs implementation
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@Dao
interface TiradaDao {

    /**
     * Observa TODAS las tiradas (filtra tombstones).
     */
    @Query("SELECT * FROM tiradas WHERE deleted = 0 ORDER BY fecha DESC")
    fun getAllTiradasFlow(): Flow<List<Tirada>>

    /**
     * Obtiene todas las tiradas (filtra tombstones).
     */
    @Query("SELECT * FROM tiradas WHERE deleted = 0 ORDER BY fecha DESC")
    suspend fun getAllTiradas(): List<Tirada>

    @Query("SELECT * FROM tiradas ORDER BY fecha DESC")
    suspend fun getAllTiradasIncludingDeleted(): List<Tirada>

    /**
     * Obtiene una tirada por ID.
     */
    @Query("SELECT * FROM tiradas WHERE id = :id")
    suspend fun getTiradaById(id: Int): Tirada?

    @Query("SELECT * FROM tiradas WHERE sync_id = :syncId")
    suspend fun getTiradaBySyncId(syncId: String): Tirada?

    /**
     * Obtiene tiradas con puntuación.
     */
    @Query("SELECT * FROM tiradas WHERE puntuacion IS NOT NULL AND deleted = 0 ORDER BY fecha DESC")
    suspend fun getTiradasConPuntuacion(): List<Tirada>

    /**
     * Obtiene tiradas de una localización específica.
     */
    @Query("SELECT * FROM tiradas WHERE rango = :localizacion AND deleted = 0 ORDER BY fecha DESC")
    suspend fun getTiradasByLocalizacion(localizacion: String): List<Tirada>

    /**
     * Cuenta tiradas totales.
     */
    @Query("SELECT COUNT(*) FROM tiradas WHERE deleted = 0")
    suspend fun countTiradas(): Int

    /**
     * Cuenta TODAS las tiradas (alias para migración).
     */
    @Query("SELECT COUNT(*) FROM tiradas WHERE deleted = 0")
    suspend fun getCount(): Int

    /**
     * Calcula puntuación promedio.
     */
    @Query("SELECT AVG(puntuacion) FROM tiradas WHERE puntuacion IS NOT NULL AND deleted = 0")
    suspend fun getPromedioPuntuacion(): Float?

    /**
     * Obtiene mejor puntuación.
     */
    @Query("SELECT MAX(puntuacion) FROM tiradas WHERE puntuacion IS NOT NULL AND deleted = 0")
    suspend fun getMejorPuntuacion(): Float?

    @Query("SELECT sync_id, updated_at, deleted FROM tiradas")
    suspend fun getAllSyncMetadata(): List<TiradaSyncMeta>

    /**
     * @deprecated Used by the legacy diff sync.
     */
    @Query("SELECT id, updated_at FROM tiradas")
    suspend fun getAllTimestamps(): Map<@MapColumn(columnName = "id") Int, @MapColumn(columnName = "updated_at") Long>

    /**
     * Inserta una tirada
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tirada: Tirada): Long

    /**
     * Inserta múltiples tiradas
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tiradas: List<Tirada>)

    /**
     * Actualiza una tirada
     */
    @Update
    suspend fun update(tirada: Tirada)

    /**
     * Elimina una tirada
     */
    @Delete
    suspend fun delete(tirada: Tirada)

    /**
     * Elimina tirada por ID
     */
    @Query("DELETE FROM tiradas WHERE id = :id")
    suspend fun deleteById(id: Int)

    /**
     * Elimina TODAS las tiradas
     */
    @Query("DELETE FROM tiradas")
    suspend fun deleteAll()

    /**
     * Reemplaza TODAS las tiradas (para sync con Firebase)
     */
    @Transaction
    suspend fun replaceAll(tiradas: List<Tirada>) {
        deleteAll()
        insertAll(tiradas)
    }

    @Query("SELECT COUNT(*) FROM tiradas WHERE deleted = 0 AND data_quality != 'ok'")
    fun countNeedsAttentionFlow(): Flow<Int>

    @Query("UPDATE tiradas SET deleted = 1, deleted_at = :now, updated_at = :now WHERE sync_id = :syncId")
    suspend fun tombstoneBySyncId(syncId: String, now: Long): Int

    @Query("DELETE FROM tiradas WHERE deleted = 1 AND deleted_at IS NOT NULL AND deleted_at < :before")
    suspend fun purgeTombstonesBefore(before: Long): Int
}

data class TiradaSyncMeta(
    @ColumnInfo(name = "sync_id")
    val syncId: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "deleted")
    val deleted: Boolean
)
