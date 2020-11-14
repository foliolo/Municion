package al.ahgitdevelopment.municion.repository.database.dao

import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.repository.database.KEY_ID
import al.ahgitdevelopment.municion.repository.database.TABLE_LICENSES
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LicenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg license: License)

    @Update
    suspend fun update(vararg license: License)

    @Query("DELETE FROM $TABLE_LICENSES")
    suspend fun deleteAll()

    @Query("DELETE FROM $TABLE_LICENSES WHERE $KEY_ID = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * from $TABLE_LICENSES")
    fun getLicenses(): Flow<List<License>>

    @Query("SELECT * from $TABLE_LICENSES WHERE $KEY_ID = :licenseId")
    fun getLicenseById(licenseId: Long): Flow<License>
}
