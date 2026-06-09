package al.ahgitdevelopment.municion.data.repository

import al.ahgitdevelopment.municion.data.local.room.dao.CompraDao
import al.ahgitdevelopment.municion.data.local.room.dao.SyncOperationDao
import al.ahgitdevelopment.municion.data.local.room.entities.Compra
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

class CompraRepository(
    private val compraDao: CompraDao,
    private val outboxDao: SyncOperationDao,
    private val enqueuer: SyncOutboxEnqueuer,
    private val rtdb: MunicionRtdbDatasource,
    private val crashReporter: CrashReporter,
    private val syncScheduler: SyncScheduler,
) {
    val compras: Flow<List<Compra>> = compraDao.getAllComprasFlow()
    val needsAttentionCount: Flow<Int> = compraDao.countNeedsAttentionFlow()

    fun getComprasByGuia(guiaId: Int): Flow<List<Compra>> = compraDao.getComprasByGuiaFlow(guiaId)

    suspend fun getCompraById(id: Int): Compra? = compraDao.getCompraById(id)

    suspend fun getCompraBySyncId(syncId: String): Compra? = compraDao.getCompraBySyncId(syncId)

    suspend fun saveCompra(
        compra: Compra,
        userId: String? = null,
    ): Result<Long> =
        runCatching {
            val stamped =
                compra.copy(
                    syncId = compra.syncId.ifBlank { SyncIdGenerator.newSyncId() },
                    deleted = false,
                    deletedAt = null,
                    updatedAt = nowMillis(),
                )
            val rowId = compraDao.insert(stamped)
            enqueuer.enqueueUpsert(stamped.copy(id = rowId.toInt()), userId)
            syncScheduler.requestImmediateDrain()
            rowId
        }.onFailure { crashReporter.recordException(it) }

    suspend fun updateCompra(
        compra: Compra,
        userId: String? = null,
    ): Result<Unit> =
        runCatching {
            val stamped =
                compra.copy(
                    syncId = compra.syncId.ifBlank { SyncIdGenerator.newSyncId() },
                    updatedAt = nowMillis(),
                )
            compraDao.update(stamped)
            enqueuer.enqueueUpsert(stamped, userId)
            syncScheduler.requestImmediateDrain()
        }.onFailure { crashReporter.recordException(it) }

    suspend fun deleteCompra(
        compra: Compra,
        userId: String? = null,
    ): Result<Unit> =
        runCatching {
            val now = nowMillis()
            val tombstoned =
                compra.copy(
                    syncId = compra.syncId.ifBlank { SyncIdGenerator.newSyncId() },
                    deleted = true,
                    deletedAt = now,
                    updatedAt = now,
                )
            compraDao.update(tombstoned)
            enqueuer.enqueueUpsert(tombstoned, userId)
            syncScheduler.requestImmediateDrain()
        }.onFailure { crashReporter.recordException(it) }

    suspend fun syncFromFirebase(userId: String): Result<SyncResultWithErrors> =
        runCatching {
            crashReporter.setUserId(userId)
            val dtos = rtdb.readCompras(userId)
            val parseErrors = mutableListOf<ParseError>()
            val localBySyncId = compraDao.getAllComprasIncludingDeleted().associateBy { it.syncId }
            val pending = outboxDao.pendingSyncIdsFor("Compra").toSet()
            var upserted = 0

            dtos.forEach { (key, dto) ->
                val parsed = TolerantParsers.parseCompra(key, dto)
                if (parsed == null) {
                    parseErrors += ParseError("Compra", key ?: "?", "<unparseable>", "InvalidShape", "[REDACTED]")
                    return@forEach
                }
                if (parsed.syncId in pending) return@forEach
                val local = localBySyncId[parsed.syncId]
                if (local == null || parsed.updatedAt > local.updatedAt) {
                    compraDao.insert(if (local != null) parsed.copy(id = local.id) else parsed.copy(id = 0))
                    upserted++
                }
            }

            SyncResultWithErrors(
                success = true,
                syncedCount = upserted,
                totalInFirebase = dtos.size,
                parseErrors = parseErrors,
                hasLocalData = compraDao.getCount() > 0,
            )
        }.onFailure { crashReporter.recordException(it) }
}
