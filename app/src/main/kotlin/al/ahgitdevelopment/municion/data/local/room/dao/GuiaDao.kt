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
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para Guia
 *
 * FASE 2.3: DAOs implementation
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@Dao
interface GuiaDao {

    /**
     * Observa TODAS las guías (filtra tombstones).
     */
    @Query("SELECT * FROM guias WHERE deleted = 0 ORDER BY apodo ASC")
    fun getAllGuiasFlow(): Flow<List<Guia>>

    /**
     * Obtiene todas las guías (filtra tombstones).
     */
    @Query("SELECT * FROM guias WHERE deleted = 0 ORDER BY apodo ASC")
    suspend fun getAllGuias(): List<Guia>

    /**
     * Obtiene TODAS las guías incluyendo tombstones (uso interno del sync).
     */
    @Query("SELECT * FROM guias ORDER BY apodo ASC")
    suspend fun getAllGuiasIncludingDeleted(): List<Guia>

    /**
     * Obtiene guías de un tipo de licencia específico.
     */
    @Query("SELECT * FROM guias WHERE tipo_licencia = :tipoLicencia AND deleted = 0 ORDER BY apodo ASC")
    fun getGuiasByTipoLicenciaFlow(tipoLicencia: Int): Flow<List<Guia>>

    /**
     * Obtiene una guía por ID (incluye tombstones para resolución de
     * referencias desde compras que aún apuntan a guías borradas).
     */
    @Query("SELECT * FROM guias WHERE id = :id")
    suspend fun getGuiaById(id: Int): Guia?

    /**
     * Obtiene una guía por syncId.
     */
    @Query("SELECT * FROM guias WHERE sync_id = :syncId")
    suspend fun getGuiaBySyncId(syncId: String): Guia?

    /**
     * Obtiene guía por número de guía (excluye tombstones).
     */
    @Query("SELECT * FROM guias WHERE num_guia = :numGuia AND deleted = 0")
    suspend fun getGuiaByNumero(numGuia: String): Guia?

    /**
     * Obtiene guías con cupo agotado.
     */
    @Query("SELECT * FROM guias WHERE gastado >= cupo AND deleted = 0")
    suspend fun getGuiasConCupoAgotado(): List<Guia>

    /**
     * Obtiene guías con cupo disponible.
     */
    @Query("SELECT * FROM guias WHERE gastado < cupo AND deleted = 0")
    suspend fun getGuiasConCupoDisponible(): List<Guia>

    /**
     * Cuenta guías de un tipo de licencia.
     */
    @Query("SELECT COUNT(*) FROM guias WHERE tipo_licencia = :tipoLicencia AND deleted = 0")
    suspend fun countGuiasByTipoLicencia(tipoLicencia: Int): Int

    /**
     * Cuenta TODAS las guías (para migración).
     */
    @Query("SELECT COUNT(*) FROM guias WHERE deleted = 0")
    suspend fun getCount(): Int

    @Query("SELECT sync_id, updated_at, deleted FROM guias")
    suspend fun getAllSyncMetadata(): List<GuiaSyncMeta>

    /**
     * @deprecated Used by the legacy diff sync; replaced by [getAllSyncMetadata].
     */
    @Query("SELECT id, updated_at FROM guias")
    suspend fun getAllTimestamps(): Map<@MapColumn(columnName = "id") Int, @MapColumn(columnName = "updated_at") Long>

    /**
     * Inserta una guía
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(guia: Guia): Long

    /**
     * Inserta múltiples guías
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(guias: List<Guia>)

    /**
     * Actualiza una guía
     */
    @Update
    suspend fun update(guia: Guia)

    /**
     * Actualiza solo el campo gastado de una guía
     */
    @Query("UPDATE guias SET gastado = :gastado WHERE id = :id")
    suspend fun updateGastado(id: Int, gastado: Int)

    /**
     * Incrementa el gastado de una guía
     */
    @Query("UPDATE guias SET gastado = gastado + :cantidad WHERE id = :id")
    suspend fun incrementGastado(id: Int, cantidad: Int)

    /**
     * Decrementa el gastado de una guía (para rollback)
     */
    @Query("UPDATE guias SET gastado = MAX(0, gastado - :cantidad) WHERE id = :id")
    suspend fun decrementGastado(id: Int, cantidad: Int)

    /**
     * Resetea el gastado de TODAS las guías (para nuevo año)
     */
    @Query("UPDATE guias SET gastado = 0")
    suspend fun resetAllGastado()

    /**
     * Elimina una guía
     */
    @Delete
    suspend fun delete(guia: Guia)

    /**
     * Elimina guía por ID
     */
    @Query("DELETE FROM guias WHERE id = :id")
    suspend fun deleteById(id: Int)

    /**
     * Elimina TODAS las guías
     */
    @Query("DELETE FROM guias")
    suspend fun deleteAll()

    /**
     * Reemplaza TODAS las guías (para sync con Firebase)
     */
    @Transaction
    suspend fun replaceAll(guias: List<Guia>) {
        deleteAll()
        insertAll(guias)
    }

    @Query("SELECT COUNT(*) FROM guias WHERE deleted = 0 AND data_quality != 'ok'")
    fun countNeedsAttentionFlow(): Flow<Int>

    @Query("UPDATE guias SET deleted = 1, deleted_at = :now, updated_at = :now WHERE sync_id = :syncId")
    suspend fun tombstoneBySyncId(syncId: String, now: Long): Int

    @Query("DELETE FROM guias WHERE deleted = 1 AND deleted_at IS NOT NULL AND deleted_at < :before")
    suspend fun purgeTombstonesBefore(before: Long): Int
}

data class GuiaSyncMeta(
    @ColumnInfo(name = "sync_id")
    val syncId: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "deleted")
    val deleted: Boolean
)
