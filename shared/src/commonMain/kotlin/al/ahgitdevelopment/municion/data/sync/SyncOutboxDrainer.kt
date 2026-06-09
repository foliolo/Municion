package al.ahgitdevelopment.municion.data.sync

import al.ahgitdevelopment.municion.data.local.room.dao.SyncOperationDao
import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.local.room.entities.SyncOperation
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import al.ahgitdevelopment.municion.firebase.CrashReporter
import al.ahgitdevelopment.municion.util.nowMillis
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

/**
 * Drains the outbox: writes each PENDING op to Firebase RTDB and updates its status.
 * Platform-agnostic — the [al.ahgitdevelopment.municion.data.sync.SyncScheduler] decides WHEN
 * to call [drainOnce] (WorkManager on Android, foreground/on-demand on iOS).
 */
class SyncOutboxDrainer(
    private val outboxDao: SyncOperationDao,
    private val rtdb: MunicionRtdbDatasource,
    private val crashReporter: CrashReporter,
) {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    data class DrainResult(
        val synced: Int,
        val failed: Int,
        val hasMore: Boolean,
    )

    suspend fun drainOnce(): DrainResult {
        outboxDao.resetInFlight()

        val now = nowMillis()
        val batch =
            outboxDao.nextBatch(SyncOutboxConfig.BATCH_SIZE, now, backoffMs = 0L).filter { row ->
                val nextAttemptAt = (row.lastAttemptAt ?: 0L) + SyncOutboxConfig.computeBackoffMs(row.retryCount)
                row.lastAttemptAt == null || nextAttemptAt <= now
            }
        if (batch.isEmpty()) return DrainResult(0, 0, hasMore = false)

        var synced = 0
        var failed = 0
        for (op in batch) {
            try {
                if (outboxDao.markInFlight(op.id, nowMillis()) == 0) continue // raced
                process(op)
                outboxDao.markSynced(op.id, nowMillis())
                synced++
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                failed++
                if (op.retryCount + 1 >= SyncOutboxConfig.MAX_RETRIES) {
                    crashReporter.log("Outbox op exhausted retries: ${op.entityType}/${op.entitySyncId}")
                    crashReporter.recordException(e)
                    outboxDao.markFailed(op.id, nowMillis(), e.message)
                } else {
                    outboxDao.markRetry(op.id, nowMillis(), e.message)
                }
            }
        }

        val hasMore = failed > 0 || batch.size == SyncOutboxConfig.BATCH_SIZE
        return DrainResult(synced, failed, hasMore)
    }

    private suspend fun process(op: SyncOperation) {
        val uid = op.userId
        // Both UPSERT and DELETE write the entity (deleted=true for tombstones).
        when (op.entityType) {
            "Licencia" -> rtdb.writeLicencia(uid, json.decodeFromString<Licencia>(op.payloadJson))
            "Guia" -> rtdb.writeGuia(uid, json.decodeFromString<Guia>(op.payloadJson))
            "Compra" -> rtdb.writeCompra(uid, json.decodeFromString<Compra>(op.payloadJson))
            "Tirada" -> rtdb.writeTirada(uid, json.decodeFromString<Tirada>(op.payloadJson))
            else -> throw IllegalArgumentException("Unknown entityType: ${op.entityType}")
        }
    }
}
