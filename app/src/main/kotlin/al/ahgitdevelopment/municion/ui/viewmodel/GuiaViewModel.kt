package al.ahgitdevelopment.municion.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.repository.GuiaRepository
import al.ahgitdevelopment.municion.data.repository.ImageRepository
import al.ahgitdevelopment.municion.data.repository.LicenciaRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel para Guías
 *
 * FASE 3: ViewModels con Hilt
 *
 * @since v3.0.0 (TRACK B Modernization)
 * @since v3.2.2 (Image Upload Feature)
 */
@HiltViewModel
class GuiaViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val licenciaRepository: LicenciaRepository,
    private val imageRepository: ImageRepository,
    private val firebaseAuth: FirebaseAuth,
    private val crashlytics: FirebaseCrashlytics
) : ViewModel() {

    val guias: StateFlow<List<Guia>> = guiaRepository.guias
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Licencias disponibles para seleccionar al crear una Guía
     */
    val licencias: StateFlow<List<Licencia>> = licenciaRepository.licencias
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _uiState = MutableStateFlow<GuiaUiState>(GuiaUiState.Idle)
    val uiState: StateFlow<GuiaUiState> = _uiState.asStateFlow()

    /** Uri temporal de la imagen seleccionada (antes de subir) */
    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    /** Progreso de subida (0.0 - 1.0) */
    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()

    /**
     * Establece la Uri de la imagen seleccionada temporalmente
     */
    fun setSelectedImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    /**
     * Guarda una Guía SIN imagen
     */
    fun saveGuia(guia: Guia) {
        viewModelScope.launch {
            _uiState.value = GuiaUiState.Loading
            try {
                val userId = firebaseAuth.currentUser?.uid
                guiaRepository.saveGuia(guia, userId).getOrThrow()
                _uiState.value = GuiaUiState.Success("Guía guardada")
            } catch (e: Exception) {
                crashlytics.recordException(e)
                _uiState.value = GuiaUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Guarda una Guía CON imagen
     *
     * Flujo:
     * 1. Sube la imagen a Firebase Storage
     * 2. Actualiza la Guía con fotoUrl y storagePath
     * 3. Guarda la Guía en Room + Firebase Database
     *
     * @param guia Guía a guardar (puede tener id=0 si es nueva)
     * @param imageUri Uri local de la imagen a subir
     */
    fun saveGuiaWithImage(guia: Guia, imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = GuiaUiState.Uploading(0f)
            try {
                val userId = firebaseAuth.currentUser?.uid
                    ?: throw IllegalStateException("Usuario no autenticado")

                // Generar ID único para la imagen (usar id existente o generar UUID)
                val armaId = if (guia.id > 0) guia.id.toString() else UUID.randomUUID().toString()

                _uploadProgress.value = 0.3f
                _uiState.value = GuiaUiState.Uploading(0.3f)

                // 1. Subir imagen a Storage
                val uploadResult = imageRepository.uploadGuiaImage(
                    uri = imageUri,
                    userId = userId,
                    armaId = armaId
                ).getOrThrow()

                _uploadProgress.value = 0.7f
                _uiState.value = GuiaUiState.Uploading(0.7f)

                // 2. Actualizar Guía con URLs
                val guiaWithImage = guia.copy(
                    fotoUrl = uploadResult.downloadUrl,
                    storagePath = uploadResult.storagePath
                )

                // 3. Guardar en Room + Firebase Database
                guiaRepository.saveGuia(guiaWithImage, userId).getOrThrow()

                _uploadProgress.value = 1f
                _selectedImageUri.value = null
                _uiState.value = GuiaUiState.Success("Guía guardada con imagen")

            } catch (e: Exception) {
                crashlytics.recordException(e)
                _uploadProgress.value = 0f
                _uiState.value = GuiaUiState.Error(e.message ?: "Error al subir imagen")
            }
        }
    }

    /**
     * Actualiza una Guía SIN cambiar la imagen
     */
    fun updateGuia(guia: Guia) {
        viewModelScope.launch {
            _uiState.value = GuiaUiState.Loading
            try {
                val userId = firebaseAuth.currentUser?.uid
                guiaRepository.updateGuia(guia, userId).getOrThrow()
                _uiState.value = GuiaUiState.Success("Guía actualizada")
            } catch (e: Exception) {
                crashlytics.recordException(e)
                _uiState.value = GuiaUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Actualiza una Guía CON nueva imagen
     *
     * Si la guía tenía imagen previa, la elimina de Storage
     *
     * @param guia Guía a actualizar
     * @param newImageUri Uri de la nueva imagen
     */
    fun updateGuiaWithImage(guia: Guia, newImageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = GuiaUiState.Uploading(0f)
            try {
                val userId = firebaseAuth.currentUser?.uid
                    ?: throw IllegalStateException("Usuario no autenticado")

                _uploadProgress.value = 0.1f

                // 1. Si tenía imagen previa, eliminarla
                guia.storagePath?.let { oldPath ->
                    if (oldPath.isNotBlank()) {
                        imageRepository.deleteGuiaImage(oldPath)
                        // No fallar si no se puede borrar la vieja
                    }
                }

                _uploadProgress.value = 0.3f
                _uiState.value = GuiaUiState.Uploading(0.3f)

                // 2. Subir nueva imagen
                val uploadResult = imageRepository.uploadGuiaImage(
                    uri = newImageUri,
                    userId = userId,
                    armaId = guia.id.toString()
                ).getOrThrow()

                _uploadProgress.value = 0.7f
                _uiState.value = GuiaUiState.Uploading(0.7f)

                // 3. Actualizar Guía con nueva URL
                val guiaWithImage = guia.copy(
                    fotoUrl = uploadResult.downloadUrl,
                    storagePath = uploadResult.storagePath
                )

                // 4. Guardar en Room + Firebase Database
                guiaRepository.updateGuia(guiaWithImage, userId).getOrThrow()

                _uploadProgress.value = 1f
                _selectedImageUri.value = null
                _uiState.value = GuiaUiState.Success("Guía actualizada con imagen")

            } catch (e: Exception) {
                crashlytics.recordException(e)
                _uploadProgress.value = 0f
                _uiState.value = GuiaUiState.Error(e.message ?: "Error al actualizar imagen")
            }
        }
    }

    /**
     * Elimina una Guía y su imagen asociada de Storage
     */
    fun deleteGuia(guia: Guia) {
        viewModelScope.launch {
            _uiState.value = GuiaUiState.Loading
            try {
                val userId = firebaseAuth.currentUser?.uid

                // 1. Eliminar imagen de Storage si existe
                guia.storagePath?.let { storagePath ->
                    if (storagePath.isNotBlank()) {
                        imageRepository.deleteGuiaImage(storagePath)
                        // No fallar si no se puede borrar la imagen
                    }
                }

                // 2. Eliminar de Room + Firebase Database
                guiaRepository.deleteGuia(guia, userId).getOrThrow()
                _uiState.value = GuiaUiState.Success("Guía eliminada")

            } catch (e: Exception) {
                crashlytics.recordException(e)
                _uiState.value = GuiaUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Elimina solo la imagen de una Guía (sin eliminar la Guía)
     */
    fun removeGuiaImage(guia: Guia) {
        viewModelScope.launch {
            _uiState.value = GuiaUiState.Loading
            try {
                val userId = firebaseAuth.currentUser?.uid

                // 1. Eliminar de Storage
                guia.storagePath?.let { storagePath ->
                    if (storagePath.isNotBlank()) {
                        imageRepository.deleteGuiaImage(storagePath).getOrThrow()
                    }
                }

                // 2. Actualizar Guía sin imagen
                val guiaWithoutImage = guia.copy(
                    fotoUrl = null,
                    storagePath = null
                )
                guiaRepository.updateGuia(guiaWithoutImage, userId).getOrThrow()

                _selectedImageUri.value = null
                _uiState.value = GuiaUiState.Success("Imagen eliminada")

            } catch (e: Exception) {
                crashlytics.recordException(e)
                _uiState.value = GuiaUiState.Error(e.message ?: "Error al eliminar imagen")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = GuiaUiState.Idle
        _uploadProgress.value = 0f
    }

    sealed class GuiaUiState {
        data object Idle : GuiaUiState()
        data object Loading : GuiaUiState()
        data class Uploading(val progress: Float) : GuiaUiState()
        data class Success(val message: String) : GuiaUiState()
        data class Error(val message: String) : GuiaUiState()
    }
}
