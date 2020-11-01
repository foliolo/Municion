package al.ahgitdevelopment.municion.repository.database.dao

import al.ahgitdevelopment.municion.datamodel.Purchase
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PurchaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg purchase: Purchase)

    @Update
    fun update(vararg purchase: Purchase)

    @Query("DELETE FROM $TABLE_PURCHASES")
    fun deleteAll()

    @Query("DELETE FROM $TABLE_PURCHASES WHERE $KEY_ID = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * from $TABLE_PURCHASES")
    fun getPurchases(): LiveData<List<Purchase>>

    @Query("SELECT * from $TABLE_PURCHASES WHERE $KEY_ID = :purchaseId")
    fun getPurchaseById(purchaseId: Long): LiveData<Purchase>
}
