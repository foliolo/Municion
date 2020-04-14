package al.ahgitdevelopment.municion.repository.dao

import al.ahgitdevelopment.municion.datamodel.Tirada
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TiradaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg tirada: Tirada)

    @Update
    fun update(vararg tirada: Tirada)

    @Query("DELETE FROM $TABLE_TIRADAS")
    fun deleteAll()

    @Query("SELECT * from $TABLE_TIRADAS")
    fun retrieveTiradas(): LiveData<List<Tirada>>

    @Query("SELECT * from $TABLE_TIRADAS WHERE $KEY_ID = :tiradaId")
    fun getTiradaById(tiradaId: Long): LiveData<Tirada>
}