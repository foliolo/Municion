package al.ahgitdevelopment.municion.repository.dao

import al.ahgitdevelopment.municion.datamodel.Guia
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface GuiaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg guia: Guia)

    @Update
    fun update(vararg guia: Guia)

    @Query("DELETE FROM $TABLE_GUIAS")
    fun deleteAll()

    @Query("SELECT * from $TABLE_GUIAS")
    fun retrieveGuias(): LiveData<List<Guia>>

    @Query("SELECT * from $TABLE_GUIAS WHERE $KEY_ID = :guiaId")
    fun getGuiaById(guiaId: Long): LiveData<Guia>
}