package al.ahgitdevelopment.municion.data.sync

import al.ahgitdevelopment.municion.data.local.room.dao.SyncOperationDao
import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.local.room.entities.SyncOperation
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around [SyncOperationDao] that:
 *
 *  - Serializes entities to JSON via kotlinx.serialization (entities are
 *    [@Serializable]).
 *  - Enqueues UPSERT or DELETE operations, with UPSERT coalescing so that
 *    a rapid burst of edits to the same entity collapses into a single
 *    outbox row.
 *  - Skips the enqueue entirely when there's no userId (anonymous flow or
 *    pre-login state); writes will be re-emitted from Room when the user
 *    authenticates because the new sync path is idempotent.
 *
 * @since v3.3.0 (Sync redesign)
 */
@Singleton
class SyncOutboxEnqueuer @Inject constructor(
    private val outboxDao: SyncOperationDao
) {
    companion object {
        private const val TAG = "SyncOutboxEnqueuer"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun enqueueUpsert(entity: Licencia, userId: String?) {
        val uid = userId ?: return
        require(entity.syncId.isNotBlank()) { "Licencia.syncId must not be blank when enqueueing" }
        outboxDao.enqueueCoalescing(SyncOperation(
            entityType = "Licencia",
            entitySyncId = entity.syncId,
            operation = SyncOperation.Operation.UPSERT,
            payloadJson = json.encodeToString(entity),
            userId = uid
        ))
        Log.d(TAG, "Enqueued UPSERT Licencia ${entity.syncId}")
    }

    suspend fun enqueueUpsert(entity: Guia, userId: String?) {
        val uid = userId ?: return
        require(entity.syncId.isNotBlank()) { "Guia.syncId must not be blank when enqueueing" }
        outboxDao.enqueueCoalescing(SyncOperation(
            entityType = "Guia",
            entitySyncId = entity.syncId,
            operation = SyncOperation.Operation.UPSERT,
            payloadJson = json.encodeToString(entity),
            userId = uid
        ))
        Log.d(TAG, "Enqueued UPSERT Guia ${entity.syncId}")
    }

    suspend fun enqueueUpsert(entity: Compra, userId: String?) {
        val uid = userId ?: return
        require(entity.syncId.isNotBlank()) { "Compra.syncId must not be blank when enqueueing" }
        outboxDao.enqueueCoalescing(SyncOperation(
            entityType = "Compra",
            entitySyncId = entity.syncId,
            operation = SyncOperation.Operation.UPSERT,
            payloadJson = json.encodeToString(entity),
            userId = uid
        ))
        Log.d(TAG, "Enqueued UPSERT Compra ${entity.syncId}")
    }

    suspend fun enqueueUpsert(entity: Tirada, userId: String?) {
        val uid = userId ?: return
        require(entity.syncId.isNotBlank()) { "Tirada.syncId must not be blank when enqueueing" }
        outboxDao.enqueueCoalescing(SyncOperation(
            entityType = "Tirada",
            entitySyncId = entity.syncId,
            operation = SyncOperation.Operation.UPSERT,
            payloadJson = json.encodeToString(entity),
            userId = uid
        ))
        Log.d(TAG, "Enqueued UPSERT Tirada ${entity.syncId}")
    }

    /**
     * Soft delete: the tombstone is propagated as an UPSERT with deleted=true.
     * Other devices apply it via the normal sync-from-Firebase path.
     *
     * The caller is expected to have already saved the entity locally with
     * deleted=true so Room and the outbox payload agree.
     */
    suspend fun enqueueDelete(entityType: String, syncId: String, payloadJson: String, userId: String?) {
        val uid = userId ?: return
        require(syncId.isNotBlank()) { "syncId must not be blank when enqueueing delete" }
        outboxDao.enqueueCoalescing(SyncOperation(
            entityType = entityType,
            entitySyncId = syncId,
            operation = SyncOperation.Operation.DELETE,
            payloadJson = payloadJson,
            userId = uid
        ))
        Log.d(TAG, "Enqueued DELETE $entityType $syncId")
    }
}
