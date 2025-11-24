package al.ahgitdevelopment.municion.data.local.room.dao

import androidx.room.*
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
     * Observa TODAS las tiradas ordenadas por fecha descendente
     */
    @Query("SELECT * FROM tiradas ORDER BY fecha DESC")
    fun getAllTiradasFlow(): Flow<List<Tirada>>

    /**
     * Obtiene todas las tiradas
     */
    @Query("SELECT * FROM tiradas ORDER BY fecha DESC")
    suspend fun getAllTiradas(): List<Tirada>

    /**
     * Obtiene una tirada por ID
     */
    @Query("SELECT * FROM tiradas WHERE id = :id")
    suspend fun getTiradaById(id: Int): Tirada?

    /**
     * Obtiene tiradas con puntuación
     */
    @Query("SELECT * FROM tiradas WHERE puntuacion IS NOT NULL ORDER BY fecha DESC")
    suspend fun getTiradasConPuntuacion(): List<Tirada>

    /**
     * Obtiene tiradas de un rango específico
     */
    @Query("SELECT * FROM tiradas WHERE rango = :rango ORDER BY fecha DESC")
    suspend fun getTiradasByRango(rango: String): List<Tirada>

    /**
     * Cuenta tiradas totales
     */
    @Query("SELECT COUNT(*) FROM tiradas")
    suspend fun countTiradas(): Int

    /**
     * Calcula puntuación promedio
     */
    @Query("SELECT AVG(puntuacion) FROM tiradas WHERE puntuacion IS NOT NULL")
    suspend fun getPromedioPuntuacion(): Float?

    /**
     * Obtiene mejor puntuación
     */
    @Query("SELECT MAX(puntuacion) FROM tiradas WHERE puntuacion IS NOT NULL")
    suspend fun getMejorPuntuacion(): Float?

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
}
