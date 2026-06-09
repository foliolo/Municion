package al.ahgitdevelopment.municion.data.repository

import al.ahgitdevelopment.municion.data.local.room.dao.GuiaDao
import al.ahgitdevelopment.municion.data.local.room.dao.SyncOperationDao
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
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

class GuiaRepository(
    private val guiaDao: GuiaDao,
    private val outboxDao: SyncOperationDao,
    private val enqueuer: SyncOutboxEnqueuer,
    private val rtdb: MunicionRtdbDatasource,
    private val crashReporter: CrashReporter,
    private val syncScheduler: SyncScheduler,
) {
    val guias: Flow<List<Guia>> = guiaDao.getAllGuiasFlow()
    val needsAttentionCount: Flow<Int> = guiaDao.countNeedsAttentionFlow()

    fun getGuiasByTipoLicencia(tipoLicencia: Int): Flow<List<Guia>> = guiaDao.getGuiasByTipoLicenciaFlow(tipoLicencia)
    suspend fun getGuiaById(id: Int): Guia? = guiaDao.getGuiaById(id)
    suspend fun getGuiaBySyncId(syncId: String): Guia? = guiaDao.getGuiaBySyncId(syncId)
    suspend fun getGuiaByNumero(numGuia: String): Guia? = guiaDao.getGuiaByNumero(numGuia)
    suspend fun getAllGuias(): List<Guia> = guiaDao.getAllGuias()

    suspend fun saveGuia(guia: Guia, userId: String? = null): Result<Long> = runCatching {
        val stamped = guia.copy(
            syncId = guia.syncId.ifBlank { SyncIdGenerator.newSyncId() },
            deleted = false,
            deletedAt = null,
            updatedAt = nowMillis(),
        )
        val rowId = guiaDao.insert(stamped)
        enqueuer.enqueueUpsert(stamped.copy(id = rowId.toInt()), userId)
        syncScheduler.requestImmediateDrain()
        rowId
    }.onFailure { crashReporter.recordException(it) }

    suspend fun updateGuia(guia: Guia, userId: String? = null): Result<Unit> = runCatching {
        val stamped = guia.copy(
            syncId = guia.syncId.ifBlank { SyncIdGenerator.newSyncId() },
            updatedAt = nowMillis(),
        )
        guiaDao.update(stamped)
        enqueuer.enqueueUpsert(stamped, userId)
        syncScheduler.requestImmediateDrain()
    }.onFailure { crashReporter.recordException(it) }

    suspend fun deleteGuia(guia: Guia, userId: String? = null): Result<Unit> = runCatching {
        val now = nowMillis()
        val tombstoned = guia.copy(
            syncId = guia.syncId.ifBlank { SyncIdGenerator.newSyncId() },
            deleted = true,
            deletedAt = now,
            updatedAt = now,
        )
        guiaDao.update(tombstoned)
        enqueuer.enqueueUpsert(tombstoned, userId)
        syncScheduler.requestImmediateDrain()
    }.onFailure { crashReporter.recordException(it) }

    /** Increments the spent quota and re-enqueues the updated guia (called by Compra use cases). */
    suspend fun incrementGastado(id: Int, cantidad: Int, userId: String? = null): Result<Unit> = runCatching {
        val guia = guiaDao.getGuiaById(id) ?: error("Guía no encontrada: $id")
        val updated = guia.copy(gastado = guia.gastado + cantidad, updatedAt = nowMillis())
        guiaDao.update(updated)
        enqueuer.enqueueUpsert(updated, userId)
        syncScheduler.requestImmediateDrain()
    }.onFailure { crashReporter.recordException(it) }

    /** Decrements the spent quota (rollback), never below zero. */
    suspend fun decrementGastado(id: Int, cantidad: Int, userId: String? = null): Result<Unit> = runCatching {
        val guia = guiaDao.getGuiaById(id) ?: error("Guía no encontrada: $id")
        val updated = guia.copy(gastado = (guia.gastado - cantidad).coerceAtLeast(0), updatedAt = nowMillis())
        guiaDao.update(updated)
        enqueuer.enqueueUpsert(updated, userId)
        syncScheduler.requestImmediateDrain()
    }.onFailure { crashReporter.recordException(it) }

    suspend fun syncFromFirebase(userId: String): Result<SyncResultWithErrors> = runCatching {
        crashReporter.setUserId(userId)
        val dtos = rtdb.readGuias(userId)
        val parseErrors = mutableListOf<ParseError>()
        val localBySyncId = guiaDao.getAllGuiasIncludingDeleted().associateBy { it.syncId }
        val pending = outboxDao.pendingSyncIdsFor("Guia").toSet()
        var upserted = 0

        dtos.forEach { (key, dto) ->
            val parsed = TolerantParsers.parseGuia(key, dto)
            if (parsed == null) {
                parseErrors += ParseError("Guia", key ?: "?", "<unparseable>", "InvalidShape", "[REDACTED]")
                return@forEach
            }
            if (parsed.syncId in pending) return@forEach
            val local = localBySyncId[parsed.syncId]
            if (local == null || parsed.updatedAt > local.updatedAt) {
                guiaDao.insert(if (local != null) parsed.copy(id = local.id) else parsed.copy(id = 0))
                upserted++
            }
        }

        SyncResultWithErrors(
            success = true,
            syncedCount = upserted,
            totalInFirebase = dtos.size,
            parseErrors = parseErrors,
            hasLocalData = guiaDao.getCount() > 0,
        )
    }.onFailure { crashReporter.recordException(it) }
}
