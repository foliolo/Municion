package al.ahgitdevelopment.municion.repository.database.dao

import al.ahgitdevelopment.municion.datamodel.License
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface LicenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg license: License)

    @Update
    fun update(vararg license: License)

    @Query("DELETE FROM $TABLE_LICENSES")
    fun deleteAll()

    @Query("DELETE FROM $TABLE_LICENSES WHERE $KEY_ID = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * from $TABLE_LICENSES")
    fun getLicenses(): LiveData<List<License>>

    @Query("SELECT * from $TABLE_LICENSES WHERE $KEY_ID = :licenseId")
    fun getLicenseById(licenseId: Long): LiveData<License>
}
