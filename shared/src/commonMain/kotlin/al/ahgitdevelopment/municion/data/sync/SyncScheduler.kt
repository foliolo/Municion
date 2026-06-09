package al.ahgitdevelopment.municion.data.sync

/**
 * Schedules outbox drains and periodic maintenance. The drain LOGIC lives in commonMain
 * ([SyncOutboxDrainer]); only the scheduling is platform-specific:
 *  - Android: WorkManager (periodic + one-shot + daily tombstone cleanup).
 *  - iOS (v1): foreground/app-launch + on-demand (GitLive offline persistence covers gaps).
 */
interface SyncScheduler {
    /** Sets up periodic drain + daily cleanup and runs startup recovery. Call once at app start. */
    fun start()

    /** Requests an immediate drain after a local write (coalesced). */
    fun requestImmediateDrain()
}

/** No-op scheduler for tests/previews. */
object NoOpSyncScheduler : SyncScheduler {
    override fun start() = Unit

    override fun requestImmediateDrain() = Unit
}
