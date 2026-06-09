package al.ahgitdevelopment.municion.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker that drains the outbox. The drain logic lives in commonMain
 * ([SyncOutboxDrainer]); this is the Android scheduling shell. Uses the default WorkerFactory
 * and resolves its dependency from Koin (KoinComponent) so no custom Configuration is needed.
 */
class SyncOutboxWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params),
    KoinComponent {
    private val drainer: SyncOutboxDrainer by inject()

    override suspend fun doWork(): Result {
        val result = drainer.drainOnce()
        if (result.hasMore) enqueueOneShot(applicationContext)
        return if (result.failed == 0) Result.success() else Result.retry()
    }

    companion object {
        private const val ONESHOT_NAME = "sync_outbox_oneshot"
        private const val PERIODIC_NAME = "sync_outbox_periodic"

        private val connected =
            Constraints
                .Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        fun enqueueOneShot(context: Context) {
            val req =
                OneTimeWorkRequestBuilder<SyncOutboxWorker>()
                    .setConstraints(connected)
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                    .build()
            WorkManager.getInstance(context).enqueueUniqueWork(ONESHOT_NAME, ExistingWorkPolicy.KEEP, req)
        }

        fun enqueuePeriodic(context: Context) {
            val req =
                PeriodicWorkRequestBuilder<SyncOutboxWorker>(15, TimeUnit.MINUTES)
                    .setConstraints(connected)
                    .build()
            WorkManager
                .getInstance(context)
                .enqueueUniquePeriodicWork(PERIODIC_NAME, ExistingPeriodicWorkPolicy.KEEP, req)
        }
    }
}
