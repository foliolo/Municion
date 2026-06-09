package al.ahgitdevelopment.municion.ui.guias

import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.cd_delete
import al.ahgitdevelopment.municion.resources.content_description_weapon_image
import al.ahgitdevelopment.municion.ui.theme.LicenseExpired
import al.ahgitdevelopment.municion.ui.theme.LicenseExpiring
import al.ahgitdevelopment.municion.ui.theme.LicenseValid
import al.ahgitdevelopment.municion.ui.theme.Primary
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.stringResource

/** Guía card with cupo gradient bar, photo/icon, swipe-to-delete and click-to-edit. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuiaItem(
    guia: Guia,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onImageClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState()
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    // Remaining-cupo fraction: 1.0 = full, 0.0 = exhausted.
    val remainingFraction = if (guia.cupo > 0) {
        (guia.disponible().toFloat() / guia.cupo.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    // Continuous color interpolation: green (full) → yellow (half) → red (empty).
    val cupoColor = when {
        remainingFraction >= 0.5f -> lerp(LicenseExpiring, LicenseValid, (remainingFraction - 0.5f) * 2f)
        else -> lerp(LicenseExpired, LicenseExpiring, remainingFraction * 2f)
    }

    val imageUrl = guia.fotoUrl ?: guia.imagePath
    val hasValidImage = guia.hasImage() && !imageUrl.isNullOrBlank()

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Default.Delete, stringResource(Res.string.cd_delete), tint = MaterialTheme.colorScheme.onError)
            }
        },
        enableDismissFromStartToEnd = false,
        modifier = modifier,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary.copy(alpha = 0.15f))
                        .then(
                            if (hasValidImage && onImageClick != null) {
                                Modifier.clickable { onImageClick(imageUrl!!) }
                            } else {
                                Modifier
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (hasValidImage) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = stringResource(Res.string.content_description_weapon_image),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Icon(Icons.Default.Security, null, tint = Primary, modifier = Modifier.size(36.dp))
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(guia.apodo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(2.dp))
                    Text("${guia.marca} ${guia.modelo}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(2.dp))
                    Text(guia.calibre1, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        val barBrush = Brush.horizontalGradient(
                            colors = listOf(lerp(cupoColor, LicenseValid, 0.3f), cupoColor),
                        )
                        Canvas(modifier = Modifier.weight(1f).height(6.dp)) {
                            val cornerRadius = CornerRadius(size.height / 2f)
                            drawRoundRect(color = trackColor, cornerRadius = cornerRadius)
                            if (remainingFraction > 0f) {
                                drawRoundRect(
                                    brush = barBrush,
                                    size = Size(size.width * remainingFraction, size.height),
                                    cornerRadius = cornerRadius,
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("${guia.disponible()}/${guia.cupo}", style = MaterialTheme.typography.labelSmall, color = cupoColor)
                    }
                }
            }
        }
    }
}
