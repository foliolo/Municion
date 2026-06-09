package al.ahgitdevelopment.municion.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch

/**
 * Reusable image field for the entity forms: shows the current photo (freshly-picked bytes take
 * precedence over the stored [currentImageUrl]), lets the user pick a new one from the gallery via
 * FileKit (Android + iOS, no cinterop), and remove it. Upload happens on save in the ViewModel.
 */
@Composable
fun ImagePickerField(
    currentImageUrl: String?,
    pickedBytes: ByteArray?,
    isBusy: Boolean,
    onPick: (ByteArray) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Foto",
) {
    val scope = rememberCoroutineScope()
    val launcher =
        rememberFilePickerLauncher(type = FileKitType.Image) { file ->
            file?.let { picked -> scope.launch { onPick(picked.readBytes()) } }
        }
    val hasImage = pickedBytes != null || !currentImageUrl.isNullOrBlank()

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp)),
        ) {
            Box(contentAlignment = Alignment.Center) {
                when {
                    pickedBytes != null ->
                        AsyncImage(
                            model = pickedBytes,
                            contentDescription = label,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    !currentImageUrl.isNullOrBlank() ->
                        AsyncImage(
                            model = currentImageUrl,
                            contentDescription = label,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    else ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.AddAPhoto,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                "Sin foto",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                }
                if (isBusy) {
                    Surface(color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f), modifier = Modifier.fillMaxSize()) {
                        Box(contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { launcher.launch() }, enabled = !isBusy) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(if (hasImage) "Cambiar" else "Añadir foto", fontSize = 14.sp)
            }
            if (hasImage) {
                TextButton(onClick = onRemove, enabled = !isBusy) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Eliminar", fontSize = 14.sp)
                }
            }
        }
    }
}
