package al.ahgitdevelopment.municion.repository.dao

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

    @Query("DELETE FROM $TABLE_LICENCIAS")
    fun deleteAll()

    @Query("SELECT * from $TABLE_LICENCIAS")
    fun getLicenses(): LiveData<List<License>>

    @Query("SELECT * from $TABLE_LICENCIAS WHERE $KEY_ID = :licenseId")
    fun getLicenseById(licenseId: Long): LiveData<License>
}