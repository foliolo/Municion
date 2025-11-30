package al.ahgitdevelopment.municion.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import al.ahgitdevelopment.municion.domain.usecase.SyncDataUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MainViewModel para sincronizacion de datos con Firebase.
 *
 * v3.4.0: Auth Simplification
 * - Eliminado: checkAuthState, attemptFirebaseRecovery, signInAnonymously
 * - La autenticacion ahora es manejada por AuthViewModel en MainActivity
 * - Este ViewModel solo maneja sincronizacion de datos
 *
 * @since v3.0.0 (TRACK B Modernization)
 * @updated v3.4.0 (Auth Simplification)
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val syncDataUseCase: SyncDataUseCase,
    private val crashlytics: FirebaseCrashlytics
) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    /**
     * Inicia sincronizacion automatica si hay usuario autenticado.
     * Llamado desde MainActivity despues de verificar autenticacion.
     */
    fun initSync() {
        if (currentUserId != null) {
            syncFromFirebase()
        }
    }

    fun syncFromFirebase() {
        val userId = currentUserId ?: run {
            Log.w(TAG, "syncFromFirebase: No userId available")
            return
        }

        viewModelScope.launch {
            Log.i(TAG, "Starting syncFromFirebase with auto-fix for user: $userId")
            _syncState.value = SyncState.Syncing
            try {
                val result = syncDataUseCase.syncFromFirebaseWithAutoFix(userId).getOrThrow()
                val downloadResult = result.downloadResult

                // Log parse errors si los hay
                if (result.hasParseErrors) {
                    Log.w(TAG, "Sync had ${result.parseErrorCount} parse errors")
                    downloadResult.allParseErrors.forEach { error ->
                        Log.w(TAG, "  Parse error: ${error.entity}[${error.itemKey}].${error.failedField}: ${error.errorType}")
                    }
                }

                // Log auto-fix si se aplicó
                if (result.autoFixApplied) {
                    Log.i(TAG, "Auto-fix applied for: ${result.entitiesFixed.joinToString()}")
                }

                _syncState.value = when {
                    result.allSuccess && !result.hasParseErrors -> {
                        Log.i(TAG, "Sync completed: ${downloadResult.successCount}/4 SUCCESS")
                        SyncState.Success(downloadResult.successCount)
                    }
                    result.allSuccess && result.hasParseErrors -> {
                        Log.w(TAG, "Sync completed with parse errors (auto-fix: ${result.autoFixApplied})")
                        SyncState.SuccessWithParseErrors(
                            count = downloadResult.successCount,
                            parseErrorCount = result.parseErrorCount,
                            autoFixApplied = result.autoFixApplied
                        )
                    }
                    else -> {
                        Log.w(TAG, "Sync partial: ${downloadResult.successCount}/4 - " +
                            "guias=${downloadResult.guiasSuccess}, compras=${downloadResult.comprasSuccess}, " +
                            "licencias=${downloadResult.licenciasSuccess}, tiradas=${downloadResult.tiradasSuccess}")
                        SyncState.PartialSuccess(downloadResult.successCount)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sync failed", e)
                crashlytics.recordException(e)
                _syncState.value = SyncState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun syncToFirebase() {
        val userId = currentUserId ?: run {
            Log.w(TAG, "syncToFirebase: No userId available")
            return
        }

        viewModelScope.launch {
            Log.i(TAG, "Starting syncToFirebase for user: $userId")
            _syncState.value = SyncState.Syncing
            try {
                val result = syncDataUseCase.syncToFirebase(userId).getOrThrow()
                _syncState.value = if (result.allSuccess) {
                    Log.i(TAG, "Upload completed: ${result.successCount}/4 SUCCESS")
                    SyncState.Success(result.successCount)
                } else {
                    Log.w(TAG, "Upload partial: ${result.successCount}/4 - " +
                        "guias=${result.guiasSuccess}, compras=${result.comprasSuccess}, " +
                        "licencias=${result.licenciasSuccess}, tiradas=${result.tiradasSuccess}")
                    SyncState.PartialSuccess(result.successCount)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Upload failed", e)
                crashlytics.recordException(e)
                _syncState.value = SyncState.Error(e.message ?: "Unknown error")
            }
        }
    }

    sealed class SyncState {
        object Idle : SyncState()
        object Syncing : SyncState()
        data class Success(val count: Int) : SyncState()
        data class SuccessWithParseErrors(
            val count: Int,
            val parseErrorCount: Int,
            val autoFixApplied: Boolean
        ) : SyncState()
        data class PartialSuccess(val count: Int) : SyncState()
        data class Error(val message: String) : SyncState()
    }
}
