package al.ahgitdevelopment.municion.ui.components.imagepicker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import al.ahgitdevelopment.municion.R
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import java.io.File

/**
 * Estado del selector de imagen.
 */
data class ImagePickerState(
    val isProcessing: Boolean = false,
    val showSourceDialog: Boolean = false,
    val showPermissionRationale: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Componente completo para selección de imagen desde cámara o galería.
 * 
 * Características:
 * - Selección desde galería usando Photo Picker (Android 13+) con fallback
 * - Captura desde cámara con permisos en runtime
 * - Corrección automática de orientación EXIF
 * - Compresión y redimensionado
 * - Manejo de estados de carga y errores
 * 
 * @param currentImageUrl URL o Uri de la imagen actual (puede ser local o remota)
 * @param isUploading Si está en proceso de subida a servidor
 * @param uploadProgress Progreso de subida (0.0 - 1.0)
 * @param onImageSelected Callback cuando se selecciona una imagen (Uri procesada)
 * @param onImageRemoved Callback cuando se elimina la imagen
 * @param label Etiqueta mostrada sobre el selector
 * @param imageSize Tamaño del contenedor de imagen
 * 
 * @since v3.2.2
 */
@Composable
fun ImagePickerWithCamera(
    currentImageUrl: String?,
    isUploading: Boolean,
    uploadProgress: Float,
    onImageSelected: (Uri) -> Unit,
    onImageRemoved: () -> Unit,
    label: String = stringResource(R.string.label_weapon_photo),
    imageSize: Int = 180,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var state by remember { mutableStateOf(ImagePickerState()) }
    var tempCameraFile by rememberSaveable { mutableStateOf<String?>(null) }
    
    // Limpiar archivos temporales antiguos al montar
    LaunchedEffect(Unit) {
        ImageUtils.cleanupOldTempFiles(context)
    }
    
    // Launcher para galería (Photo Picker)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { selectedUri ->
            scope.launch {
                state = state.copy(isProcessing = true)
                val processedUri = ImageUtils.processAndCorrectOrientation(context, selectedUri)
                state = state.copy(isProcessing = false)
                
                if (processedUri != null) {
                    onImageSelected(processedUri)
                } else {
                    state = state.copy(errorMessage = context.getString(R.string.error_processing_image))
                }
            }
        }
    }
    
    // Launcher para cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraFile != null) {
            scope.launch {
                state = state.copy(isProcessing = true)
                val fileUri = Uri.fromFile(File(tempCameraFile!!))
                val processedUri = ImageUtils.processAndCorrectOrientation(context, fileUri)
                state = state.copy(isProcessing = false)
                
                if (processedUri != null) {
                    onImageSelected(processedUri)
                } else {
                    state = state.copy(errorMessage = context.getString(R.string.error_processing_image))
                }
                tempCameraFile = null
            }
        } else {
            tempCameraFile = null
        }
    }
    
    // Launcher para permiso de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera(context, cameraLauncher) { filePath ->
                tempCameraFile = filePath
            }
        } else {
            state = state.copy(showPermissionRationale = true)
        }
    }
    
    // UI Principal
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .size(imageSize.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(enabled = !isUploading && !state.isProcessing) {
                    state = state.copy(showSourceDialog = true)
                },
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isProcessing -> {
                    // Procesando imagen
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.processing_image),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                currentImageUrl != null -> {
                    // Mostrar imagen actual
                    val fixedUrl = fixFirebaseStorageUrl(currentImageUrl)
                    
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(fixedUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(R.string.content_description_weapon_image),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Botón de eliminar
                    if (!isUploading) {
                        IconButton(
                            onClick = onImageRemoved,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(32.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.action_remove_image),
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    // Overlay de subida
                    if (isUploading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                }
                
                else -> {
                    // Placeholder vacío
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.action_add_photo),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Barra de progreso de subida
        if (isUploading) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { uploadProgress },
                modifier = Modifier.fillMaxWidth(0.6f)
            )
            Text(
                text = "${(uploadProgress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Mensaje de error
        state.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            LaunchedEffect(error) {
                kotlinx.coroutines.delay(3000)
                state = state.copy(errorMessage = null)
            }
        }
    }
    
    // Diálogo de selección de fuente
    if (state.showSourceDialog) {
        ImageSourceDialog(
            onDismiss = { state = state.copy(showSourceDialog = false) },
            onCameraSelected = {
                state = state.copy(showSourceDialog = false)
                // Verificar permiso de cámara
                when {
                    hasCameraPermission(context) -> {
                        launchCamera(context, cameraLauncher) { filePath ->
                            tempCameraFile = filePath
                        }
                    }
                    else -> {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            },
            onGallerySelected = {
                state = state.copy(showSourceDialog = false)
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }
    
    // Diálogo de permiso denegado
    if (state.showPermissionRationale) {
        PermissionRationaleDialog(
            onDismiss = { state = state.copy(showPermissionRationale = false) },
            onRetry = {
                state = state.copy(showPermissionRationale = false)
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        )
    }
}

/**
 * Diálogo para seleccionar la fuente de imagen.
 */
@Composable
private fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onCameraSelected: () -> Unit,
    onGallerySelected: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.dialog_select_image_source))
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.dialog_select_image_source_description),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Opción Cámara
                    OutlinedButton(
                        onClick = onCameraSelected,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.option_camera),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Opción Galería
                    OutlinedButton(
                        onClick = onGallerySelected,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.option_gallery),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

/**
 * Diálogo de explicación cuando el permiso de cámara es denegado.
 */
@Composable
private fun PermissionRationaleDialog(
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.dialog_camera_permission_title))
        },
        text = {
            Text(text = stringResource(R.string.dialog_camera_permission_rationale))
        },
        confirmButton = {
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.action_grant_permission))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_use_gallery))
            }
        }
    )
}

// ============== UTILIDADES ==============

private fun hasCameraPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}

private fun launchCamera(
    context: Context,
    cameraLauncher: androidx.activity.result.ActivityResultLauncher<Uri>,
    onFileCreated: (String) -> Unit
) {
    try {
        val photoFile = ImageUtils.createImageFile(context)
        onFileCreated(photoFile.absolutePath)
        val photoUri = ImageUtils.getUriForFile(context, photoFile)
        cameraLauncher.launch(photoUri)
    } catch (e: Exception) {
        Log.e("ImagePickerWithCamera", "Error creating file for camera: ${e.message}", e)
    }
}

/**
 * Corrige URLs de Firebase Storage que tienen el path decodificado incorrectamente.
 */
private fun fixFirebaseStorageUrl(url: String): String {
    if (url.isBlank() || !url.contains("firebasestorage.googleapis.com")) {
        return url
    }
    
    if (url.contains("/o/") && url.substringAfter("/o/").substringBefore("?").contains("%2F")) {
        return url
    }
    
    return try {
        val baseUrl = url.substringBefore("/o/") + "/o/"
        val pathAndQuery = url.substringAfter("/o/")
        val path = pathAndQuery.substringBefore("?")
        val query = if (pathAndQuery.contains("?")) "?" + pathAndQuery.substringAfter("?") else ""
        
        val encodedPath = java.net.URLEncoder.encode(path, "UTF-8")
            .replace("+", "%20")
        
        baseUrl + encodedPath + query
    } catch (e: Exception) {
        url
    }
}
