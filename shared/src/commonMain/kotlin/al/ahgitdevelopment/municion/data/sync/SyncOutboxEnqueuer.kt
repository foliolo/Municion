package al.ahgitdevelopment.municion.data.sync

import al.ahgitdevelopment.municion.data.local.room.dao.SyncOperationDao
import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.local.room.entities.SyncOperation
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Serializes entities to JSON and enqueues UPSERT/DELETE outbox rows (UPSERT coalesced).
 * Skips the enqueue when there's no userId (pre-login); writes are re-emitted from Room on login.
 */
class SyncOutboxEnqueuer(
    private val outboxDao: SyncOperationDao,
) {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    suspend fun enqueueUpsert(
        entity: Licencia,
        userId: String?,
    ) = enqueueUpsert("Licencia", entity.syncId, json.encodeToString(entity), userId)

    suspend fun enqueueUpsert(
        entity: Guia,
        userId: String?,
    ) = enqueueUpsert("Guia", entity.syncId, json.encodeToString(entity), userId)

    suspend fun enqueueUpsert(
        entity: Compra,
        userId: String?,
    ) = enqueueUpsert("Compra", entity.syncId, json.encodeToString(entity), userId)

    suspend fun enqueueUpsert(
        entity: Tirada,
        userId: String?,
    ) = enqueueUpsert("Tirada", entity.syncId, json.encodeToString(entity), userId)

    private suspend fun enqueueUpsert(
        entityType: String,
        syncId: String,
        payloadJson: String,
        userId: String?,
    ) {
        val uid = userId ?: return
        require(syncId.isNotBlank()) { "$entityType.syncId must not be blank when enqueueing" }
        outboxDao.enqueueCoalescing(
            SyncOperation(
                entityType = entityType,
                entitySyncId = syncId,
                operation = SyncOperation.Operation.UPSERT,
                payloadJson = payloadJson,
                userId = uid,
            ),
        )
    }

    /** Soft delete: propagated as an UPSERT with deleted=true (caller already saved deleted=true). */
    suspend fun enqueueDelete(
        entityType: String,
        syncId: String,
        payloadJson: String,
        userId: String?,
    ) {
        val uid = userId ?: return
        require(syncId.isNotBlank()) { "syncId must not be blank when enqueueing delete" }
        outboxDao.enqueueCoalescing(
            SyncOperation(
                entityType = entityType,
                entitySyncId = syncId,
                operation = SyncOperation.Operation.DELETE,
                payloadJson = payloadJson,
                userId = uid,
            ),
        )
    }
}
