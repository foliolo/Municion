package al.ahgitdevelopment.municion.data.sync

import al.ahgitdevelopment.municion.data.local.room.dao.SyncOperationDao
import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.local.room.entities.SyncOperation
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DatabaseReference
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

/**
 * Drains the [sync_outbox] by writing each PENDING operation to Firebase RTDB.
 *
 * Algorithm per pass:
 *
 *  1. Reset rows stuck IN_FLIGHT (from a previous worker kill).
 *  2. Fetch up to [SyncOutboxConfig.BATCH_SIZE] PENDING rows whose backoff
 *     window has elapsed.
 *  3. For each row: mark IN_FLIGHT, deserialize payload to the proper
 *     entity, write to `users/{userId}/db/{path}/{syncId}` via
 *     [DatabaseReference.setValue]. Mark SYNCED on success, retry-or-fail
 *     on exception.
 *
 * The worker is enqueued with backoff so WorkManager itself retries the
 * worker if the network is unreachable.
 *
 * @since v3.3.0 (Sync redesign)
 */
@HiltWorker
class SyncOutboxWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val outboxDao: SyncOperationDao,
    private val firebaseDb: DatabaseReference,
    private val crashlytics: FirebaseCrashlytics
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "sync_outbox"
        private const val TAG = "SyncOutboxWorker"

        /**
         * Enqueues a one-shot drain attempt. Coalesces with any pending
         * scheduled run (KEEP policy) so a burst of saves doesn't pile up
         * worker instances.
         */
        fun enqueueOneShot(context: Context) {
            val req = OneTimeWorkRequestBuilder<SyncOutboxWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.KEEP,
                req
            )
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun doWork(): Result {
        Log.i(TAG, "Drain pass starting")
        val resetCount = outboxDao.resetInFlight()
        if (resetCount > 0) {
            Log.w(TAG, "Reset $resetCount stuck IN_FLIGHT rows")
        }

        var successCount = 0
        var failureCount = 0

        // Each pass tries up to BATCH_SIZE rows. We don't loop indefinitely
        // inside one worker call — WorkManager schedules the next pass
        // after backoff if there's still work to do.
        val now = System.currentTimeMillis()
        val batch = outboxDao.nextBatch(
            limit = SyncOutboxConfig.BATCH_SIZE,
            now = now,
            backoffMs = 0L  // backoff is per-row; we apply it in the loop below
        ).filter { row ->
            val nextAttemptAt = (row.lastAttemptAt ?: 0L) + SyncOutboxConfig.computeBackoffMs(row.retryCount)
            row.lastAttemptAt == null || nextAttemptAt <= now
        }

        if (batch.isEmpty()) {
            Log.d(TAG, "Nothing to drain")
            return Result.success()
        }

        Log.i(TAG, "Draining ${batch.size} outbox operations")
        for (op in batch) {
            try {
                val marked = outboxDao.markInFlight(op.id, System.currentTimeMillis())
                if (marked == 0) {
                    // Another worker (or a startup reset) raced us.
                    continue
                }

                processOperation(op)

                outboxDao.markSynced(op.id, System.currentTimeMillis())
                successCount++
            } catch (e: Exception) {
                Log.w(TAG, "Op ${op.id} failed: ${e.message}")
                failureCount++
                val newRetry = op.retryCount + 1
                if (newRetry >= SyncOutboxConfig.MAX_RETRIES) {
                    crashlytics.log("Outbox op exhausted retries: $op")
                    crashlytics.recordException(e)
                    outboxDao.markFailed(op.id, System.currentTimeMillis(), e.message)
                } else {
                    outboxDao.markRetry(op.id, System.currentTimeMillis(), e.message)
                }
            }
        }

        Log.i(TAG, "Drain pass done: $successCount synced, $failureCount failed")

        // If there are still pending rows after this pass, request another
        // drain. WorkManager's KEEP policy coalesces.
        if (failureCount > 0 || batch.size == SyncOutboxConfig.BATCH_SIZE) {
            enqueueOneShot(applicationContext)
        }

        return if (failureCount == 0) Result.success() else Result.retry()
    }

    private suspend fun processOperation(op: SyncOperation) {
        // Both UPSERT and DELETE write the entity (with deleted=true for
        // tombstones) to Firebase. We never use removeValue here so other
        // devices see the tombstone instead of "entity missing".
        val firebaseMap = decodeAndConvertToMap(op)
        firebaseDb
            .child("users").child(op.userId)
            .child("db")
            .child(SyncOutboxConfig.firebasePathFor(op.entityType))
            .child(op.entitySyncId)
            .setValue(firebaseMap)
            .await()
    }

    private fun decodeAndConvertToMap(op: SyncOperation): Map<String, Any?> {
        return when (op.entityType) {
            "Licencia" -> json.decodeFromString<Licencia>(op.payloadJson).toFirebaseMap()
            "Guia" -> json.decodeFromString<Guia>(op.payloadJson).toFirebaseMap()
            "Compra" -> json.decodeFromString<Compra>(op.payloadJson).toFirebaseMap()
            "Tirada" -> json.decodeFromString<Tirada>(op.payloadJson).toFirebaseMap()
            else -> throw IllegalArgumentException("Unknown entityType: ${op.entityType}")
        }
    }
}
