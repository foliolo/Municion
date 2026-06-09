package al.ahgitdevelopment.municion.ui.licencias

import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.cd_delete
import al.ahgitdevelopment.municion.resources.content_description_license_image
import al.ahgitdevelopment.municion.resources.tipo_licencias
import al.ahgitdevelopment.municion.ui.theme.LicenseExpired
import al.ahgitdevelopment.municion.ui.theme.LicenseExpiring
import al.ahgitdevelopment.municion.ui.theme.LicenseValid
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

/** License card with status color, photo/icon, swipe-to-delete and click-to-edit. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenciaItem(
    licencia: Licencia,
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

    val statusColor =
        when {
            licencia.estaCaducada() -> LicenseExpired
            licencia.caducaProxima() -> LicenseExpiring
            else -> LicenseValid
        }
    val tipos = stringArrayResource(Res.array.tipo_licencias)
    val nombre =
        licencia.nombre?.takeIf { it.isNotBlank() }
            ?: tipos.getOrElse(licencia.tipo) { "Licencia Tipo ${licencia.tipo}" }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier =
                    Modifier
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
                    modifier =
                        Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .then(
                                if (!licencia.fotoUrl.isNullOrBlank() && onImageClick != null) {
                                    Modifier.clickable { onImageClick(licencia.fotoUrl) }
                                } else {
                                    Modifier
                                },
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!licencia.fotoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = licencia.fotoUrl,
                            contentDescription = stringResource(Res.string.content_description_license_image),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Icon(Icons.Default.Badge, null, tint = statusColor, modifier = Modifier.size(36.dp))
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        licencia.numLicencia,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(licencia.estadoDescripcion(), style = MaterialTheme.typography.labelMedium, color = statusColor)
                        Text(
                            "Caduca: ${licencia.fechaCaducidad}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        }
    }
}
