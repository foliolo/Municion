package al.ahgitdevelopment.municion

import al.ahgitdevelopment.municion.data.sync.SyncIdBackfill
import al.ahgitdevelopment.municion.data.sync.SyncOutboxConfig
import al.ahgitdevelopment.municion.data.sync.SyncOutboxWorker
import al.ahgitdevelopment.municion.data.sync.TombstoneCleanupWorker
import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.BackoffPolicy
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Application class con Hilt.
 *
 * Bootstraps:
 *  - Firebase RTDB disk persistence (must happen BEFORE any reference is taken).
 *  - HiltWorkerFactory for @HiltWorker-annotated workers (the outbox worker).
 *  - MobileAds.
 *  - SyncIdBackfill (one pass; idempotent).
 *  - SyncOutboxWorker periodic schedule.
 *  - TombstoneCleanupWorker periodic schedule.
 *
 * @since v3.0.0 (TRACK B Modernization)
 * @updated v3.3.0 (Sync redesign)
 */
@HiltAndroidApp
class MunicionApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var syncIdBackfill: SyncIdBackfill

    private val appScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Persistence must be enabled BEFORE any FirebaseDatabase reference
        // is obtained. Offline writes are queued and sent on reconnect.
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        appScope.launch {
            try {
                syncIdBackfill.run()
            } catch (e: Exception) {
                Log.e("MunicionApplication", "SyncIdBackfill failed", e)
            }
        }

        scheduleSyncWorkers()

        appScope.launch {
            MobileAds.initialize(this@MunicionApplication) {}
        }
    }

    /**
     * Periodic schedules:
     *
     *  - SyncOutboxWorker every 15 min when CONNECTED to network. Catches
     *    rows that failed earlier and need their backoff window to elapse.
     *
     *  - TombstoneCleanupWorker once a day. Purges synced outbox rows older
     *    than [SyncOutboxConfig.OUTBOX_SYNCED_TTL_MS] and Room tombstones
     *    older than [SyncOutboxConfig.TOMBSTONE_TTL_MS].
     */
    private fun scheduleSyncWorkers() {
        val wm = WorkManager.getInstance(this)

        val networkConstraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val outboxPeriodic = PeriodicWorkRequestBuilder<SyncOutboxWorker>(15, TimeUnit.MINUTES)
            .setConstraints(networkConstraint)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        wm.enqueueUniquePeriodicWork(
            "${SyncOutboxWorker.WORK_NAME}_periodic",
            ExistingPeriodicWorkPolicy.KEEP,
            outboxPeriodic
        )

        val cleanupPeriodic = PeriodicWorkRequestBuilder<TombstoneCleanupWorker>(1, TimeUnit.DAYS)
            .build()
        wm.enqueueUniquePeriodicWork(
            TombstoneCleanupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupPeriodic
        )
    }
}
