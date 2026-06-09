package al.ahgitdevelopment.municion.ui.compras

import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.cancelar
import al.ahgitdevelopment.municion.resources.dialog_no_guides_available
import al.ahgitdevelopment.municion.resources.dialog_select_guide
import al.ahgitdevelopment.municion.resources.label_quota
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
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

/** Dialog to pick the parent guía before creating a compra. */
@Composable
fun GuiaSelectionDialog(
    guias: List<Guia>,
    onSelect: (Guia) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.dialog_select_guide)) },
        text = {
            if (guias.isEmpty()) {
                Text(stringResource(Res.string.dialog_no_guides_available))
            } else {
                LazyColumn {
                    items(guias) { guia ->
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(guia) }
                                    .padding(vertical = 12.dp),
                        ) {
                            Text(
                                text = "${guia.marca} ${guia.modelo}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = "${guia.calibre1} - ${stringResource(Res.string.label_quota)}${guia.disponible()}/${guia.cupo}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
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
                Text(stringResource(Res.string.cancelar))
            }
        },
    )
}
