package al.ahgitdevelopment.municion.ui.guias

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.ui.theme.LicenseExpired
import al.ahgitdevelopment.municion.ui.theme.LicenseExpiring
import al.ahgitdevelopment.municion.ui.theme.LicenseValid
import al.ahgitdevelopment.municion.ui.theme.Primary
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Item de Guía para mostrar en LazyColumn.
 *
 * @param guia Datos de la guía
 * @param onClick Callback para click simple
 * @param onLongClick Callback para long-press (editar)
 * @param onDelete Callback para swipe-to-delete
 * @param onImageClick Callback para click en la imagen (null si no tiene imagen)
 * @param modifier Modificador opcional
 *
 * @since v3.0.0 (Compose Migration)
 * @since v3.2.2 (Image display feature)
 * @since v3.2.3 (Added image click to zoom)
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GuiaItem(
    guia: Guia,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
    onImageClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    // Color según uso de cupo
    val cupoColor = when {
        guia.cupoAgotado() -> LicenseExpired
        guia.porcentajeUsado() > 0.8f -> LicenseExpiring
        else -> LicenseValid
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.cd_delete),
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        },
        enableDismissFromStartToEnd = false,
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Imagen del arma o icono por defecto
                val hasValidImage = guia.hasImage()
                val imageUrl = if (hasValidImage) {
                    fixFirebaseStorageUrl(guia.fotoUrl ?: guia.imagePath ?: "")
                } else ""
                
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary.copy(alpha = 0.15f))
                        .then(
                            if (hasValidImage && onImageClick != null) {
                                Modifier.clickable { onImageClick(imageUrl) }
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasValidImage) {
                        // Mostrar imagen del arma
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(R.string.content_description_weapon_image),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Mostrar icono por defecto
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Contenido
                Column(modifier = Modifier.weight(1f)) {
                    // Marca y modelo
                    Text(
                        text = "${guia.marca} ${guia.modelo}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Calibre
                    Text(
                        text = guia.calibre1,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Barra de progreso de cupo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            LinearProgressIndicator(
                                progress = { guia.porcentajeUsado() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = cupoColor,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "${guia.disponible()}/${guia.cupo}",
                            style = MaterialTheme.typography.labelSmall,
                            color = cupoColor
                        )
                    }
                }
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
