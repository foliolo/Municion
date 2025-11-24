package al.ahgitdevelopment.municion.data.local.room.dao

import androidx.room.*
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
     * Observa TODAS las guías
     */
    @Query("SELECT * FROM guias ORDER BY apodo ASC")
    fun getAllGuiasFlow(): Flow<List<Guia>>

    /**
     * Obtiene todas las guías
     */
    @Query("SELECT * FROM guias ORDER BY apodo ASC")
    suspend fun getAllGuias(): List<Guia>

    /**
     * Obtiene guías de un tipo de licencia específico
     */
    @Query("SELECT * FROM guias WHERE tipo_licencia = :tipoLicencia ORDER BY apodo ASC")
    fun getGuiasByTipoLicenciaFlow(tipoLicencia: Int): Flow<List<Guia>>

    /**
     * Obtiene una guía por ID
     */
    @Query("SELECT * FROM guias WHERE id = :id")
    suspend fun getGuiaById(id: Int): Guia?

    /**
     * Obtiene guía por número de guía
     */
    @Query("SELECT * FROM guias WHERE num_guia = :numGuia")
    suspend fun getGuiaByNumero(numGuia: String): Guia?

    /**
     * Obtiene guías con cupo agotado
     */
    @Query("SELECT * FROM guias WHERE gastado >= cupo")
    suspend fun getGuiasConCupoAgotado(): List<Guia>

    /**
     * Obtiene guías con cupo disponible
     */
    @Query("SELECT * FROM guias WHERE gastado < cupo")
    suspend fun getGuiasConCupoDisponible(): List<Guia>

    /**
     * Cuenta guías de un tipo de licencia
     */
    @Query("SELECT COUNT(*) FROM guias WHERE tipo_licencia = :tipoLicencia")
    suspend fun countGuiasByTipoLicencia(tipoLicencia: Int): Int

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
}
