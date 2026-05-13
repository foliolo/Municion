package al.ahgitdevelopment.municion.data.repository

import al.ahgitdevelopment.municion.data.local.room.MunicionDatabase
import al.ahgitdevelopment.municion.data.local.room.dao.GuiaDao
import al.ahgitdevelopment.municion.data.local.room.dao.SyncOperationDao
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.sync.SyncIdGenerator
import al.ahgitdevelopment.municion.data.sync.SyncOutboxEnqueuer
import al.ahgitdevelopment.municion.data.sync.SyncOutboxWorker
import al.ahgitdevelopment.municion.data.sync.TolerantParsers
import al.ahgitdevelopment.municion.domain.usecase.ParseError
import al.ahgitdevelopment.municion.domain.usecase.SyncResultWithErrors
import android.content.Context
import android.util.Log
import androidx.room.withTransaction
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
 * Repository for Guia.
 *
 * Same write/read contract as [LicenciaRepository]: writes go through the
 * outbox; reads are non-destructive.
 *
 * @since v3.0.0 (TRACK B Modernization)
 * @updated v3.3.0 (Sync redesign)
 */
@Singleton
class GuiaRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val database: MunicionDatabase,
    private val guiaDao: GuiaDao,
    private val outboxDao: SyncOperationDao,
    private val outboxEnqueuer: SyncOutboxEnqueuer,
    private val firebaseDb: DatabaseReference,
    private val crashlytics: FirebaseCrashlytics
) {

    companion object {
        private const val TAG = "GuiaRepository"
        private const val ENTITY_PATH = "guias"
        private const val ENTITY_TYPE = "Guia"
    }

    val guias: Flow<List<Guia>> = guiaDao.getAllGuiasFlow()

    val needsAttentionCount: Flow<Int> = guiaDao.countNeedsAttentionFlow()

    fun getGuiasByTipoLicencia(tipoLicencia: Int): Flow<List<Guia>> {
        return guiaDao.getGuiasByTipoLicenciaFlow(tipoLicencia)
    }

    suspend fun getGuiaById(id: Int): Guia? = withContext(Dispatchers.IO) {
        guiaDao.getGuiaById(id)
    }

    suspend fun getGuiaBySyncId(syncId: String): Guia? = withContext(Dispatchers.IO) {
        guiaDao.getGuiaBySyncId(syncId)
    }

    suspend fun saveGuia(guia: Guia, userId: String? = null): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val stamped = guia.copy(
                syncId = guia.syncId.takeIf { it.isNotBlank() } ?: SyncIdGenerator.newSyncId(),
                deleted = false,
                deletedAt = null,
                updatedAt = System.currentTimeMillis()
            )
            val id = database.withTransaction {
                val rowId = guiaDao.insert(stamped)
                val saved = stamped.copy(id = rowId.toInt())
                outboxEnqueuer.enqueueUpsert(saved, userId)
                rowId
            }
            triggerSync(userId)
            Result.success(id)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    suspend fun updateGuia(guia: Guia, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val stamped = guia.copy(
                syncId = guia.syncId.takeIf { it.isNotBlank() } ?: SyncIdGenerator.newSyncId(),
                updatedAt = System.currentTimeMillis()
            )
            database.withTransaction {
                guiaDao.update(stamped)
                outboxEnqueuer.enqueueUpsert(stamped, userId)
            }
            triggerSync(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    suspend fun deleteGuia(guia: Guia, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis()
            val tombstoned = guia.copy(
                syncId = guia.syncId.takeIf { it.isNotBlank() } ?: SyncIdGenerator.newSyncId(),
                deleted = true,
                deletedAt = now,
                updatedAt = now
            )
            database.withTransaction {
                guiaDao.update(tombstoned)
                outboxEnqueuer.enqueueUpsert(tombstoned, userId)
            }
            triggerSync(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    suspend fun updateGastado(guiaId: Int, gastado: Int, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            database.withTransaction {
                guiaDao.updateGastado(guiaId, gastado)
                guiaDao.getGuiaById(guiaId)?.let { updated ->
                    val stamped = updated.copy(updatedAt = System.currentTimeMillis())
                    guiaDao.update(stamped)
                    outboxEnqueuer.enqueueUpsert(stamped, userId)
                }
            }
            triggerSync(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    suspend fun incrementGastado(guiaId: Int, cantidad: Int, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            database.withTransaction {
                guiaDao.incrementGastado(guiaId, cantidad)
                guiaDao.getGuiaById(guiaId)?.let { updated ->
                    val stamped = updated.copy(updatedAt = System.currentTimeMillis())
                    guiaDao.update(stamped)
                    outboxEnqueuer.enqueueUpsert(stamped, userId)
                }
            }
            triggerSync(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    suspend fun decrementGastado(guiaId: Int, cantidad: Int, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            database.withTransaction {
                guiaDao.decrementGastado(guiaId, cantidad)
                guiaDao.getGuiaById(guiaId)?.let { updated ->
                    val stamped = updated.copy(updatedAt = System.currentTimeMillis())
                    guiaDao.update(stamped)
                    outboxEnqueuer.enqueueUpsert(stamped, userId)
                }
            }
            triggerSync(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Annual quota reset. Updates each guia individually and enqueues a
     * single outbox row per guia (coalesced if multiple updates land on
     * the same entity).
     */
    suspend fun resetAllGastado(userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            database.withTransaction {
                guiaDao.resetAllGastado()
                val now = System.currentTimeMillis()
                for (guia in guiaDao.getAllGuias()) {
                    val stamped = guia.copy(gastado = 0, updatedAt = now)
                    guiaDao.update(stamped)
                    outboxEnqueuer.enqueueUpsert(stamped, userId)
                }
            }
            triggerSync(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    suspend fun getGuiasConCupoAgotado(): List<Guia> = withContext(Dispatchers.IO) {
        guiaDao.getGuiasConCupoAgotado()
    }

    suspend fun syncFromFirebase(userId: String): Result<SyncResultWithErrors> = withContext(Dispatchers.IO) {
        try {
            crashlytics.setUserId(userId)
            val snapshot = firebaseDb
                .child("users").child(userId).child("db").child(ENTITY_PATH)
                .get().await()

            val totalInFirebase = snapshot.childrenCount.toInt()
            val parseErrors = mutableListOf<ParseError>()
            val degradedSyncIds = mutableListOf<String>()
            val localBySyncId = guiaDao.getAllGuiasIncludingDeleted().associateBy { it.syncId }
            val pendingSyncIds = outboxDao.pendingSyncIdsFor(ENTITY_TYPE).toSet()

            var upserted = 0
            var skippedPending = 0
            var skippedOlder = 0

            snapshot.children.forEach { child ->
                val key = child.key
                val parsed = TolerantParsers.parseGuia(key, child.value)
                if (parsed == null) {
                    parseErrors += ParseError(
                        entity = "Guia",
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
                    guiaDao.insert(toInsert)
                    upserted++
                } else {
                    skippedOlder++
                }
            }

            Log.i(TAG, "Sync $ENTITY_PATH: total=$totalInFirebase upserted=$upserted skippedPending=$skippedPending skippedOlder=$skippedOlder degraded=${degradedSyncIds.size}")
            if (degradedSyncIds.isNotEmpty()) {
                crashlytics.log("Guia degraded count: ${degradedSyncIds.size}")
            }

            Result.success(SyncResultWithErrors(
                success = true,
                syncedCount = upserted,
                totalInFirebase = totalInFirebase,
                parseErrors = parseErrors,
                hasLocalData = guiaDao.getCount() > 0
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

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
