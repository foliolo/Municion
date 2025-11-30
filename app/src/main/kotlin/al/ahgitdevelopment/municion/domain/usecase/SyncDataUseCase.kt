package al.ahgitdevelopment.municion.domain.usecase

import al.ahgitdevelopment.municion.auth.FirebaseAuthRepository
import al.ahgitdevelopment.municion.data.repository.CompraRepository
import al.ahgitdevelopment.municion.data.repository.GuiaRepository
import al.ahgitdevelopment.municion.data.repository.LicenciaRepository
import al.ahgitdevelopment.municion.data.repository.TiradaRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * Use Case para sincronizar TODOS los datos con Firebase
 *
 * FASE 5: Sync Bidireccional Inteligente
 * - Verifica estado de autenticación antes de sincronizar
 * - Solo sincroniza si el usuario está autenticado
 * - Usuarios anónimos: sync bidireccional (local-first)
 * - Usuarios vinculados: sync completo con prioridad cloud
 * - Manejo de errores individual por entidad
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
class SyncDataUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val compraRepository: CompraRepository,
    private val licenciaRepository: LicenciaRepository,
    private val tiradaRepository: TiradaRepository,
    private val firebaseAuthRepository: FirebaseAuthRepository,
    private val crashlytics: FirebaseCrashlytics
) {

    companion object {
        private const val TAG = "SyncDataUseCase"
    }

    /**
     * Verifica si el usuario puede sincronizar
     * @return true si está autenticado (anónimo o vinculado)
     */
    fun canSync(): Boolean = firebaseAuthRepository.isAuthenticated()

    /**
     * Verifica si el usuario tiene cuenta vinculada (no anónima)
     * @return true si está vinculado con Google/Email
     */
    fun isLinked(): Boolean = firebaseAuthRepository.isLinked()

    /**
     * Obtiene el ID del usuario actual
     */
    fun getCurrentUserId(): String? = firebaseAuthRepository.getCurrentUser()?.uid

    /**
     * Sincroniza desde Firebase → Room (download)
     * Ahora retorna información detallada de errores de parseo por entidad
     */
    suspend fun syncFromFirebase(userId: String): Result<SyncResult> = coroutineScope {
        try {
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

            // Log detallado de qué entidad falló
            android.util.Log.i(TAG, "Sync from Firebase: $successCount/4 successful")
            if (!guiasResult.isSuccess) {
                android.util.Log.e(TAG, "FAILED: Guias - ${guiasResult.exceptionOrNull()?.message}")
            }
            if (!comprasResult.isSuccess) {
                android.util.Log.e(TAG, "FAILED: Compras - ${comprasResult.exceptionOrNull()?.message}")
            }
            if (!licenciasResult.isSuccess) {
                android.util.Log.e(TAG, "FAILED: Licencias - ${licenciasResult.exceptionOrNull()?.message}")
            }
            if (!tiradasResult.isSuccess) {
                android.util.Log.e(TAG, "FAILED: Tiradas - ${tiradasResult.exceptionOrNull()?.message}")
            }

            // Recopilar todos los errores de parseo
            val allParseErrors = mutableListOf<ParseError>()
            guiasResult.getOrNull()?.parseErrors?.let { allParseErrors.addAll(it) }
            comprasResult.getOrNull()?.parseErrors?.let { allParseErrors.addAll(it) }
            licenciasResult.getOrNull()?.parseErrors?.let { allParseErrors.addAll(it) }
            tiradasResult.getOrNull()?.parseErrors?.let { allParseErrors.addAll(it) }

            if (allParseErrors.isNotEmpty()) {
                android.util.Log.w(TAG, "Total parse errors: ${allParseErrors.size}")
                allParseErrors.forEach { error ->
                    android.util.Log.w(TAG, "  - ${error.entity}[${error.itemKey}].${error.failedField}: ${error.errorType}")
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
            android.util.Log.e("SyncDataUseCase", "Error syncing from Firebase", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Sincroniza desde Firebase → Room con auto-corrección
     *
     * Si Firebase tiene errores de parseo pero Room tiene datos válidos,
     * automáticamente sube los datos de Room a Firebase para corregir
     * los datos corruptos.
     *
     * @return SyncResultWithAutoFix con detalles de la sincronización y auto-fix
     */
    suspend fun syncFromFirebaseWithAutoFix(userId: String): Result<SyncResultWithAutoFix> = coroutineScope {
        try {
            // Paso 1: Intentar sincronizar desde Firebase
            val downloadResult = syncFromFirebase(userId).getOrThrow()

            // Paso 2: Verificar si necesita auto-fix por entidad
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

            // Paso 3: Aplicar auto-fix si es necesario
            var autoFixApplied = false
            var uploadResult: SyncResult? = null

            if (entitiesNeedingFix.isNotEmpty()) {
                android.util.Log.w(TAG, "Auto-fix needed for: ${entitiesNeedingFix.joinToString()}")
                crashlytics.log("Auto-fix triggered for entities: ${entitiesNeedingFix.joinToString()}")

                // Subir datos de Room a Firebase para corregir
                uploadResult = syncToFirebase(userId).getOrNull()
                autoFixApplied = uploadResult?.allSuccess == true

                if (autoFixApplied) {
                    android.util.Log.i(TAG, "Auto-fix applied successfully")
                    crashlytics.log("Auto-fix completed successfully")
                } else {
                    android.util.Log.e(TAG, "Auto-fix upload failed")
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
            android.util.Log.e(TAG, "Sync with auto-fix failed", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Sincroniza hacia Firebase ← Room (upload)
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

            // Log detallado de qué entidad falló
            android.util.Log.i(TAG, "Sync to Firebase: $successCount/4 successful")
            if (!guiasResult.isSuccess) {
                android.util.Log.e(TAG, "FAILED upload: Guias - ${guiasResult.exceptionOrNull()?.message}")
            }
            if (!comprasResult.isSuccess) {
                android.util.Log.e(TAG, "FAILED upload: Compras - ${comprasResult.exceptionOrNull()?.message}")
            }
            if (!licenciasResult.isSuccess) {
                android.util.Log.e(TAG, "FAILED upload: Licencias - ${licenciasResult.exceptionOrNull()?.message}")
            }
            if (!tiradasResult.isSuccess) {
                android.util.Log.e(TAG, "FAILED upload: Tiradas - ${tiradasResult.exceptionOrNull()?.message}")
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
            android.util.Log.e("SyncDataUseCase", "Error syncing to Firebase", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Sincronización inicial inteligente
     *
     * Estrategia:
     * - Si el usuario NO está autenticado: no sincroniza (solo local)
     * - Si el usuario está autenticado (anónimo o vinculado):
     *   1. Descarga de Firebase → Room (prioridad Cloud para evitar sobrescribir datos)
     *   2. La subida (Upload) se realiza solo de manera reactiva en los repositorios (Save/Update/Delete)
     *      para evitar que un estado local vacío sobrescriba los datos remotos ("Last Write Wins").
     *
     * @return BidirectionalSyncResult con detalles de la descarga
     */
    suspend fun syncBidirectional(): Result<BidirectionalSyncResult> {
        // Verificar autenticación
        if (!canSync()) {
            android.util.Log.w(TAG, "Cannot sync: user not authenticated")
            return Result.success(
                BidirectionalSyncResult(
                    downloadResult = null,
                    uploadResult = null,
                    skipped = true,
                    reason = "Usuario no autenticado"
                )
            )
        }

        val userId = getCurrentUserId()
        if (userId == null) {
            android.util.Log.w(TAG, "Cannot sync: no user ID")
            return Result.failure(Exception("No user ID available"))
        }

        return try {
            android.util.Log.i(TAG, "Starting smart sync for user: $userId")

            // Paso 1: Descargar de Firebase → Room
            val downloadResult = syncFromFirebase(userId).getOrNull()
            android.util.Log.i(TAG, "Download completed: ${downloadResult?.successCount ?: 0}/4")

            // NOTA: Eliminamos el "Paso 2: Subir" automático para evitar data loss.
            // La subida se delega a las operaciones individuales (save/update/delete)
            // que ahora hacen un "Fetch-Merge-Push" seguro.

            Result.success(
                BidirectionalSyncResult(
                    downloadResult = downloadResult,
                    uploadResult = null, // No upload in auto-sync
                    skipped = false,
                    reason = null
                )
            )
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Smart sync failed", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Sincronización automática al iniciar/pausar la app
     * Solo sincroniza si hay conexión y el usuario está autenticado
     */
    suspend fun autoSync(): Result<BidirectionalSyncResult> {
        // Solo usuarios vinculados sincronizan automáticamente
        // Usuarios anónimos deben sincronizar manualmente para evitar
        // sobrescribir datos accidentalmente
        if (!isLinked()) {
            android.util.Log.d(TAG, "Auto-sync skipped: user is anonymous")
            return Result.success(
                BidirectionalSyncResult(
                    downloadResult = null,
                    uploadResult = null,
                    skipped = true,
                    reason = "Auto-sync solo para cuentas vinculadas"
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

        /**
         * Todos los errores de parseo de todas las entidades
         */
        val allParseErrors: List<ParseError>
            get() = listOfNotNull(
                guiasSyncResult?.parseErrors,
                comprasSyncResult?.parseErrors,
                licenciasSyncResult?.parseErrors,
                tiradasSyncResult?.parseErrors
            ).flatten()

        /**
         * Indica si hubo errores de parseo
         */
        val hasParseErrors: Boolean
            get() = allParseErrors.isNotEmpty()

        /**
         * Indica si alguna entidad necesita auto-fix
         */
        val needsAutoFix: Boolean
            get() = guiasSyncResult?.needsAutoFix == true ||
                    comprasSyncResult?.needsAutoFix == true ||
                    licenciasSyncResult?.needsAutoFix == true ||
                    tiradasSyncResult?.needsAutoFix == true
    }

    /**
     * Resultado de sincronización con auto-corrección
     */
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
