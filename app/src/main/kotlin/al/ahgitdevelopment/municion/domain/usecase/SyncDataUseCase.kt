package al.ahgitdevelopment.municion.domain.usecase

import al.ahgitdevelopment.municion.auth.FirebaseAuthRepository
import al.ahgitdevelopment.municion.data.repository.BillingRepository
import al.ahgitdevelopment.municion.data.repository.CompraRepository
import al.ahgitdevelopment.municion.data.repository.GuiaRepository
import al.ahgitdevelopment.municion.data.repository.LicenciaRepository
import al.ahgitdevelopment.municion.data.repository.TiradaRepository
import al.ahgitdevelopment.municion.data.sync.FirebaseFormatMigrator
import al.ahgitdevelopment.municion.data.sync.SyncOutboxWorker
import android.content.Context
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Orchestrates the download side of sync.
 *
 * v3.3+ redesign:
 *
 *  - **Download** (`syncFromFirebase`) reads each collection from
 *    Firebase and applies a non-destructive merge per the new
 *    repository contract. Tolerant parsing keeps every parseable
 *    entity, including stability-corrupt ones (flagged dataQuality).
 *
 *  - **Upload** (`syncToFirebase`) is no longer a thing. The
 *    [SyncOutboxWorker] is the only writer to Firebase, and it runs
 *    automatically on each save/update/delete plus on a periodic
 *    schedule. Callers that used to invoke `syncToFirebase` get a
 *    one-shot trigger of the worker instead — convenient as a manual
 *    "force sync" button without re-introducing the destructive path.
 *
 *  - **Auto-fix** (the old `syncFromFirebaseWithAutoFix` that called
 *    `fullSyncToFirebase`) is removed. Its destructive `setValue(map)`
 *    on the entire collection was the proximate cause of the bulk
 *    data-loss incidents. The new code returns the same result shape
 *    but `autoFixApplied` is always `false`.
 *
 * @since v3.0.0 (TRACK B Modernization)
 * @updated v3.3.0 (Sync redesign — no destructive paths)
 */
class SyncDataUseCase @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val guiaRepository: GuiaRepository,
    private val compraRepository: CompraRepository,
    private val licenciaRepository: LicenciaRepository,
    private val tiradaRepository: TiradaRepository,
    private val billingRepository: BillingRepository,
    private val firebaseAuthRepository: FirebaseAuthRepository,
    private val firebaseFormatMigrator: FirebaseFormatMigrator,
    private val crashlytics: FirebaseCrashlytics
) {

    companion object {
        private const val TAG = "SyncDataUseCase"
    }

    fun canSync(): Boolean = firebaseAuthRepository.isAuthenticated()

    fun isLinked(): Boolean = firebaseAuthRepository.isLinked()

    fun getCurrentUserId(): String? = firebaseAuthRepository.getCurrentUser()?.uid

    /**
     * Pulls each collection from Firebase and applies a non-destructive
     * merge. Returns per-entity results and any parse errors observed.
     */
    suspend fun syncFromFirebase(userId: String): Result<SyncResult> = coroutineScope {
        try {
            launch { firebaseFormatMigrator.migrateIfNeeded(userId) }
            launch { billingRepository.syncAdsStatus(userId) }

            val guiasDeferred = async { guiaRepository.syncFromFirebase(userId) }
            val comprasDeferred = async { compraRepository.syncFromFirebase(userId) }
            val licenciasDeferred = async { licenciaRepository.syncFromFirebase(userId) }
            val tiradasDeferred = async { tiradaRepository.syncFromFirebase(userId) }

            val guiasResult = guiasDeferred.await()
            val comprasResult = comprasDeferred.await()
            val licenciasResult = licenciasDeferred.await()
            val tiradasResult = tiradasDeferred.await()

            val successCount = listOf(guiasResult, comprasResult, licenciasResult, tiradasResult)
                .count { it.isSuccess }

            Log.i(TAG, "Sync from Firebase: $successCount/4 successful")
            listOf(
                "guias" to guiasResult,
                "compras" to comprasResult,
                "licencias" to licenciasResult,
                "tiradas" to tiradasResult
            ).forEach { (label, result) ->
                if (!result.isSuccess) {
                    Log.e(TAG, "FAILED: $label - ${result.exceptionOrNull()?.message}")
                }
            }

            val allParseErrors = listOfNotNull(
                guiasResult.getOrNull()?.parseErrors,
                comprasResult.getOrNull()?.parseErrors,
                licenciasResult.getOrNull()?.parseErrors,
                tiradasResult.getOrNull()?.parseErrors
            ).flatten()

            if (allParseErrors.isNotEmpty()) {
                Log.w(TAG, "Total parse errors: ${allParseErrors.size}")
                allParseErrors.forEach { error ->
                    Log.w(TAG, "  - ${error.entity}[${error.itemKey}].${error.failedField}: ${error.errorType}")
                }
            }

            // Trigger outbox drain so any pending local writes propagate now
            // that we know there's a usable network and an authenticated user.
            SyncOutboxWorker.enqueueOneShot(appContext)

            Result.success(SyncResult(
                guiasSuccess = guiasResult.isSuccess,
                comprasSuccess = comprasResult.isSuccess,
                licenciasSuccess = licenciasResult.isSuccess,
                tiradasSuccess = tiradasResult.isSuccess,
                guiasSyncResult = guiasResult.getOrNull(),
                comprasSyncResult = comprasResult.getOrNull(),
                licenciasSyncResult = licenciasResult.getOrNull(),
                tiradasSyncResult = tiradasResult.getOrNull()
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing from Firebase", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Compatibility alias kept so existing callers (MainViewModel) keep
     * working. v3.3+ never auto-fixes by re-uploading Room; the worker
     * handles upload. Returns success with `autoFixApplied=false`.
     */
    suspend fun syncFromFirebaseWithAutoFix(userId: String): Result<SyncResultWithAutoFix> {
        return syncFromFirebase(userId).map { downloadResult ->
            SyncResultWithAutoFix(
                downloadResult = downloadResult,
                autoFixApplied = false,
                entitiesFixed = emptyList(),
                uploadResult = null
            )
        }
    }

    /**
     * Triggers a one-shot drain of the [sync_outbox]. Kept for compatibility
     * with the MainViewModel "manual sync" button. Never performs the
     * destructive setValue(map) of v3.2.x.
     */
    suspend fun syncToFirebase(userId: String): Result<SyncResult> {
        SyncOutboxWorker.enqueueOneShot(appContext)
        return Result.success(SyncResult(
            guiasSuccess = true,
            comprasSuccess = true,
            licenciasSuccess = true,
            tiradasSuccess = true
        ))
    }

    suspend fun syncBidirectional(): Result<BidirectionalSyncResult> {
        if (!canSync()) {
            Log.w(TAG, "Cannot sync: user not authenticated")
            return Result.success(BidirectionalSyncResult(null, null, true, "User not authenticated"))
        }

        val userId = getCurrentUserId()
            ?: return Result.failure(Exception("No user ID available"))

        return try {
            val downloadResult = syncFromFirebase(userId).getOrNull()
            Result.success(BidirectionalSyncResult(downloadResult, null, false, null))
        } catch (e: Exception) {
            Log.e(TAG, "Smart sync failed", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    suspend fun autoSync(): Result<BidirectionalSyncResult> {
        if (!isLinked()) {
            return Result.success(BidirectionalSyncResult(null, null, true, "Auto-sync only for linked accounts"))
        }
        return syncBidirectional()
    }

    data class SyncResult(
        val guiasSuccess: Boolean,
        val comprasSuccess: Boolean,
        val licenciasSuccess: Boolean,
        val tiradasSuccess: Boolean,
        val guiasSyncResult: SyncResultWithErrors? = null,
        val comprasSyncResult: SyncResultWithErrors? = null,
        val licenciasSyncResult: SyncResultWithErrors? = null,
        val tiradasSyncResult: SyncResultWithErrors? = null
    ) {
        val allSuccess: Boolean
            get() = guiasSuccess && comprasSuccess && licenciasSuccess && tiradasSuccess

        val successCount: Int
            get() = listOf(guiasSuccess, comprasSuccess, licenciasSuccess, tiradasSuccess).count { it }

        val allParseErrors: List<ParseError>
            get() = listOfNotNull(
                guiasSyncResult?.parseErrors,
                comprasSyncResult?.parseErrors,
                licenciasSyncResult?.parseErrors,
                tiradasSyncResult?.parseErrors
            ).flatten()

        val hasParseErrors: Boolean
            get() = allParseErrors.isNotEmpty()

        /** Always false in v3.3+: auto-fix has been removed. */
        @Deprecated("auto-fix removed in v3.3", ReplaceWith("false"))
        val needsAutoFix: Boolean
            get() = false
    }

    data class SyncResultWithAutoFix(
        val downloadResult: SyncResult,
        val autoFixApplied: Boolean,
        val entitiesFixed: List<String>,
        val uploadResult: SyncResult?
    ) {
        val allSuccess: Boolean
            get() = downloadResult.allSuccess

        val hasParseErrors: Boolean
            get() = downloadResult.hasParseErrors

        val parseErrorCount: Int
            get() = downloadResult.allParseErrors.size
    }

    data class BidirectionalSyncResult(
        val downloadResult: SyncResult?,
        val uploadResult: SyncResult?,
        val skipped: Boolean,
        val reason: String?
    ) {
        val allSuccess: Boolean
            get() = !skipped && (downloadResult?.allSuccess ?: false)

        val totalSuccessCount: Int
            get() = (downloadResult?.successCount ?: 0) + (uploadResult?.successCount ?: 0)
    }
}
