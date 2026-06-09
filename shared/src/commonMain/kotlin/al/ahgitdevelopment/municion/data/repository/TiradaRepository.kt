package al.ahgitdevelopment.municion.data.repository

import al.ahgitdevelopment.municion.data.local.room.dao.SyncOperationDao
import al.ahgitdevelopment.municion.data.local.room.dao.TiradaDao
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import al.ahgitdevelopment.municion.data.sync.MunicionRtdbDatasource
import al.ahgitdevelopment.municion.data.sync.SyncIdGenerator
import al.ahgitdevelopment.municion.data.sync.SyncOutboxEnqueuer
import al.ahgitdevelopment.municion.data.sync.SyncScheduler
import al.ahgitdevelopment.municion.data.sync.TolerantParsers
import al.ahgitdevelopment.municion.domain.usecase.ParseError
import al.ahgitdevelopment.municion.domain.usecase.SyncResultWithErrors
import al.ahgitdevelopment.municion.firebase.CrashReporter
import al.ahgitdevelopment.municion.util.nowMillis
import kotlinx.coroutines.flow.Flow

class TiradaRepository(
    private val tiradaDao: TiradaDao,
    private val outboxDao: SyncOperationDao,
    private val enqueuer: SyncOutboxEnqueuer,
    private val rtdb: MunicionRtdbDatasource,
    private val crashReporter: CrashReporter,
    private val syncScheduler: SyncScheduler,
) {
    val tiradas: Flow<List<Tirada>> = tiradaDao.getAllTiradasFlow()
    val needsAttentionCount: Flow<Int> = tiradaDao.countNeedsAttentionFlow()

    suspend fun getTiradaById(id: Int): Tirada? = tiradaDao.getTiradaById(id)
    suspend fun getTiradaBySyncId(syncId: String): Tirada? = tiradaDao.getTiradaBySyncId(syncId)

    suspend fun saveTirada(tirada: Tirada, userId: String? = null): Result<Long> = runCatching {
        val stamped = tirada.copy(
            syncId = tirada.syncId.ifBlank { SyncIdGenerator.newSyncId() },
            deleted = false,
            deletedAt = null,
            updatedAt = nowMillis(),
        )
        val rowId = tiradaDao.insert(stamped)
        enqueuer.enqueueUpsert(stamped.copy(id = rowId.toInt()), userId)
        syncScheduler.requestImmediateDrain()
        rowId
    }.onFailure { crashReporter.recordException(it) }

    suspend fun updateTirada(tirada: Tirada, userId: String? = null): Result<Unit> = runCatching {
        val stamped = tirada.copy(
            syncId = tirada.syncId.ifBlank { SyncIdGenerator.newSyncId() },
            updatedAt = nowMillis(),
        )
        tiradaDao.update(stamped)
        enqueuer.enqueueUpsert(stamped, userId)
        syncScheduler.requestImmediateDrain()
    }.onFailure { crashReporter.recordException(it) }

    suspend fun deleteTirada(tirada: Tirada, userId: String? = null): Result<Unit> = runCatching {
        val now = nowMillis()
        val tombstoned = tirada.copy(
            syncId = tirada.syncId.ifBlank { SyncIdGenerator.newSyncId() },
            deleted = true,
            deletedAt = now,
            updatedAt = now,
        )
        tiradaDao.update(tombstoned)
        enqueuer.enqueueUpsert(tombstoned, userId)
        syncScheduler.requestImmediateDrain()
    }.onFailure { crashReporter.recordException(it) }

    suspend fun syncFromFirebase(userId: String): Result<SyncResultWithErrors> = runCatching {
        crashReporter.setUserId(userId)
        val dtos = rtdb.readTiradas(userId)
        val parseErrors = mutableListOf<ParseError>()
        val localBySyncId = tiradaDao.getAllTiradasIncludingDeleted().associateBy { it.syncId }
        val pending = outboxDao.pendingSyncIdsFor("Tirada").toSet()
        var upserted = 0

        dtos.forEach { (key, dto) ->
            val parsed = TolerantParsers.parseTirada(key, dto)
            if (parsed == null) {
                parseErrors += ParseError("Tirada", key ?: "?", "<unparseable>", "InvalidShape", "[REDACTED]")
                return@forEach
            }
            if (parsed.syncId in pending) return@forEach
            val local = localBySyncId[parsed.syncId]
            if (local == null || parsed.updatedAt > local.updatedAt) {
                tiradaDao.insert(if (local != null) parsed.copy(id = local.id) else parsed.copy(id = 0))
                upserted++
            }
        }

        SyncResultWithErrors(
            success = true,
            syncedCount = upserted,
            totalInFirebase = dtos.size,
            parseErrors = parseErrors,
            hasLocalData = tiradaDao.countTiradas() > 0,
        )
    }.onFailure { crashReporter.recordException(it) }
}
