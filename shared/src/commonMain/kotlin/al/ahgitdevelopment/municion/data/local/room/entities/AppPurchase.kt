package al.ahgitdevelopment.municion.data.local.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Local mirror of an in-app purchase (e.g. remove-ads). RevenueCat remains the source of truth. */
@Entity(tableName = "app_purchases")
data class AppPurchase(
    @PrimaryKey
    val sku: String,
    val purchaseToken: String,
    val purchaseTime: Long,
    val isAcknowledged: Boolean,
)
