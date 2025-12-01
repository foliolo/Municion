package al.ahgitdevelopment.municion.data.local.room.dao

import androidx.room.*
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
     * Observa TODAS las licencias ordenadas por fecha de caducidad
     */
    @Query("SELECT * FROM licencias ORDER BY fecha_caducidad ASC")
    fun getAllLicenciasFlow(): Flow<List<Licencia>>

    /**
     * Obtiene todas las licencias
     */
    @Query("SELECT * FROM licencias ORDER BY fecha_caducidad ASC")
    suspend fun getAllLicencias(): List<Licencia>

    /**
     * Obtiene licencias por tipo
     */
    @Query("SELECT * FROM licencias WHERE tipo = :tipo ORDER BY fecha_caducidad ASC")
    fun getLicenciasByTipoFlow(tipo: Int): Flow<List<Licencia>>

    /**
     * Obtiene una licencia por ID
     */
    @Query("SELECT * FROM licencias WHERE id = :id")
    suspend fun getLicenciaById(id: Int): Licencia?

    /**
     * Obtiene licencia por número.
     *
     * @warning Desde v25, num_licencia NO es único. Este método retornará
     * la primera coincidencia arbitraria. Usar con precaución.
     */
    @Query("SELECT * FROM licencias WHERE num_licencia = :numLicencia")
    suspend fun getLicenciaByNumero(numLicencia: String): Licencia?

    /**
     * Verifica si existe una licencia con ese número
     */
    @Query("SELECT EXISTS(SELECT 1 FROM licencias WHERE num_licencia = :numLicencia)")
    suspend fun existsLicencia(numLicencia: String): Boolean

    /**
     * Cuenta licencias totales
     */
    @Query("SELECT COUNT(*) FROM licencias")
    suspend fun countLicencias(): Int

    /**
     * Cuenta TODAS las licencias (alias para migración)
     */
    @Query("SELECT COUNT(*) FROM licencias")
    suspend fun getCount(): Int

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
}
