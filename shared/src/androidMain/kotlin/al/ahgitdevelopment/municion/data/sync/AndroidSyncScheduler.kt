package al.ahgitdevelopment.municion.data.sync

import android.content.Context

/** Android [SyncScheduler] backed by WorkManager. */
class AndroidSyncScheduler(
    private val context: Context,
) : SyncScheduler {
    override fun start() {
        SyncOutboxWorker.enqueuePeriodic(context)
        TombstoneCleanupWorker.enqueuePeriodic(context)
        // Drain anything pending from a previous session as soon as connectivity allows.
        SyncOutboxWorker.enqueueOneShot(context)
    }

    override fun requestImmediateDrain() {
        SyncOutboxWorker.enqueueOneShot(context)
    }
}
