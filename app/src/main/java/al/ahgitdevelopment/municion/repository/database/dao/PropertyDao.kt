package al.ahgitdevelopment.municion.repository.database.dao

import al.ahgitdevelopment.municion.datamodel.Property
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PropertyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg property: Property)

    @Update
    fun update(vararg property: Property)

    @Query("DELETE FROM $TABLE_PROPERTIES")
    fun deleteAll()

    @Query("DELETE FROM $TABLE_PROPERTIES WHERE $KEY_ID = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * from $TABLE_PROPERTIES")
    fun getProperties(): LiveData<List<Property>>

    @Query("SELECT * from $TABLE_PROPERTIES WHERE $KEY_ID = :propertyId")
    fun getPropertyById(propertyId: Long): LiveData<Property>
}
