package al.ahgitdevelopment.municion.data.repository

import al.ahgitdevelopment.municion.data.local.room.dao.LicenciaDao
import al.ahgitdevelopment.municion.data.local.room.dao.SyncOperationDao
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
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

/**
 * Write path: Room insert → outbox enqueue → request drain. The outbox is the only writer
 * to Firebase. Read path: tolerant parse + non-destructive merge (never deletes a local row
 * just because it's absent remotely; pending local writes win).
 */
class LicenciaRepository(
    private val licenciaDao: LicenciaDao,
    private val outboxDao: SyncOperationDao,
    private val enqueuer: SyncOutboxEnqueuer,
    private val rtdb: MunicionRtdbDatasource,
    private val crashReporter: CrashReporter,
    private val syncScheduler: SyncScheduler,
) {
    val licencias: Flow<List<Licencia>> = licenciaDao.getAllLicenciasFlow()
    val needsAttentionCount: Flow<Int> = licenciaDao.countNeedsAttentionFlow()

    fun getLicenciasByTipo(tipo: Int): Flow<List<Licencia>> = licenciaDao.getLicenciasByTipoFlow(tipo)
    suspend fun getLicenciaById(id: Int): Licencia? = licenciaDao.getLicenciaById(id)
    suspend fun getLicenciaBySyncId(syncId: String): Licencia? = licenciaDao.getLicenciaBySyncId(syncId)
    suspend fun getLicenciaByNumero(numLicencia: String): Licencia? = licenciaDao.getLicenciaByNumero(numLicencia)
    suspend fun existsLicencia(numLicencia: String): Boolean = licenciaDao.existsLicencia(numLicencia)

    suspend fun saveLicencia(licencia: Licencia, userId: String? = null): Result<Long> = runCatching {
        val stamped = licencia.copy(
            syncId = licencia.syncId.ifBlank { SyncIdGenerator.newSyncId() },
            deleted = false,
            deletedAt = null,
            updatedAt = nowMillis(),
        )
        val rowId = licenciaDao.insert(stamped)
        enqueuer.enqueueUpsert(stamped.copy(id = rowId.toInt()), userId)
        syncScheduler.requestImmediateDrain()
        rowId
    }.onFailure { crashReporter.recordException(it) }

    suspend fun updateLicencia(licencia: Licencia, userId: String? = null): Result<Unit> = runCatching {
        val stamped = licencia.copy(
            syncId = licencia.syncId.ifBlank { SyncIdGenerator.newSyncId() },
            updatedAt = nowMillis(),
        )
        licenciaDao.update(stamped)
        enqueuer.enqueueUpsert(stamped, userId)
        syncScheduler.requestImmediateDrain()
    }.onFailure { crashReporter.recordException(it) }

    suspend fun deleteLicencia(licencia: Licencia, userId: String? = null): Result<Unit> = runCatching {
        val now = nowMillis()
        val tombstoned = licencia.copy(
            syncId = licencia.syncId.ifBlank { SyncIdGenerator.newSyncId() },
            deleted = true,
            deletedAt = now,
            updatedAt = now,
        )
        licenciaDao.update(tombstoned)
        enqueuer.enqueueUpsert(tombstoned, userId)
        syncScheduler.requestImmediateDrain()
    }.onFailure { crashReporter.recordException(it) }

    suspend fun syncFromFirebase(userId: String): Result<SyncResultWithErrors> = runCatching {
        crashReporter.setUserId(userId)
        val dtos = rtdb.readLicencias(userId)
        val parseErrors = mutableListOf<ParseError>()
        val localBySyncId = licenciaDao.getAllLicenciasIncludingDeleted().associateBy { it.syncId }
        val pending = outboxDao.pendingSyncIdsFor("Licencia").toSet()
        var upserted = 0

        dtos.forEach { (key, dto) ->
            val parsed = TolerantParsers.parseLicencia(key, dto)
            if (parsed == null) {
                parseErrors += ParseError("Licencia", key ?: "?", "<unparseable>", "InvalidShape", "[REDACTED]")
                return@forEach
            }
            if (parsed.syncId in pending) return@forEach
            val local = localBySyncId[parsed.syncId]
            if (local == null || parsed.updatedAt > local.updatedAt) {
                licenciaDao.insert(if (local != null) parsed.copy(id = local.id) else parsed.copy(id = 0))
                upserted++
            }
        }

        SyncResultWithErrors(
            success = true,
            syncedCount = upserted,
            totalInFirebase = dtos.size,
            parseErrors = parseErrors,
            hasLocalData = licenciaDao.countLicencias() > 0,
        )
    }.onFailure { crashReporter.recordException(it) }
}
