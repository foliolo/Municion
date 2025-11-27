package al.ahgitdevelopment.municion.ui.compras

import al.ahgitdevelopment.municion.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import al.ahgitdevelopment.municion.data.local.room.entities.Guia

/**
 * Dialog para seleccionar una guía antes de crear una compra.
 *
 * @param guias Lista de guías disponibles
 * @param onGuiaSelected Callback cuando se selecciona una guía
 * @param onDismiss Callback cuando se cancela
 *
 * @since v3.0.0 (Compose Migration)
 */
@Composable
fun GuiaSelectionDialog(
    guias: List<Guia>,
    onGuiaSelected: (Guia) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_select_guide)) },
        text = {
            if (guias.isEmpty()) {
                Text(stringResource(R.string.dialog_no_guides_available))
            } else {
                LazyColumn {
                    items(guias) { guia ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onGuiaSelected(guia) }
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = "${guia.marca} ${guia.modelo}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "${guia.calibre1} - ${stringResource(R.string.label_quota)}${guia.disponible()}/${guia.cupo}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancelar))
            }
        }
    )
}
