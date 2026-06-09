package al.ahgitdevelopment.municion.data.sync

import al.ahgitdevelopment.municion.data.local.room.MunicionDatabase
import al.ahgitdevelopment.municion.util.nowMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIApplicationWillEnterForegroundNotification

/**
 * iOS [SyncScheduler] (v1): drains the outbox on app launch, when the app returns to the
 * foreground, and on demand after a local write. GitLive RTDB offline persistence means a
 * missed window is not data loss — the outbox simply drains next foreground. Tombstone cleanup
 * runs opportunistically (throttled to once/day). BGTaskScheduler can be added later.
 */
class IosSyncScheduler(
    private val drainer: SyncOutboxDrainer,
    private val database: MunicionDatabase,
) : SyncScheduler {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var lastCleanupAt = 0L

    override fun start() {
        drainNow()
        NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationWillEnterForegroundNotification,
            `object` = null,
            queue = null,
        ) { _ ->
            drainNow()
            maybeCleanup()
        }
    }

    override fun requestImmediateDrain() = drainNow()

    private fun drainNow() {
        scope.launch { runCatching { drainer.drainOnce() } }
    }

    private fun maybeCleanup() {
        scope.launch {
            val now = nowMillis()
            if (now - lastCleanupAt < ONE_DAY_MS) return@launch
            lastCleanupAt = now
            runCatching {
                val tombstoneBefore = now - SyncOutboxConfig.TOMBSTONE_TTL_MS
                database.licenciaDao().purgeTombstonesBefore(tombstoneBefore)
                database.guiaDao().purgeTombstonesBefore(tombstoneBefore)
                database.compraDao().purgeTombstonesBefore(tombstoneBefore)
                database.tiradaDao().purgeTombstonesBefore(tombstoneBefore)
                database.syncOperationDao().purgeSyncedBefore(now - SyncOutboxConfig.OUTBOX_SYNCED_TTL_MS)
            }
        }
    }

    private companion object {
        const val ONE_DAY_MS = 24L * 60 * 60 * 1000
    }
}
