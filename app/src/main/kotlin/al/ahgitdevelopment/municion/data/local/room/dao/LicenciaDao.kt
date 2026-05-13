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
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para Licencia
 *
 * FASE 2.3: DAOs implementation
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@Dao
interface LicenciaDao {

    /**
     * Observa TODAS las licencias ordenadas por fecha de caducidad.
     * Filtra tombstones automáticamente.
     */
    @Query("SELECT * FROM licencias WHERE deleted = 0 ORDER BY fecha_caducidad ASC")
    fun getAllLicenciasFlow(): Flow<List<Licencia>>

    /**
     * Obtiene todas las licencias (excluye tombstones).
     */
    @Query("SELECT * FROM licencias WHERE deleted = 0 ORDER BY fecha_caducidad ASC")
    suspend fun getAllLicencias(): List<Licencia>

    /**
     * Obtiene TODAS las licencias incluyendo tombstones (uso interno del sync).
     */
    @Query("SELECT * FROM licencias ORDER BY fecha_caducidad ASC")
    suspend fun getAllLicenciasIncludingDeleted(): List<Licencia>

    /**
     * Obtiene licencias por tipo
     */
    @Query("SELECT * FROM licencias WHERE tipo = :tipo AND deleted = 0 ORDER BY fecha_caducidad ASC")
    fun getLicenciasByTipoFlow(tipo: Int): Flow<List<Licencia>>

    /**
     * Obtiene una licencia por ID (incluye tombstones porque las
     * compras pueden seguir referenciando licencias borradas).
     */
    @Query("SELECT * FROM licencias WHERE id = :id")
    suspend fun getLicenciaById(id: Int): Licencia?

    /**
     * Obtiene una licencia por syncId (UUID global, todas las copias).
     */
    @Query("SELECT * FROM licencias WHERE sync_id = :syncId")
    suspend fun getLicenciaBySyncId(syncId: String): Licencia?

    /**
     * Obtiene licencia por número.
     *
     * @warning Desde v25, num_licencia NO es único. Este método retornará
     * la primera coincidencia arbitraria. Usar con precaución.
     */
    @Query("SELECT * FROM licencias WHERE num_licencia = :numLicencia AND deleted = 0")
    suspend fun getLicenciaByNumero(numLicencia: String): Licencia?

    /**
     * Verifica si existe una licencia con ese número (excluye tombstones).
     */
    @Query("SELECT EXISTS(SELECT 1 FROM licencias WHERE num_licencia = :numLicencia AND deleted = 0)")
    suspend fun existsLicencia(numLicencia: String): Boolean

    /**
     * Cuenta licencias totales (excluye tombstones).
     */
    @Query("SELECT COUNT(*) FROM licencias WHERE deleted = 0")
    suspend fun countLicencias(): Int

    /**
     * Cuenta TODAS las licencias (alias para migración).
     */
    @Query("SELECT COUNT(*) FROM licencias WHERE deleted = 0")
    suspend fun getCount(): Int

    /**
     * Obtiene sync_id y updated_at de todas las licencias (incluye
     * tombstones para que el sync los propague correctamente).
     */
    @Query("SELECT sync_id, updated_at, deleted FROM licencias")
    suspend fun getAllSyncMetadata(): List<LicenciaSyncMeta>

    /**
     * @deprecated Uses legacy int IDs; retained for the v3.2.x diff sync.
     * The v3.5+ sync path uses [getAllSyncMetadata] instead.
     */
    @Query("SELECT id, updated_at FROM licencias")
    suspend fun getAllTimestamps(): Map<@MapColumn(columnName = "id") Int, @MapColumn(columnName = "updated_at") Long>

    /**
     * Inserta una licencia
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(licencia: Licencia): Long

    /**
     * Inserta múltiples licencias
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(licencias: List<Licencia>)

    /**
     * Actualiza una licencia
     */
    @Update
    suspend fun update(licencia: Licencia)

    /**
     * Elimina una licencia
     */
    @Delete
    suspend fun delete(licencia: Licencia)

    /**
     * Elimina licencia por ID
     */
    @Query("DELETE FROM licencias WHERE id = :id")
    suspend fun deleteById(id: Int)

    /**
     * Elimina TODAS las licencias
     */
    @Query("DELETE FROM licencias")
    suspend fun deleteAll()

    /**
     * Reemplaza TODAS las licencias (para sync con Firebase)
     */
    @Transaction
    suspend fun replaceAll(licencias: List<Licencia>) {
        deleteAll()
        insertAll(licencias)
    }

    /**
     * Marks a licencia as tombstoned. Used by the new sync path so deletes
     * are propagated to remote devices via Firebase. The row remains in
     * the DB until [purgeTombstonesBefore] cleans it up.
     */
    /**
     * Counts non-deleted rows whose data is suspect — i.e. the v3.3 parser
     * resolved them with safe defaults (`dataQuality = "degraded"`) or the
     * legacy stability-bug pattern (`dataQuality = "lost"`). Drives the
     * "X entidades necesitan revisión" banner.
     */
    @Query("SELECT COUNT(*) FROM licencias WHERE deleted = 0 AND data_quality != 'ok'")
    fun countNeedsAttentionFlow(): Flow<Int>

    @Query("UPDATE licencias SET deleted = 1, deleted_at = :now, updated_at = :now WHERE sync_id = :syncId")
    suspend fun tombstoneBySyncId(syncId: String, now: Long): Int

    /**
     * Hard-deletes tombstoned rows older than the given threshold. Called
     * by a periodic worker to keep the local DB compact. The corresponding
     * tombstones in Firebase are cleaned up by the same worker before they
     * disappear locally.
     */
    @Query("DELETE FROM licencias WHERE deleted = 1 AND deleted_at IS NOT NULL AND deleted_at < :before")
    suspend fun purgeTombstonesBefore(before: Long): Int
}

/**
 * Lightweight projection of a licencia for sync comparisons. Avoids loading
 * the full row when all we need is the timestamp + tombstone flag.
 */
data class LicenciaSyncMeta(
    @ColumnInfo(name = "sync_id")
    val syncId: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "deleted")
    val deleted: Boolean
)
