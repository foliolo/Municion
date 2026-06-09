package al.ahgitdevelopment.municion.data.local.room.dao

import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
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
interface LicenciaDao {
    @Query("SELECT * FROM licencias WHERE deleted = 0 ORDER BY fecha_caducidad ASC")
    fun getAllLicenciasFlow(): Flow<List<Licencia>>

    @Query("SELECT * FROM licencias WHERE deleted = 0 ORDER BY fecha_caducidad ASC")
    suspend fun getAllLicencias(): List<Licencia>

    @Query("SELECT * FROM licencias ORDER BY fecha_caducidad ASC")
    suspend fun getAllLicenciasIncludingDeleted(): List<Licencia>

    @Query("SELECT * FROM licencias WHERE tipo = :tipo AND deleted = 0 ORDER BY fecha_caducidad ASC")
    fun getLicenciasByTipoFlow(tipo: Int): Flow<List<Licencia>>

    /** Includes tombstones: compras may still reference deleted licencias. */
    @Query("SELECT * FROM licencias WHERE id = :id")
    suspend fun getLicenciaById(id: Int): Licencia?

    @Query("SELECT * FROM licencias WHERE sync_id = :syncId")
    suspend fun getLicenciaBySyncId(syncId: String): Licencia?

    @Query("SELECT * FROM licencias WHERE num_licencia = :numLicencia AND deleted = 0")
    suspend fun getLicenciaByNumero(numLicencia: String): Licencia?

    @Query("SELECT EXISTS(SELECT 1 FROM licencias WHERE num_licencia = :numLicencia AND deleted = 0)")
    suspend fun existsLicencia(numLicencia: String): Boolean

    @Query("SELECT COUNT(*) FROM licencias WHERE deleted = 0")
    suspend fun countLicencias(): Int

    @Query("SELECT COUNT(*) FROM licencias WHERE deleted = 0")
    suspend fun getCount(): Int

    @Query("SELECT sync_id, updated_at, deleted FROM licencias")
    suspend fun getAllSyncMetadata(): List<LicenciaSyncMeta>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(licencia: Licencia): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(licencias: List<Licencia>)

    @Update
    suspend fun update(licencia: Licencia)

    @Delete
    suspend fun delete(licencia: Licencia)

    @Query("DELETE FROM licencias WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM licencias")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(licencias: List<Licencia>) {
        deleteAll()
        insertAll(licencias)
    }

    /** Non-deleted rows whose data was resolved with defaults (drives the "needs review" banner). */
    @Query("SELECT COUNT(*) FROM licencias WHERE deleted = 0 AND data_quality != 'ok'")
    fun countNeedsAttentionFlow(): Flow<Int>

    @Query("UPDATE licencias SET deleted = 1, deleted_at = :now, updated_at = :now WHERE sync_id = :syncId")
    suspend fun tombstoneBySyncId(
        syncId: String,
        now: Long,
    ): Int

    @Query("DELETE FROM licencias WHERE deleted = 1 AND deleted_at IS NOT NULL AND deleted_at < :before")
    suspend fun purgeTombstonesBefore(before: Long): Int
}

/** Lightweight projection for sync comparisons (timestamp + tombstone flag). */
data class LicenciaSyncMeta(
    @ColumnInfo(name = "sync_id") val syncId: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted") val deleted: Boolean,
)
