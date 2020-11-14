package al.ahgitdevelopment.municion.repository.database.dao

import al.ahgitdevelopment.municion.datamodel.Purchase
import al.ahgitdevelopment.municion.repository.database.KEY_ID
import al.ahgitdevelopment.municion.repository.database.TABLE_PURCHASES
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg purchase: Purchase)

    @Update
    suspend fun update(vararg purchase: Purchase)

    @Query("DELETE FROM $TABLE_PURCHASES")
    suspend fun deleteAll()

    @Query("DELETE FROM $TABLE_PURCHASES WHERE $KEY_ID = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * from $TABLE_PURCHASES")
    fun getPurchases(): Flow<List<Purchase>>

    @Query("SELECT * from $TABLE_PURCHASES WHERE $KEY_ID = :purchaseId")
    fun getPurchaseById(purchaseId: Long): Purchase
}
