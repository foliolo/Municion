package al.ahgitdevelopment.municion.domain.usecase

import al.ahgitdevelopment.municion.auth.FirebaseAuthRepository
import al.ahgitdevelopment.municion.data.repository.CompraRepository
import al.ahgitdevelopment.municion.data.repository.GuiaRepository
import al.ahgitdevelopment.municion.data.repository.LicenciaRepository
import al.ahgitdevelopment.municion.data.repository.TiradaRepository
import al.ahgitdevelopment.municion.data.sync.FirebaseFormatMigrator
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Use Case to sync ALL data with Firebase
 *
 * PHASE 5: Smart Bidirectional Sync
 * - Verifies auth state before syncing
 * - Runs FirebaseFormatMigrator (array→map) on first sync
 * - Uses smart diff-based sync (upsert newer, delete removed)
 * - Full upload only for auto-fix
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
class SyncDataUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val compraRepository: CompraRepository,
    private val licenciaRepository: LicenciaRepository,
    private val tiradaRepository: TiradaRepository,
    private val billingRepository: al.ahgitdevelopment.municion.data.repository.BillingRepository,
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
     * Syncs from Firebase -> Room (download) with smart diff.
     * Runs format migration (array→map) before first sync.
     */
    suspend fun syncFromFirebase(userId: String): Result<SyncResult> = coroutineScope {
        try {
            // Run format migration (idempotent, fast if already done)
            launch { firebaseFormatMigrator.migrateIfNeeded(userId) }

            // Trigger ads status sync independently
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
            if (!guiasResult.isSuccess) {
                Log.e(TAG, "FAILED: Guias - ${guiasResult.exceptionOrNull()?.message}")
            }
            if (!comprasResult.isSuccess) {
                Log.e(TAG, "FAILED: Compras - ${comprasResult.exceptionOrNull()?.message}")
            }
            if (!licenciasResult.isSuccess) {
                Log.e(TAG, "FAILED: Licencias - ${licenciasResult.exceptionOrNull()?.message}")
            }
            if (!tiradasResult.isSuccess) {
                Log.e(TAG, "FAILED: Tiradas - ${tiradasResult.exceptionOrNull()?.message}")
            }

            val allParseErrors = mutableListOf<ParseError>()
            guiasResult.getOrNull()?.parseErrors?.let { allParseErrors.addAll(it) }
            comprasResult.getOrNull()?.parseErrors?.let { allParseErrors.addAll(it) }
            licenciasResult.getOrNull()?.parseErrors?.let { allParseErrors.addAll(it) }
            tiradasResult.getOrNull()?.parseErrors?.let { allParseErrors.addAll(it) }

            if (allParseErrors.isNotEmpty()) {
                Log.w(TAG, "Total parse errors: ${allParseErrors.size}")
                allParseErrors.forEach { error ->
                    Log.w(TAG, "  - ${error.entity}[${error.itemKey}].${error.failedField}: ${error.errorType}")
                }
            }

            Result.success(
                SyncResult(
                    guiasSuccess = guiasResult.isSuccess,
                    comprasSuccess = comprasResult.isSuccess,
                    licenciasSuccess = licenciasResult.isSuccess,
                    tiradasSuccess = tiradasResult.isSuccess,
                    guiasSyncResult = guiasResult.getOrNull(),
                    comprasSyncResult = comprasResult.getOrNull(),
                    licenciasSyncResult = licenciasResult.getOrNull(),
                    tiradasSyncResult = tiradasResult.getOrNull()
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing from Firebase", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Syncs from Firebase with auto-fix for corrupt data.
     */
    suspend fun syncFromFirebaseWithAutoFix(userId: String): Result<SyncResultWithAutoFix> = coroutineScope {
        try {
            val downloadResult = syncFromFirebase(userId).getOrThrow()

            val entitiesNeedingFix = mutableListOf<String>()

            if (downloadResult.guiasSyncResult?.needsAutoFix == true) {
                entitiesNeedingFix.add("Guias")
            }
            if (downloadResult.comprasSyncResult?.needsAutoFix == true) {
                entitiesNeedingFix.add("Compras")
            }
            if (downloadResult.licenciasSyncResult?.needsAutoFix == true) {
                entitiesNeedingFix.add("Licencias")
            }
            if (downloadResult.tiradasSyncResult?.needsAutoFix == true) {
                entitiesNeedingFix.add("Tiradas")
            }

            var autoFixApplied = false
            var uploadResult: SyncResult? = null

            if (entitiesNeedingFix.isNotEmpty()) {
                Log.w(TAG, "Auto-fix needed for: ${entitiesNeedingFix.joinToString()}")
                crashlytics.log("Auto-fix triggered for entities: ${entitiesNeedingFix.joinToString()}")

                uploadResult = syncToFirebase(userId).getOrNull()
                autoFixApplied = uploadResult?.allSuccess == true

                if (autoFixApplied) {
                    Log.i(TAG, "Auto-fix applied successfully")
                    crashlytics.log("Auto-fix completed successfully")
                } else {
                    Log.e(TAG, "Auto-fix upload failed")
                    crashlytics.log("Auto-fix upload failed")
                }
            }

            Result.success(
                SyncResultWithAutoFix(
                    downloadResult = downloadResult,
                    autoFixApplied = autoFixApplied,
                    entitiesFixed = if (autoFixApplied) entitiesNeedingFix else emptyList(),
                    uploadResult = uploadResult
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Sync with auto-fix failed", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Full sync to Firebase (upload all). Used for auto-fix only.
     */
    suspend fun syncToFirebase(userId: String): Result<SyncResult> = coroutineScope {
        try {
            val guiasDeferred = async { guiaRepository.syncToFirebase(userId) }
            val comprasDeferred = async { compraRepository.syncToFirebase(userId) }
            val licenciasDeferred = async { licenciaRepository.syncToFirebase(userId) }
            val tiradasDeferred = async { tiradaRepository.syncToFirebase(userId) }

            val guiasResult = guiasDeferred.await()
            val comprasResult = comprasDeferred.await()
            val licenciasResult = licenciasDeferred.await()
            val tiradasResult = tiradasDeferred.await()

            val successCount = listOf(guiasResult, comprasResult, licenciasResult, tiradasResult)
                .count { it.isSuccess }

            Log.i(TAG, "Full sync to Firebase: $successCount/4 successful")
            if (!guiasResult.isSuccess) {
                Log.e(TAG, "FAILED upload: Guias - ${guiasResult.exceptionOrNull()?.message}")
            }
            if (!comprasResult.isSuccess) {
                Log.e(TAG, "FAILED upload: Compras - ${comprasResult.exceptionOrNull()?.message}")
            }
            if (!licenciasResult.isSuccess) {
                Log.e(TAG, "FAILED upload: Licencias - ${licenciasResult.exceptionOrNull()?.message}")
            }
            if (!tiradasResult.isSuccess) {
                Log.e(TAG, "FAILED upload: Tiradas - ${tiradasResult.exceptionOrNull()?.message}")
            }

            Result.success(
                SyncResult(
                    guiasSuccess = guiasResult.isSuccess,
                    comprasSuccess = comprasResult.isSuccess,
                    licenciasSuccess = licenciasResult.isSuccess,
                    tiradasSuccess = tiradasResult.isSuccess
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing to Firebase", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Smart initial sync (download only, upload is per-entity on save/update/delete)
     */
    suspend fun syncBidirectional(): Result<BidirectionalSyncResult> {
        if (!canSync()) {
            Log.w(TAG, "Cannot sync: user not authenticated")
            return Result.success(
                BidirectionalSyncResult(
                    downloadResult = null,
                    uploadResult = null,
                    skipped = true,
                    reason = "User not authenticated"
                )
            )
        }

        val userId = getCurrentUserId()
        if (userId == null) {
            Log.w(TAG, "Cannot sync: no user ID")
            return Result.failure(Exception("No user ID available"))
        }

        return try {
            Log.i(TAG, "Starting smart sync for user: $userId")

            val downloadResult = syncFromFirebase(userId).getOrNull()
            Log.i(TAG, "Download completed: ${downloadResult?.successCount ?: 0}/4")

            Result.success(
                BidirectionalSyncResult(
                    downloadResult = downloadResult,
                    uploadResult = null,
                    skipped = false,
                    reason = null
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Smart sync failed", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    suspend fun autoSync(): Result<BidirectionalSyncResult> {
        if (!isLinked()) {
            Log.d(TAG, "Auto-sync skipped: user is anonymous")
            return Result.success(
                BidirectionalSyncResult(
                    downloadResult = null,
                    uploadResult = null,
                    skipped = true,
                    reason = "Auto-sync only for linked accounts"
                )
            )
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
            get() = listOf(guiasSuccess, comprasSuccess, licenciasSuccess, tiradasSuccess)
                .count { it }

        val allParseErrors: List<ParseError>
            get() = listOfNotNull(
                guiasSyncResult?.parseErrors,
                comprasSyncResult?.parseErrors,
                licenciasSyncResult?.parseErrors,
                tiradasSyncResult?.parseErrors
            ).flatten()

        val hasParseErrors: Boolean
            get() = allParseErrors.isNotEmpty()

        val needsAutoFix: Boolean
            get() = guiasSyncResult?.needsAutoFix == true ||
                    comprasSyncResult?.needsAutoFix == true ||
                    licenciasSyncResult?.needsAutoFix == true ||
                    tiradasSyncResult?.needsAutoFix == true
    }

    data class SyncResultWithAutoFix(
        val downloadResult: SyncResult,
        val autoFixApplied: Boolean,
        val entitiesFixed: List<String>,
        val uploadResult: SyncResult?
    ) {
        val allSuccess: Boolean
            get() = downloadResult.allSuccess && (!autoFixApplied || uploadResult?.allSuccess == true)

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
            get() = !skipped &&
                    (downloadResult?.allSuccess ?: false) &&
                    (uploadResult?.allSuccess ?: false)

        val totalSuccessCount: Int
            get() = (downloadResult?.successCount ?: 0) + (uploadResult?.successCount ?: 0)
    }
}
