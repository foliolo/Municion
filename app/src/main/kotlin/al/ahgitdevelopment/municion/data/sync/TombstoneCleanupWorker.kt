package al.ahgitdevelopment.municion.data.sync

import al.ahgitdevelopment.municion.data.local.room.dao.CompraDao
import al.ahgitdevelopment.municion.data.local.room.dao.GuiaDao
import al.ahgitdevelopment.municion.data.local.room.dao.LicenciaDao
import al.ahgitdevelopment.municion.data.local.room.dao.SyncOperationDao
import al.ahgitdevelopment.municion.data.local.room.dao.TiradaDao
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Periodic worker that purges:
 *
 *  - Synced outbox rows older than [SyncOutboxConfig.OUTBOX_SYNCED_TTL_MS]
 *    so the table doesn't grow unbounded.
 *
 *  - Room tombstones (deleted=true rows) older than
 *    [SyncOutboxConfig.TOMBSTONE_TTL_MS]. By that point we trust that all
 *    of the user's devices have observed the tombstone, so we can drop
 *    the local row.
 *
 * NOTE: This worker does NOT remove tombstones from Firebase. They stay
 * forever as a single small entry, so a brand-new device pulling the
 * collection still sees the deletion. Storage cost is negligible.
 *
 * @since v3.3.0 (Sync redesign)
 */
@HiltWorker
class TombstoneCleanupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val licenciaDao: LicenciaDao,
    private val guiaDao: GuiaDao,
    private val compraDao: CompraDao,
    private val tiradaDao: TiradaDao,
    private val outboxDao: SyncOperationDao
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "tombstone_cleanup"
        private const val TAG = "TombstoneCleanup"
    }

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        val tombstoneBefore = now - SyncOutboxConfig.TOMBSTONE_TTL_MS
        val outboxBefore = now - SyncOutboxConfig.OUTBOX_SYNCED_TTL_MS

        val purgedOutbox = outboxDao.purgeSyncedBefore(outboxBefore)
        val purgedLicencias = licenciaDao.purgeTombstonesBefore(tombstoneBefore)
        val purgedGuias = guiaDao.purgeTombstonesBefore(tombstoneBefore)
        val purgedCompras = compraDao.purgeTombstonesBefore(tombstoneBefore)
        val purgedTiradas = tiradaDao.purgeTombstonesBefore(tombstoneBefore)

        Log.i(TAG, "Purged outbox=$purgedOutbox, licencias=$purgedLicencias, " +
                "guias=$purgedGuias, compras=$purgedCompras, tiradas=$purgedTiradas")

        return Result.success()
    }
}
