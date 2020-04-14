package al.ahgitdevelopment.municion.repository.dao

import al.ahgitdevelopment.municion.datamodel.Licencia
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LicenciaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg licencia: Licencia)

    @Update
    fun update(vararg licencia: Licencia)

    @Query("DELETE FROM $TABLE_LICENCIAS")
    fun deleteAll()

    @Query("SELECT * from $TABLE_LICENCIAS")
    fun retrieveLicencias(): LiveData<List<Licencia>>

    @Query("SELECT * from $TABLE_LICENCIAS WHERE $KEY_ID = :licenciaId")
    fun getLicenciaById(licenciaId: Long): LiveData<Licencia>
}