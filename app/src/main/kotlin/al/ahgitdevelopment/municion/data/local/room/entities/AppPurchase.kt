package al.ahgitdevelopment.municion.data.local.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_purchases")
data class AppPurchase(
    @PrimaryKey
    val sku: String,
    val purchaseToken: String,
    val purchaseTime: Long,
    val isAcknowledged: Boolean
)
