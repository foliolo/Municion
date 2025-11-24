package al.ahgitdevelopment.municion.data.local.room.dao

import androidx.room.*
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
     * Observa TODAS las compras ordenadas por fecha descendente
     * Flow emite automáticamente cuando hay cambios
     */
    @Query("SELECT * FROM compras ORDER BY fecha DESC")
    fun getAllComprasFlow(): Flow<List<Compra>>

    /**
     * Obtiene todas las compras (suspending para uso en coroutines)
     */
    @Query("SELECT * FROM compras ORDER BY fecha DESC")
    suspend fun getAllCompras(): List<Compra>

    /**
     * Observa compras de una guía específica
     */
    @Query("SELECT * FROM compras WHERE id_pos_guia = :guiaId ORDER BY fecha DESC")
    fun getComprasByGuiaFlow(guiaId: Int): Flow<List<Compra>>

    /**
     * Obtiene compras de una guía específica
     */
    @Query("SELECT * FROM compras WHERE id_pos_guia = :guiaId ORDER BY fecha DESC")
    suspend fun getComprasByGuia(guiaId: Int): List<Compra>

    /**
     * Obtiene una compra por ID
     */
    @Query("SELECT * FROM compras WHERE id = :id")
    suspend fun getCompraById(id: Int): Compra?

    /**
     * Cuenta compras de una guía
     */
    @Query("SELECT COUNT(*) FROM compras WHERE id_pos_guia = :guiaId")
    suspend fun countComprasByGuia(guiaId: Int): Int

    /**
     * Suma total de unidades compradas para una guía
     */
    @Query("SELECT SUM(unidades) FROM compras WHERE id_pos_guia = :guiaId")
    suspend fun getTotalUnidadesByGuia(guiaId: Int): Int?

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
}
