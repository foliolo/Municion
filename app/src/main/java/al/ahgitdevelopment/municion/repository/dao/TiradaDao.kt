package al.ahgitdevelopment.municion.repository.dao

import al.ahgitdevelopment.municion.datamodel.Tirada
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TiradaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg tirada: Tirada)

    @Update
    fun update(vararg tirada: Tirada)

    @Query("DELETE FROM $TABLE_TIRADAS")
    fun deleteAll()

    @Query("DELETE FROM $TABLE_TIRADAS WHERE $KEY_ID = :id")
    fun delete(id: Long)

    @Query("SELECT * from $TABLE_TIRADAS")
    fun retrieveTiradas(): LiveData<List<Tirada>>

    @Query("SELECT * from $TABLE_TIRADAS WHERE $KEY_ID = :tiradaId")
    fun getTiradaById(tiradaId: Long): LiveData<Tirada>
}