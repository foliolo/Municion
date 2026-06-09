package al.ahgitdevelopment.municion.data.sync

import al.ahgitdevelopment.municion.data.local.room.MunicionDatabase
import al.ahgitdevelopment.municion.util.nowMillis
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

/** Daily worker that purges old tombstones and synced outbox rows to keep the DB compact. */
class TombstoneCleanupWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params),
    KoinComponent {
    private val database: MunicionDatabase by inject()

    override suspend fun doWork(): Result {
        val now = nowMillis()
        val tombstoneBefore = now - SyncOutboxConfig.TOMBSTONE_TTL_MS
        database.licenciaDao().purgeTombstonesBefore(tombstoneBefore)
        database.guiaDao().purgeTombstonesBefore(tombstoneBefore)
        database.compraDao().purgeTombstonesBefore(tombstoneBefore)
        database.tiradaDao().purgeTombstonesBefore(tombstoneBefore)
        database.syncOperationDao().purgeSyncedBefore(now - SyncOutboxConfig.OUTBOX_SYNCED_TTL_MS)
        return Result.success()
    }

    companion object {
        private const val PERIODIC_NAME = "tombstone_cleanup_periodic"

        fun enqueuePeriodic(context: Context) {
            val req = PeriodicWorkRequestBuilder<TombstoneCleanupWorker>(1, TimeUnit.DAYS).build()
            WorkManager
                .getInstance(context)
                .enqueueUniquePeriodicWork(PERIODIC_NAME, ExistingPeriodicWorkPolicy.KEEP, req)
        }
    }
}
