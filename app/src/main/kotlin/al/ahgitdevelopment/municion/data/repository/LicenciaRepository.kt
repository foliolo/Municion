package al.ahgitdevelopment.municion.data.repository

import al.ahgitdevelopment.municion.data.local.room.dao.LicenciaDao
import al.ahgitdevelopment.municion.data.local.room.dao.SyncOperationDao
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.sync.SyncIdGenerator
import al.ahgitdevelopment.municion.data.sync.SyncOutboxEnqueuer
import al.ahgitdevelopment.municion.data.sync.SyncOutboxWorker
import al.ahgitdevelopment.municion.data.sync.TolerantParsers
import al.ahgitdevelopment.municion.domain.usecase.ParseError
import al.ahgitdevelopment.municion.domain.usecase.SyncResultWithErrors
import android.content.Context
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DatabaseReference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Licencia.
 *
 * Write path (v3.3+): Room insert → outbox enqueue. The
 * [SyncOutboxWorker] is the only component that writes to Firebase, and
 * it does so per-entity. Failures stay in the outbox and retry with
 * exponential backoff.
 *
 * Read path (v3.3+): tolerant parser + non-destructive merge. The sync
 * NEVER deletes a local entity just because it is missing from a Firebase
 * snapshot — only explicit tombstones propagate deletes. Pending outbox
 * writes always win over remote values to avoid clobbering local edits.
 *
 * @since v3.0.0 (TRACK B Modernization)
 * @updated v3.3.0 (Sync redesign)
 */
@Singleton
class LicenciaRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val licenciaDao: LicenciaDao,
    private val outboxDao: SyncOperationDao,
    private val outboxEnqueuer: SyncOutboxEnqueuer,
    private val firebaseDb: DatabaseReference,
    private val crashlytics: FirebaseCrashlytics
) {

    companion object {
        private const val TAG = "LicenciaRepository"
        private const val ENTITY_PATH = "licencias"
        private const val ENTITY_TYPE = "Licencia"
    }

    val licencias: Flow<List<Licencia>> = licenciaDao.getAllLicenciasFlow()

    fun getLicenciasByTipo(tipo: Int): Flow<List<Licencia>> {
        return licenciaDao.getLicenciasByTipoFlow(tipo)
    }

    suspend fun getLicenciaById(id: Int): Licencia? = withContext(Dispatchers.IO) {
        licenciaDao.getLicenciaById(id)
    }

    suspend fun getLicenciaBySyncId(syncId: String): Licencia? = withContext(Dispatchers.IO) {
        licenciaDao.getLicenciaBySyncId(syncId)
    }

    suspend fun getLicenciaByNumero(numLicencia: String): Licencia? = withContext(Dispatchers.IO) {
        licenciaDao.getLicenciaByNumero(numLicencia)
    }

    suspend fun existsLicencia(numLicencia: String): Boolean = withContext(Dispatchers.IO) {
        licenciaDao.existsLicencia(numLicencia)
    }

    suspend fun saveLicencia(licencia: Licencia, userId: String? = null): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val stamped = licencia.copy(
                syncId = licencia.syncId.takeIf { it.isNotBlank() } ?: SyncIdGenerator.newSyncId(),
                deleted = false,
                deletedAt = null,
                updatedAt = System.currentTimeMillis()
            )
            val id = licenciaDao.insert(stamped)
            val saved = stamped.copy(id = id.toInt())

            outboxEnqueuer.enqueueUpsert(saved, userId)
            triggerSync(userId)

            Result.success(id)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    suspend fun updateLicencia(licencia: Licencia, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val stamped = licencia.copy(
                syncId = licencia.syncId.takeIf { it.isNotBlank() } ?: SyncIdGenerator.newSyncId(),
                updatedAt = System.currentTimeMillis()
            )
            licenciaDao.update(stamped)

            outboxEnqueuer.enqueueUpsert(stamped, userId)
            triggerSync(userId)

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Soft delete: marks the row tombstoned (deleted=true) and enqueues the
     * tombstone for Firebase. Other devices receive the tombstone via the
     * normal sync-from-Firebase flow and apply the soft delete.
     */
    suspend fun deleteLicencia(licencia: Licencia, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis()
            val tombstoned = licencia.copy(
                syncId = licencia.syncId.takeIf { it.isNotBlank() } ?: SyncIdGenerator.newSyncId(),
                deleted = true,
                deletedAt = now,
                updatedAt = now
            )
            licenciaDao.update(tombstoned)
            outboxEnqueuer.enqueueUpsert(tombstoned, userId)
            triggerSync(userId)

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Downloads the licencias collection and applies it non-destructively:
     *  - Upserts entities whose remote updatedAt is newer than local
     *  - Skips entities that have a pending outbox UPSERT (local wins)
     *  - NEVER deletes local entities just because they're absent in remote
     */
    suspend fun syncFromFirebase(userId: String): Result<SyncResultWithErrors> = withContext(Dispatchers.IO) {
        try {
            crashlytics.setUserId(userId)
            val snapshot = firebaseDb
                .child("users").child(userId).child("db").child(ENTITY_PATH)
                .get().await()

            val totalInFirebase = snapshot.childrenCount.toInt()
            val parseErrors = mutableListOf<ParseError>()
            val degradedSyncIds = mutableListOf<String>()

            val localBySyncId = licenciaDao.getAllLicenciasIncludingDeleted().associateBy { it.syncId }
            val pendingSyncIds = outboxDao.pendingSyncIdsFor(ENTITY_TYPE).toSet()

            var upserted = 0
            var skippedPending = 0
            var skippedOlder = 0

            snapshot.children.forEach { child ->
                val key = child.key
                val parsed = TolerantParsers.parseLicencia(key, child.value)
                if (parsed == null) {
                    parseErrors += ParseError(
                        entity = "Licencia",
                        itemKey = key ?: "?",
                        failedField = "<unparseable>",
                        errorType = "InvalidShape",
                        fieldValue = "[REDACTED]"
                    )
                    return@forEach
                }

                if (parsed.dataQuality == "degraded" || parsed.dataQuality == "lost") {
                    degradedSyncIds += parsed.syncId
                }

                if (parsed.syncId in pendingSyncIds) {
                    skippedPending++
                    return@forEach
                }

                val local = localBySyncId[parsed.syncId]
                if (local == null || parsed.updatedAt > local.updatedAt) {
                    val toInsert = if (local != null) parsed.copy(id = local.id) else parsed.copy(id = 0)
                    licenciaDao.insert(toInsert)
                    upserted++
                } else {
                    skippedOlder++
                }
            }

            Log.i(TAG, "Sync $ENTITY_PATH: total=$totalInFirebase upserted=$upserted skippedPending=$skippedPending skippedOlder=$skippedOlder degraded=${degradedSyncIds.size}")
            if (degradedSyncIds.isNotEmpty()) {
                crashlytics.log("Licencia degraded count: ${degradedSyncIds.size}")
            }

            Result.success(SyncResultWithErrors(
                success = true,
                syncedCount = upserted,
                totalInFirebase = totalInFirebase,
                parseErrors = parseErrors,
                hasLocalData = licenciaDao.countLicencias() > 0
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * @deprecated Use the outbox-based per-entity sync. This method is kept
     * as a no-op for API compatibility with [al.ahgitdevelopment.municion.domain.usecase.SyncDataUseCase].
     * The unsafe fullSyncToFirebase that this used to call has been removed.
     */
    @Deprecated("Outbox is the source of truth for writes; this is now a no-op")
    suspend fun syncToFirebase(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        Log.w(TAG, "Deprecated syncToFirebase ignored; outbox handles writes")
        triggerSync(userId)
        Result.success(Unit)
    }

    private fun triggerSync(userId: String?) {
        if (userId.isNullOrBlank()) return
        SyncOutboxWorker.enqueueOneShot(appContext)
    }
}
