package al.ahgitdevelopment.municion.data.repository

import al.ahgitdevelopment.municion.data.local.room.MunicionDatabase
import al.ahgitdevelopment.municion.data.local.room.dao.CompraDao
import al.ahgitdevelopment.municion.data.local.room.dao.GuiaDao
import al.ahgitdevelopment.municion.data.local.room.dao.SyncOperationDao
import al.ahgitdevelopment.municion.data.local.room.entities.Compra
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
 * Repository for Compra.
 *
 * Inherits the same write/read contract as the other repos. Additionally,
 * fills [Compra.guiaSyncId] from the parent Guia at save time so future
 * cross-device sync can resolve the parent by syncId instead of the
 * fragile positional [Compra.idPosGuia].
 *
 * @since v3.0.0 (TRACK B Modernization)
 * @updated v3.3.0 (Sync redesign)
 */
@Singleton
class CompraRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val database: MunicionDatabase,
    private val compraDao: CompraDao,
    private val guiaDao: GuiaDao,
    private val outboxDao: SyncOperationDao,
    private val outboxEnqueuer: SyncOutboxEnqueuer,
    private val firebaseDb: DatabaseReference,
    private val crashlytics: FirebaseCrashlytics
) {

    companion object {
        private const val TAG = "CompraRepository"
        private const val ENTITY_PATH = "compras"
        private const val ENTITY_TYPE = "Compra"
    }

    val compras: Flow<List<Compra>> = compraDao.getAllComprasFlow()

    val needsAttentionCount: Flow<Int> = compraDao.countNeedsAttentionFlow()

    fun getComprasByGuia(guiaId: Int): Flow<List<Compra>> {
        return compraDao.getComprasByGuiaFlow(guiaId)
    }

    suspend fun getCompraById(id: Int): Compra? = withContext(Dispatchers.IO) {
        compraDao.getCompraById(id)
    }

    suspend fun getCompraBySyncId(syncId: String): Compra? = withContext(Dispatchers.IO) {
        compraDao.getCompraBySyncId(syncId)
    }

    suspend fun saveCompra(compra: Compra, userId: String? = null): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis()
            val id = database.withTransaction {
                val parentSyncId = compra.guiaSyncId
                    ?: guiaDao.getGuiaById(compra.idPosGuia)?.syncId
                val stamped = compra.copy(
                    syncId = compra.syncId.takeIf { it.isNotBlank() } ?: SyncIdGenerator.newSyncId(),
                    guiaSyncId = parentSyncId,
                    deleted = false,
                    deletedAt = null,
                    updatedAt = now
                )
                val rowId = compraDao.insert(stamped)
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

    suspend fun updateCompra(compra: Compra, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            database.withTransaction {
                val parentSyncId = compra.guiaSyncId
                    ?: guiaDao.getGuiaById(compra.idPosGuia)?.syncId
                val stamped = compra.copy(
                    syncId = compra.syncId.takeIf { it.isNotBlank() } ?: SyncIdGenerator.newSyncId(),
                    guiaSyncId = parentSyncId,
                    updatedAt = System.currentTimeMillis()
                )
                compraDao.update(stamped)
                outboxEnqueuer.enqueueUpsert(stamped, userId)
            }
            triggerSync(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    suspend fun deleteCompra(compra: Compra, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis()
            val tombstoned = compra.copy(
                syncId = compra.syncId.takeIf { it.isNotBlank() } ?: SyncIdGenerator.newSyncId(),
                deleted = true,
                deletedAt = now,
                updatedAt = now
            )
            database.withTransaction {
                compraDao.update(tombstoned)
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
            val localBySyncId = compraDao.getAllComprasIncludingDeleted().associateBy { it.syncId }
            val pendingSyncIds = outboxDao.pendingSyncIdsFor(ENTITY_TYPE).toSet()

            var upserted = 0
            var skippedPending = 0
            var skippedOlder = 0

            snapshot.children.forEach { child ->
                val key = child.key
                val parsed = TolerantParsers.parseCompra(key, child.value)
                if (parsed == null) {
                    parseErrors += ParseError(
                        entity = "Compra",
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
                    compraDao.insert(toInsert)
                    upserted++
                } else {
                    skippedOlder++
                }
            }

            Log.i(TAG, "Sync $ENTITY_PATH: total=$totalInFirebase upserted=$upserted skippedPending=$skippedPending skippedOlder=$skippedOlder degraded=${degradedSyncIds.size}")
            if (degradedSyncIds.isNotEmpty()) {
                crashlytics.log("Compra degraded count: ${degradedSyncIds.size}")
            }

            Result.success(SyncResultWithErrors(
                success = true,
                syncedCount = upserted,
                totalInFirebase = totalInFirebase,
                parseErrors = parseErrors,
                hasLocalData = compraDao.getCount() > 0
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

    suspend fun detectCorruptedCompras(): List<Compra> = withContext(Dispatchers.IO) {
        compraDao.getComprasWithCorruptedPeso()
    }

    suspend fun countComprasByGuia(guiaId: Int): Int = withContext(Dispatchers.IO) {
        compraDao.countComprasByGuia(guiaId)
    }

    private fun triggerSync(userId: String?) {
        if (userId.isNullOrBlank()) return
        SyncOutboxWorker.enqueueOneShot(appContext)
    }
}
