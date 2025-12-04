package al.ahgitdevelopment.municion.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import al.ahgitdevelopment.municion.domain.usecase.SyncDataUseCase
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker para sincronización periódica con Firebase
 *
 * FASE 5: Background Work con WorkManager
 * - Sincroniza datos automáticamente
 * - Se ejecuta periódicamente (cada 6 horas)
 * - Requiere conectividad de red
 * - Inyección de dependencias con Hilt
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncDataUseCase: SyncDataUseCase,
    private val firebaseAuth: FirebaseAuth,
    private val crashlytics: FirebaseCrashlytics
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.i("SyncWorker", "Starting background sync...")

        return try {
            // Obtener userId
            val userId = firebaseAuth.currentUser?.uid
            if (userId == null) {
                Log.w("SyncWorker", "User not authenticated, skipping sync")
                return Result.success()
            }

            // Sync desde Firebase → Room
            val syncResult = syncDataUseCase.syncFromFirebase(userId).getOrThrow()

            if (syncResult.allSuccess) {
                Log.i("SyncWorker", "Sync completed successfully: ${syncResult.successCount}/4")
                Result.success()
            } else {
                Log.w("SyncWorker", "Sync completed with errors: ${syncResult.successCount}/4")
                // Retry si no todas las entidades se sincronizaron
                if (runAttemptCount < 3) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync failed", e)
            crashlytics.log("SyncWorker failed: ${e.message}")
            crashlytics.recordException(e)

            // Retry hasta 3 intentos
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME = "municion_sync_work"
    }
}
