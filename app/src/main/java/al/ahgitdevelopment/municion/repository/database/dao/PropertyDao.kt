package al.ahgitdevelopment.municion.repository.database.dao

import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.repository.database.KEY_ID
import al.ahgitdevelopment.municion.repository.database.KEY_PROPERTY_IMAGE
import al.ahgitdevelopment.municion.repository.database.TABLE_PROPERTIES
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg property: Property)

    @Update
    suspend fun update(vararg property: Property)

    @Query("DELETE FROM $TABLE_PROPERTIES")
    suspend fun deleteAll()

    @Query("DELETE FROM $TABLE_PROPERTIES WHERE $KEY_ID = :id")
    suspend fun delete(id: String)

    @Query("SELECT * from $TABLE_PROPERTIES")
    fun getProperties(): Flow<List<Property>>

    @Query("SELECT * from $TABLE_PROPERTIES WHERE $KEY_ID = :propertyId")
    fun getPropertyById(propertyId: Long): Property

    @Query("UPDATE $TABLE_PROPERTIES SET $KEY_PROPERTY_IMAGE=:imageUrl WHERE $KEY_ID=:itemId")
    fun savePropertyImageItem(itemId: String, imageUrl: String)
}
