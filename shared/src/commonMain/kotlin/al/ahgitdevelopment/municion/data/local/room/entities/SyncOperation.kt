package al.ahgitdevelopment.municion.data.local.room.entities

import al.ahgitdevelopment.municion.util.nowMillis
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Single operation in the write outbox.
 *
 * Each save/update/delete inserts a row here in the same Room transaction. The sync
 * drainer ([al.ahgitdevelopment.municion.data.sync.SyncOutboxDrainer]) writes pending rows
 * to Firebase Realtime Database and updates their status. The outbox is the only component
 * that talks to RTDB on the write path; repositories never call Firebase directly.
 */
@Entity(
    tableName = "sync_outbox",
    indices = [
        Index(value = ["status"]),
        Index(value = ["entity_type", "entity_sync_id"]),
        Index(value = ["created_at"]),
    ],
)
data class SyncOperation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** "Licencia" | "Guia" | "Compra" | "Tirada" */
    @ColumnInfo(name = "entity_type")
    val entityType: String,

    /** syncId of the target entity. */
    @ColumnInfo(name = "entity_sync_id")
    val entitySyncId: String,

    /** [Operation.UPSERT] or [Operation.DELETE]. */
    @ColumnInfo(name = "operation")
    val operation: String,

    /** JSON snapshot of the entity when the op was enqueued. */
    @ColumnInfo(name = "payload_json")
    val payloadJson: String,

    /** Authenticated user UID at enqueue time (writes go under users/{userId}/db/...). */
    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = nowMillis(),

    @ColumnInfo(name = "last_attempt_at")
    val lastAttemptAt: Long? = null,

    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,

    @ColumnInfo(name = "last_error")
    val lastError: String? = null,

    /** [Status.PENDING] | [Status.IN_FLIGHT] | [Status.SYNCED] | [Status.FAILED] */
    @ColumnInfo(name = "status")
    val status: String = Status.PENDING,
) {
    object Operation {
        const val UPSERT = "UPSERT"
        const val DELETE = "DELETE"
    }

    object Status {
        const val PENDING = "PENDING"
        const val IN_FLIGHT = "IN_FLIGHT"
        const val SYNCED = "SYNCED"
        const val FAILED = "FAILED"
    }
}
