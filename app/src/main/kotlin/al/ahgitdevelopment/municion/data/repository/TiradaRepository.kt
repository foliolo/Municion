package al.ahgitdevelopment.municion.data.repository

import al.ahgitdevelopment.municion.data.local.room.MunicionDatabase
import al.ahgitdevelopment.municion.data.local.room.dao.SyncOperationDao
import al.ahgitdevelopment.municion.data.local.room.dao.TiradaDao
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
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
 * Repository for Tirada.
 *
 * Same write/read contract as the other repos.
 *
 * @since v3.0.0 (TRACK B Modernization)
 * @updated v3.3.0 (Sync redesign)
 */
@Singleton
class TiradaRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val database: MunicionDatabase,
    private val tiradaDao: TiradaDao,
    private val outboxDao: SyncOperationDao,
    private val outboxEnqueuer: SyncOutboxEnqueuer,
    private val firebaseDb: DatabaseReference,
    private val crashlytics: FirebaseCrashlytics
) {

    companion object {
        private const val TAG = "TiradaRepository"
        private const val ENTITY_PATH = "tiradas"
        private const val ENTITY_TYPE = "Tirada"
    }

    val tiradas: Flow<List<Tirada>> = tiradaDao.getAllTiradasFlow()

    suspend fun getTiradaById(id: Int): Tirada? = withContext(Dispatchers.IO) {
        tiradaDao.getTiradaById(id)
    }

    suspend fun getTiradaBySyncId(syncId: String): Tirada? = withContext(Dispatchers.IO) {
        tiradaDao.getTiradaBySyncId(syncId)
    }

    suspend fun getTiradasConPuntuacion(): List<Tirada> = withContext(Dispatchers.IO) {
        tiradaDao.getTiradasConPuntuacion()
    }

    suspend fun getEstadisticas(): TiradaEstadisticas = withContext(Dispatchers.IO) {
        TiradaEstadisticas(
            total = tiradaDao.countTiradas(),
            promedio = tiradaDao.getPromedioPuntuacion() ?: 0f,
            mejor = tiradaDao.getMejorPuntuacion() ?: 0f
        )
    }

    suspend fun saveTirada(tirada: Tirada, userId: String? = null): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val stamped = tirada.copy(
                syncId = tirada.syncId.takeIf { it.isNotBlank() } ?: SyncIdGenerator.newSyncId(),
                deleted = false,
                deletedAt = null,
                updatedAt = System.currentTimeMillis()
            )
            val id = database.withTransaction {
                val rowId = tiradaDao.insert(stamped)
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

    suspend fun updateTirada(tirada: Tirada, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val stamped = tirada.copy(
                syncId = tirada.syncId.takeIf { it.isNotBlank() } ?: SyncIdGenerator.newSyncId(),
                updatedAt = System.currentTimeMillis()
            )
            database.withTransaction {
                tiradaDao.update(stamped)
                outboxEnqueuer.enqueueUpsert(stamped, userId)
            }
            triggerSync(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    suspend fun deleteTirada(tirada: Tirada, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis()
            val tombstoned = tirada.copy(
                syncId = tirada.syncId.takeIf { it.isNotBlank() } ?: SyncIdGenerator.newSyncId(),
                deleted = true,
                deletedAt = now,
                updatedAt = now
            )
            database.withTransaction {
                tiradaDao.update(tombstoned)
                outboxEnqueuer.enqueueUpsert(tombstoned, userId)
            }
            triggerSync(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
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
            val localBySyncId = tiradaDao.getAllTiradasIncludingDeleted().associateBy { it.syncId }
            val pendingSyncIds = outboxDao.pendingSyncIdsFor(ENTITY_TYPE).toSet()

            var upserted = 0
            var skippedPending = 0
            var skippedOlder = 0

            snapshot.children.forEach { child ->
                val key = child.key
                val parsed = TolerantParsers.parseTirada(key, child.value)
                if (parsed == null) {
                    parseErrors += ParseError(
                        entity = "Tirada",
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
                    tiradaDao.insert(toInsert)
                    upserted++
                } else {
                    skippedOlder++
                }
            }

            Log.i(TAG, "Sync $ENTITY_PATH: total=$totalInFirebase upserted=$upserted skippedPending=$skippedPending skippedOlder=$skippedOlder degraded=${degradedSyncIds.size}")
            if (degradedSyncIds.isNotEmpty()) {
                crashlytics.log("Tirada degraded count: ${degradedSyncIds.size}")
            }

            Result.success(SyncResultWithErrors(
                success = true,
                syncedCount = upserted,
                totalInFirebase = totalInFirebase,
                parseErrors = parseErrors,
                hasLocalData = tiradaDao.countTiradas() > 0
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

    data class TiradaEstadisticas(
        val total: Int,
        val promedio: Float,
        val mejor: Float
    )
}
