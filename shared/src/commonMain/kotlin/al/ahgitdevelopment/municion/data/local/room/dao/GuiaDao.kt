package al.ahgitdevelopment.municion.data.local.room.dao

import al.ahgitdevelopment.municion.data.local.room.entities.Guia
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
interface GuiaDao {

    @Query("SELECT * FROM guias WHERE deleted = 0 ORDER BY apodo ASC")
    fun getAllGuiasFlow(): Flow<List<Guia>>

    @Query("SELECT * FROM guias WHERE deleted = 0 ORDER BY apodo ASC")
    suspend fun getAllGuias(): List<Guia>

    @Query("SELECT * FROM guias ORDER BY apodo ASC")
    suspend fun getAllGuiasIncludingDeleted(): List<Guia>

    @Query("SELECT * FROM guias WHERE tipo_licencia = :tipoLicencia AND deleted = 0 ORDER BY apodo ASC")
    fun getGuiasByTipoLicenciaFlow(tipoLicencia: Int): Flow<List<Guia>>

    /** Includes tombstones for reference resolution from compras pointing at deleted guías. */
    @Query("SELECT * FROM guias WHERE id = :id")
    suspend fun getGuiaById(id: Int): Guia?

    @Query("SELECT * FROM guias WHERE sync_id = :syncId")
    suspend fun getGuiaBySyncId(syncId: String): Guia?

    @Query("SELECT * FROM guias WHERE num_guia = :numGuia AND deleted = 0")
    suspend fun getGuiaByNumero(numGuia: String): Guia?

    @Query("SELECT * FROM guias WHERE gastado >= cupo AND deleted = 0")
    suspend fun getGuiasConCupoAgotado(): List<Guia>

    @Query("SELECT * FROM guias WHERE gastado < cupo AND deleted = 0")
    suspend fun getGuiasConCupoDisponible(): List<Guia>

    @Query("SELECT COUNT(*) FROM guias WHERE tipo_licencia = :tipoLicencia AND deleted = 0")
    suspend fun countGuiasByTipoLicencia(tipoLicencia: Int): Int

    @Query("SELECT COUNT(*) FROM guias WHERE deleted = 0")
    suspend fun getCount(): Int

    @Query("SELECT sync_id, updated_at, deleted FROM guias")
    suspend fun getAllSyncMetadata(): List<GuiaSyncMeta>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(guia: Guia): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(guias: List<Guia>)

    @Update
    suspend fun update(guia: Guia)

    @Query("UPDATE guias SET gastado = :gastado WHERE id = :id")
    suspend fun updateGastado(id: Int, gastado: Int)

    @Query("UPDATE guias SET gastado = gastado + :cantidad WHERE id = :id")
    suspend fun incrementGastado(id: Int, cantidad: Int)

    @Query("UPDATE guias SET gastado = MAX(0, gastado - :cantidad) WHERE id = :id")
    suspend fun decrementGastado(id: Int, cantidad: Int)

    @Query("UPDATE guias SET gastado = 0")
    suspend fun resetAllGastado()

    @Delete
    suspend fun delete(guia: Guia)

    @Query("DELETE FROM guias WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM guias")
    suspend fun deleteAll()

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
    @ColumnInfo(name = "sync_id") val syncId: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted") val deleted: Boolean,
)
