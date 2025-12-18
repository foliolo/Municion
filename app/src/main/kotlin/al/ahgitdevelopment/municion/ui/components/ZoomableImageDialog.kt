package al.ahgitdevelopment.municion.ui.components

import al.ahgitdevelopment.municion.R
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

/**
 * Dialog a pantalla completa con imagen zoomable desde un recurso drawable.
 *
 * Soporta:
 * - Pinch-to-zoom (1x a 5x)
 * - Pan/arrastrar cuando está en zoom
 * - Doble-tap para zoom rápido (toggle 1x <-> 2.5x)
 * - Botón de cerrar
 *
 * @param imageRes Recurso drawable de la imagen
 * @param contentDescription Descripción para accesibilidad
 * @param onDismiss Callback cuando se cierra el dialog
 */
@Composable
fun ZoomableImageDialog(
    @DrawableRes imageRes: Int,
    contentDescription: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            var scale by remember { mutableFloatStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }

            Image(
                painter = painterResource(id = imageRes),
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale > 1f) {
                                offset = Offset(
                                    x = offset.x + pan.x * 2,
                                    y = offset.y + pan.y * 2
                                )
                            } else {
                                offset = Offset.Zero
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (scale > 1f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                } else {
                                    scale = 2.5f
                                }
                            }
                        )
                    }
            )

            // Botón cerrar
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.cancelar),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Dialog a pantalla completa con imagen zoomable desde una URL.
 *
 * Soporta:
 * - Pinch-to-zoom (1x a 5x)
 * - Pan/arrastrar cuando está en zoom
 * - Doble-tap para zoom rápido (toggle 1x <-> 2.5x)
 * - Botón de cerrar
 * - Indicador de carga mientras descarga la imagen
 *
 * @param imageUrl URL de la imagen (Firebase Storage, etc.)
 * @param contentDescription Descripción para accesibilidad
 * @param onDismiss Callback cuando se cierra el dialog
 */
@Composable
fun ZoomableImageDialog(
    imageUrl: String,
    contentDescription: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            var scale by remember { mutableFloatStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }

            // Corregir URL de Firebase Storage si es necesario
            val correctedUrl = fixFirebaseStorageUrl(imageUrl)

            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(correctedUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale > 1f) {
                                offset = Offset(
                                    x = offset.x + pan.x * 2,
                                    y = offset.y + pan.y * 2
                                )
                            } else {
                                offset = Offset.Zero
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (scale > 1f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                } else {
                                    scale = 2.5f
                                }
                            }
                        )
                    },
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = Color.White
                        )
                    }
                }
            )

            // Botón cerrar
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.cancelar),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Corrige URLs de Firebase Storage que tienen el path decodificado incorrectamente.
 * 
 * Firebase Storage requiere que el path después de /o/ esté URL-encoded.
 * Ejemplo correcto: .../o/v3_userdata%2FuserId%2Farmas%2F6.jpg?alt=media...
 * Ejemplo incorrecto: .../o/v3_userdata/userId/armas/6.jpg?alt=media...
 */
private fun fixFirebaseStorageUrl(url: String): String {
    if (url.isBlank() || !url.contains("firebasestorage.googleapis.com")) {
        return url
    }
    
    // Si ya tiene %2F en el path, está correctamente codificada
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
