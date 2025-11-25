package al.ahgitdevelopment.municion.ui.viewmodel

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
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * MainViewModel para estado compartido de la app
 *
 * FASE 3: ViewModels con Hilt
 * - Maneja autenticación Firebase
 * - Sincronización con Firebase
 * - Estado global de la app
 *
 * @since v3.0.0 (TRACK B Modernization)
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

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            val user = firebaseAuth.currentUser
            if (user != null) {
                _uiState.value = MainUiState.Authenticated(user.uid)
                // Auto-sync on startup
                syncFromFirebase()
            } else {
                // DEFENSA: Intentar crear usuario anónimo en lugar de cerrar la app
                android.util.Log.w(TAG, "No Firebase user found - attempting recovery")
                attemptFirebaseRecovery()
            }
        }
    }

    /**
     * Intenta recuperar la autenticación de Firebase creando un usuario anónimo.
     * Si falla, navega a Login en lugar de cerrar la app abruptamente.
     */
    private suspend fun attemptFirebaseRecovery() {
        try {
            _uiState.value = MainUiState.Loading
            val result = firebaseAuth.signInAnonymously().await()
            result.user?.let { user ->
                android.util.Log.i(TAG, "Recovery successful: ${user.uid}")
                _uiState.value = MainUiState.Authenticated(user.uid)
                syncFromFirebase()
            } ?: run {
                android.util.Log.e(TAG, "Recovery failed: user is null")
                _uiState.value = MainUiState.Unauthenticated
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Recovery failed", e)
            crashlytics.recordException(e)
            _uiState.value = MainUiState.Unauthenticated
        }
    }

    fun syncFromFirebase() {
        val userId = currentUserId ?: run {
            android.util.Log.w(TAG, "syncFromFirebase: No userId available")
            return
        }

        viewModelScope.launch {
            android.util.Log.i(TAG, "Starting syncFromFirebase with auto-fix for user: $userId")
            _syncState.value = SyncState.Syncing
            try {
                val result = syncDataUseCase.syncFromFirebaseWithAutoFix(userId).getOrThrow()
                val downloadResult = result.downloadResult

                // Log parse errors si los hay
                if (result.hasParseErrors) {
                    android.util.Log.w(TAG, "Sync had ${result.parseErrorCount} parse errors")
                    downloadResult.allParseErrors.forEach { error ->
                        android.util.Log.w(TAG, "  Parse error: ${error.entity}[${error.itemKey}].${error.failedField}: ${error.errorType}")
                    }
                }

                // Log auto-fix si se aplicó
                if (result.autoFixApplied) {
                    android.util.Log.i(TAG, "Auto-fix applied for: ${result.entitiesFixed.joinToString()}")
                }

                _syncState.value = when {
                    result.allSuccess && !result.hasParseErrors -> {
                        android.util.Log.i(TAG, "Sync completed: ${downloadResult.successCount}/4 SUCCESS")
                        SyncState.Success(downloadResult.successCount)
                    }
                    result.allSuccess && result.hasParseErrors -> {
                        android.util.Log.w(TAG, "Sync completed with parse errors (auto-fix: ${result.autoFixApplied})")
                        SyncState.SuccessWithParseErrors(
                            count = downloadResult.successCount,
                            parseErrorCount = result.parseErrorCount,
                            autoFixApplied = result.autoFixApplied
                        )
                    }
                    else -> {
                        android.util.Log.w(TAG, "Sync partial: ${downloadResult.successCount}/4 - " +
                            "guias=${downloadResult.guiasSuccess}, compras=${downloadResult.comprasSuccess}, " +
                            "licencias=${downloadResult.licenciasSuccess}, tiradas=${downloadResult.tiradasSuccess}")
                        SyncState.PartialSuccess(downloadResult.successCount)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Sync failed", e)
                crashlytics.recordException(e)
                _syncState.value = SyncState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun syncToFirebase() {
        val userId = currentUserId ?: run {
            android.util.Log.w(TAG, "syncToFirebase: No userId available")
            return
        }

        viewModelScope.launch {
            android.util.Log.i(TAG, "Starting syncToFirebase for user: $userId")
            _syncState.value = SyncState.Syncing
            try {
                val result = syncDataUseCase.syncToFirebase(userId).getOrThrow()
                _syncState.value = if (result.allSuccess) {
                    android.util.Log.i(TAG, "Upload completed: ${result.successCount}/4 SUCCESS")
                    SyncState.Success(result.successCount)
                } else {
                    android.util.Log.w(TAG, "Upload partial: ${result.successCount}/4 - " +
                        "guias=${result.guiasSuccess}, compras=${result.comprasSuccess}, " +
                        "licencias=${result.licenciasSuccess}, tiradas=${result.tiradasSuccess}")
                    SyncState.PartialSuccess(result.successCount)
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Upload failed", e)
                crashlytics.recordException(e)
                _syncState.value = SyncState.Error(e.message ?: "Unknown error")
            }
        }
    }

    sealed class MainUiState {
        object Loading : MainUiState()
        object Unauthenticated : MainUiState()
        data class Authenticated(val userId: String) : MainUiState()
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
