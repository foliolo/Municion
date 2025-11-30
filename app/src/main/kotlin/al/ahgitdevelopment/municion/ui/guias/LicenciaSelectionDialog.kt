package al.ahgitdevelopment.municion.ui.guias

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia

/**
 * Dialog para seleccionar una licencia antes de crear una guía.
 *
 * @param licencias Lista de licencias disponibles
 * @param onLicenciaSelected Callback cuando se selecciona una licencia
 * @param onDismiss Callback cuando se cancela
 *
 * @since v3.0.0 (Compose Migration)
 */
@Composable
fun LicenciaSelectionDialog(
    licencias: List<Licencia>,
    onLicenciaSelected: (Licencia) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecciona una licencia") },
        text = {
            if (licencias.isEmpty()) {
                Text("No hay licencias disponibles. Primero debes crear una licencia.")
            } else {
                LazyColumn {
                    items(licencias) { licencia ->
                        Text(
                            text = licencia.getNombre(context),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLicenciaSelected(licencia) }
                                .padding(vertical = 12.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
