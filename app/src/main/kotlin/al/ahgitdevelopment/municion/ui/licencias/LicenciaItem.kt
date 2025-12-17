package al.ahgitdevelopment.municion.ui.licencias

import al.ahgitdevelopment.municion.R
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.ui.theme.LicenseExpired
import al.ahgitdevelopment.municion.ui.theme.LicenseExpiring
import al.ahgitdevelopment.municion.ui.theme.LicenseValid
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Item de Licencia para mostrar en LazyColumn.
 *
 * Diseño minimalista con:
 * - Icono a la izquierda con indicador de estado
 * - Título (tipo de licencia)
 * - Subtítulo (número de licencia)
 * - Fecha de caducidad con estado visual
 * - Swipe-to-delete
 * - Long-press para editar
 *
 * @param licencia Datos de la licencia
 * @param onClick Callback para click simple
 * @param onLongClick Callback para long-press (editar)
 * @param onDelete Callback para swipe-to-delete
 * @param modifier Modificador opcional
 *
 * @since v3.0.0 (Compose Migration)
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LicenciaItem(
    licencia: Licencia,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dismissState = rememberSwipeToDismissBoxState()

    // Detectar cuando se completa el swipe
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    // Determinar el color del estado de la licencia
    val statusColor = when {
        licencia.estaCaducada() -> LicenseExpired
        licencia.caducaProxima() -> LicenseExpiring
        else -> LicenseValid
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            // Fondo rojo al hacer swipe
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
                // Imagen o icono (ampliado a 64dp como en GuiaItem)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!licencia.fotoUrl.isNullOrBlank()) {
                        // Mostrar imagen de Firebase Storage
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(licencia.fotoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(R.string.content_description_license_image),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Mostrar icono por defecto
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Contenido
                Column(modifier = Modifier.weight(1f)) {
                    // Nombre del tipo de licencia
                    Text(
                        text = licencia.getNombre(context),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Número de licencia
                    Text(
                        text = licencia.numLicencia,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Estado y fecha de caducidad
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = licencia.estadoDescripcion(),
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor
                        )

                        Text(
                            text = "Caduca: ${licencia.fechaCaducidad}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
