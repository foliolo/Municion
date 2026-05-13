package al.ahgitdevelopment.municion.data.local.room.dao

import al.ahgitdevelopment.municion.data.local.room.entities.SyncOperation
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * DAO for the sync outbox.
 *
 * @since v3.3.0 (Sync redesign)
 */
@Dao
interface SyncOperationDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun enqueue(operation: SyncOperation): Long

    /**
     * Coalesces consecutive UPSERTs for the same entity: if a pending
     * UPSERT already exists for (entityType, entitySyncId), replaces its
     * payload + timestamp instead of inserting a new row. Cuts the volume
     * of outbox traffic when the user edits the same entity quickly.
     *
     * A DELETE always inserts a new row (and supersedes any prior UPSERTs
     * for the same entity once it's marked SYNCED).
     */
    @Transaction
    suspend fun enqueueCoalescing(operation: SyncOperation): Long {
        if (operation.operation == SyncOperation.Operation.UPSERT) {
            val existing = findPendingUpsert(operation.entityType, operation.entitySyncId)
            if (existing != null) {
                updatePending(
                    id = existing.id,
                    payloadJson = operation.payloadJson,
                    createdAt = operation.createdAt
                )
                return existing.id
            }
        }
        return enqueue(operation)
    }

    @Query("""
        SELECT * FROM sync_outbox
        WHERE entity_type = :entityType
          AND entity_sync_id = :entitySyncId
          AND operation = 'UPSERT'
          AND status = 'PENDING'
        LIMIT 1
    """)
    suspend fun findPendingUpsert(entityType: String, entitySyncId: String): SyncOperation?

    @Query("""
        UPDATE sync_outbox
        SET payload_json = :payloadJson,
            created_at = :createdAt,
            retry_count = 0,
            last_error = NULL
        WHERE id = :id
    """)
    suspend fun updatePending(id: Long, payloadJson: String, createdAt: Long)

    /**
     * Returns the next batch of items to attempt. Skips IN_FLIGHT and
     * exponential-backoff items that haven't reached their next-attempt time.
     */
    @Query("""
        SELECT * FROM sync_outbox
        WHERE status = 'PENDING'
          AND (last_attempt_at IS NULL OR last_attempt_at + :backoffMs <= :now)
        ORDER BY created_at ASC
        LIMIT :limit
    """)
    suspend fun nextBatch(limit: Int, now: Long, backoffMs: Long): List<SyncOperation>

    @Query("""
        UPDATE sync_outbox
        SET status = 'IN_FLIGHT',
            last_attempt_at = :now
        WHERE id = :id AND status = 'PENDING'
    """)
    suspend fun markInFlight(id: Long, now: Long): Int

    @Query("""
        UPDATE sync_outbox
        SET status = 'SYNCED',
            last_attempt_at = :now,
            last_error = NULL
        WHERE id = :id
    """)
    suspend fun markSynced(id: Long, now: Long)

    @Query("""
        UPDATE sync_outbox
        SET status = 'PENDING',
            retry_count = retry_count + 1,
            last_attempt_at = :now,
            last_error = :error
        WHERE id = :id
    """)
    suspend fun markRetry(id: Long, now: Long, error: String?)

    @Query("""
        UPDATE sync_outbox
        SET status = 'FAILED',
            last_attempt_at = :now,
            last_error = :error
        WHERE id = :id
    """)
    suspend fun markFailed(id: Long, now: Long, error: String?)

    /** Sync IDs of entities with pending writes — used to gate the download path. */
    @Query("""
        SELECT entity_sync_id FROM sync_outbox
        WHERE entity_type = :entityType AND status IN ('PENDING', 'IN_FLIGHT')
    """)
    suspend fun pendingSyncIdsFor(entityType: String): List<String>

    @Query("SELECT COUNT(*) FROM sync_outbox WHERE status IN ('PENDING', 'IN_FLIGHT')")
    fun countPendingFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sync_outbox WHERE status = 'FAILED'")
    fun countFailedFlow(): Flow<Int>

    @Query("DELETE FROM sync_outbox WHERE status = 'SYNCED' AND last_attempt_at < :before")
    suspend fun purgeSyncedBefore(before: Long): Int

    @Query("SELECT COUNT(*) FROM sync_outbox")
    suspend fun countAll(): Int

    /**
     * Resets any rows stuck IN_FLIGHT back to PENDING. Called at app startup
     * because a worker that was killed mid-flight cannot recover its row
     * state on its own.
     */
    @Query("""
        UPDATE sync_outbox
        SET status = 'PENDING',
            last_error = 'reset_in_flight_at_startup'
        WHERE status = 'IN_FLIGHT'
    """)
    suspend fun resetInFlight(): Int

    /**
     * Resets all FAILED rows back to PENDING with retryCount=0 so the
     * worker tries them again from scratch. Surfaced as a user-facing
     * "Retry failed syncs" button in Settings — the data is still in
     * Room (we never lose writes there), but Firebase doesn't have it
     * until the worker drains the outbox.
     */
    @Query("""
        UPDATE sync_outbox
        SET status = 'PENDING',
            retry_count = 0,
            last_attempt_at = NULL,
            last_error = NULL
        WHERE status = 'FAILED'
    """)
    suspend fun resetFailedToRetry(): Int
}
