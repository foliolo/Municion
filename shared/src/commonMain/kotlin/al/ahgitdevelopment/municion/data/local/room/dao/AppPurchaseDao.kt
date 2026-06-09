package al.ahgitdevelopment.municion.data.local.room.dao

import al.ahgitdevelopment.municion.data.local.room.entities.AppPurchase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppPurchaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(purchase: AppPurchase)

    @Query("SELECT * FROM app_purchases WHERE sku = :sku")
    fun getPurchaseFlow(sku: String): Flow<AppPurchase?>

    @Query("SELECT * FROM app_purchases")
    suspend fun getAllPurchases(): List<AppPurchase>

    @Query("DELETE FROM app_purchases WHERE sku = :sku")
    suspend fun delete(sku: String)
}
