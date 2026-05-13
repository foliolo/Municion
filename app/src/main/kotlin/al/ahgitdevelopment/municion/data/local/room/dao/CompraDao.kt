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
import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para Compra
 *
 * FASE 2.3: DAOs implementation
 * - Flow para observar cambios reactivamente
 * - Suspend functions para coroutines
 * - @Transaction para operaciones atómicas
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@Dao
interface CompraDao {

    /**
     * Observa TODAS las compras (filtra tombstones).
     */
    @Query("SELECT * FROM compras WHERE deleted = 0 ORDER BY fecha DESC")
    fun getAllComprasFlow(): Flow<List<Compra>>

    /**
     * Obtiene todas las compras (filtra tombstones).
     */
    @Query("SELECT * FROM compras WHERE deleted = 0 ORDER BY fecha DESC")
    suspend fun getAllCompras(): List<Compra>

    @Query("SELECT * FROM compras ORDER BY fecha DESC")
    suspend fun getAllComprasIncludingDeleted(): List<Compra>

    /**
     * Observa compras de una guía específica.
     */
    @Query("SELECT * FROM compras WHERE id_pos_guia = :guiaId AND deleted = 0 ORDER BY fecha DESC")
    fun getComprasByGuiaFlow(guiaId: Int): Flow<List<Compra>>

    /**
     * Obtiene compras de una guía específica.
     */
    @Query("SELECT * FROM compras WHERE id_pos_guia = :guiaId AND deleted = 0 ORDER BY fecha DESC")
    suspend fun getComprasByGuia(guiaId: Int): List<Compra>

    /**
     * Compras de una guía por syncId.
     */
    @Query("SELECT * FROM compras WHERE guia_sync_id = :guiaSyncId AND deleted = 0 ORDER BY fecha DESC")
    suspend fun getComprasByGuiaSyncId(guiaSyncId: String): List<Compra>

    /**
     * Obtiene una compra por ID (incluye tombstones).
     */
    @Query("SELECT * FROM compras WHERE id = :id")
    suspend fun getCompraById(id: Int): Compra?

    @Query("SELECT * FROM compras WHERE sync_id = :syncId")
    suspend fun getCompraBySyncId(syncId: String): Compra?

    /**
     * Cuenta compras de una guía.
     */
    @Query("SELECT COUNT(*) FROM compras WHERE id_pos_guia = :guiaId AND deleted = 0")
    suspend fun countComprasByGuia(guiaId: Int): Int

    /**
     * Cuenta TODAS las compras.
     */
    @Query("SELECT COUNT(*) FROM compras WHERE deleted = 0")
    suspend fun getCount(): Int

    /**
     * Suma total de unidades compradas para una guía.
     */
    @Query("SELECT SUM(unidades) FROM compras WHERE id_pos_guia = :guiaId AND deleted = 0")
    suspend fun getTotalUnidadesByGuia(guiaId: Int): Int?

    @Query("SELECT sync_id, updated_at, deleted FROM compras")
    suspend fun getAllSyncMetadata(): List<CompraSyncMeta>

    /**
     * @deprecated Used by the legacy diff sync.
     */
    @Query("SELECT id, updated_at FROM compras")
    suspend fun getAllTimestamps(): Map<@MapColumn(columnName = "id") Int, @MapColumn(columnName = "updated_at") Long>

    /**
     * Inserta una compra
     * @return ID de la compra insertada
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(compra: Compra): Long

    /**
     * Inserta múltiples compras
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(compras: List<Compra>)

    /**
     * Actualiza una compra
     */
    @Update
    suspend fun update(compra: Compra)

    /**
     * Elimina una compra
     */
    @Delete
    suspend fun delete(compra: Compra)

    /**
     * Elimina compra por ID
     */
    @Query("DELETE FROM compras WHERE id = :id")
    suspend fun deleteById(id: Int)

    /**
     * Elimina TODAS las compras
     */
    @Query("DELETE FROM compras")
    suspend fun deleteAll()

    /**
     * Reemplaza TODAS las compras (para sync con Firebase)
     * @Transaction garantiza atomicidad
     */
    @Transaction
    suspend fun replaceAll(compras: List<Compra>) {
        deleteAll()
        insertAll(compras)
    }

    /**
     * Obtiene compras con peso corrupto (peso = 0) para diagnóstico
     */
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
    @ColumnInfo(name = "sync_id")
    val syncId: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "deleted")
    val deleted: Boolean
)
