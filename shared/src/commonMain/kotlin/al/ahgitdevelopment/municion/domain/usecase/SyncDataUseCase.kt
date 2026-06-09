package al.ahgitdevelopment.municion.domain.usecase

import al.ahgitdevelopment.municion.data.repository.CompraRepository
import al.ahgitdevelopment.municion.data.repository.GuiaRepository
import al.ahgitdevelopment.municion.data.repository.LicenciaRepository
import al.ahgitdevelopment.municion.data.repository.TiradaRepository
import al.ahgitdevelopment.municion.data.sync.SyncScheduler
import al.ahgitdevelopment.municion.firebase.CrashReporter
import al.ahgitdevelopment.municion.firebase.CurrentUserIdProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Orchestrates the download side of sync (non-destructive per the repository contract) and
 * triggers an outbox drain. Upload is the outbox's job, never a destructive re-upload.
 *
 * NOTE: ads status reconciliation (BillingRepository) is wired in phase 7; the legacy
 * Firebase-format migrator is intentionally dropped (tolerant parsing + deterministic syncIds
 * subsume it).
 */
class SyncDataUseCase(
    private val guiaRepository: GuiaRepository,
    private val compraRepository: CompraRepository,
    private val licenciaRepository: LicenciaRepository,
    private val tiradaRepository: TiradaRepository,
    private val syncScheduler: SyncScheduler,
    private val currentUserIdProvider: CurrentUserIdProvider,
    private val crashReporter: CrashReporter,
) {
    fun canSync(): Boolean = currentUserIdProvider.currentUserId() != null
    fun getCurrentUserId(): String? = currentUserIdProvider.currentUserId()

    /** Pulls all four collections in parallel and merges non-destructively. */
    suspend fun syncFromFirebase(userId: String): Result<SyncResult> = try {
        coroutineScope {
            val guias = async { guiaRepository.syncFromFirebase(userId) }
            val compras = async { compraRepository.syncFromFirebase(userId) }
            val licencias = async { licenciaRepository.syncFromFirebase(userId) }
            val tiradas = async { tiradaRepository.syncFromFirebase(userId) }

            val gr = guias.await()
            val cr = compras.await()
            val lr = licencias.await()
            val tr = tiradas.await()

            // Drain any pending local writes now that we know the network is usable.
            syncScheduler.requestImmediateDrain()

            Result.success(
                SyncResult(
                    guiasSuccess = gr.isSuccess,
                    comprasSuccess = cr.isSuccess,
                    licenciasSuccess = lr.isSuccess,
                    tiradasSuccess = tr.isSuccess,
                    guiasSyncResult = gr.getOrNull(),
                    comprasSyncResult = cr.getOrNull(),
                    licenciasSyncResult = lr.getOrNull(),
                    tiradasSyncResult = tr.getOrNull(),
                ),
            )
        }
    } catch (e: Exception) {
        crashReporter.recordException(e)
        Result.failure(e)
    }

    /** Compatibility wrapper (auto-fix removed in the redesign). */
    suspend fun syncFromFirebaseWithAutoFix(userId: String): Result<SyncResult> = syncFromFirebase(userId)

    /** Manual "force sync": trigger an outbox drain (never a destructive re-upload). */
    fun syncToFirebase() {
        syncScheduler.requestImmediateDrain()
    }

    data class SyncResult(
        val guiasSuccess: Boolean,
        val comprasSuccess: Boolean,
        val licenciasSuccess: Boolean,
        val tiradasSuccess: Boolean,
        val guiasSyncResult: SyncResultWithErrors? = null,
        val comprasSyncResult: SyncResultWithErrors? = null,
        val licenciasSyncResult: SyncResultWithErrors? = null,
        val tiradasSyncResult: SyncResultWithErrors? = null,
    ) {
        val allSuccess: Boolean get() = guiasSuccess && comprasSuccess && licenciasSuccess && tiradasSuccess
        val successCount: Int get() = listOf(guiasSuccess, comprasSuccess, licenciasSuccess, tiradasSuccess).count { it }
        val allParseErrors: List<ParseError>
            get() = listOfNotNull(
                guiasSyncResult?.parseErrors,
                comprasSyncResult?.parseErrors,
                licenciasSyncResult?.parseErrors,
                tiradasSyncResult?.parseErrors,
            ).flatten()
        val hasParseErrors: Boolean get() = allParseErrors.isNotEmpty()
    }
}
