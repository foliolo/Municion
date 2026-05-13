package al.ahgitdevelopment.municion.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Single operation in the outbox.
 *
 * Each save/update/delete on an entity inserts a row here in the same
 * transaction. [al.ahgitdevelopment.municion.data.sync.SyncOutboxWorker]
 * drains rows by writing to Firebase RTDB and updating the row status.
 *
 * The outbox is the ONLY component that talks to Firebase RTDB on the
 * write path. Repositories never call Firebase directly.
 *
 * @since v3.3.0 (Sync redesign)
 */
@Entity(
    tableName = "sync_outbox",
    indices = [
        Index(value = ["status"]),
        Index(value = ["entity_type", "entity_sync_id"]),
        Index(value = ["created_at"])
    ]
)
data class SyncOperation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** "Licencia" | "Guia" | "Compra" | "Tirada" */
    @ColumnInfo(name = "entity_type")
    val entityType: String,

    /** syncId of the target entity */
    @ColumnInfo(name = "entity_sync_id")
    val entitySyncId: String,

    /** [Operation.UPSERT] or [Operation.DELETE] (stored as string for resilience) */
    @ColumnInfo(name = "operation")
    val operation: String,

    /** JSON snapshot of the entity at the time the op was enqueued */
    @ColumnInfo(name = "payload_json")
    val payloadJson: String,

    /** Authenticated user UID at enqueue time; the worker writes under users/{userId}/db/... */
    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_attempt_at")
    val lastAttemptAt: Long? = null,

    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,

    @ColumnInfo(name = "last_error")
    val lastError: String? = null,

    /** [Status.PENDING] | [Status.IN_FLIGHT] | [Status.SYNCED] | [Status.FAILED] */
    @ColumnInfo(name = "status")
    val status: String = Status.PENDING
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
