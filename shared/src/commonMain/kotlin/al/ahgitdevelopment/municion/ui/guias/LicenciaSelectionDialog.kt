package al.ahgitdevelopment.municion.ui.guias

import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.cancelar
import al.ahgitdevelopment.municion.resources.dialog_no_licenses_available
import al.ahgitdevelopment.municion.resources.dialog_select_license
import al.ahgitdevelopment.municion.resources.tipo_licencias
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
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

/** Dialog that lists the user's licencias so one can be picked before creating a guía. */
@Composable
fun LicenciaSelectionDialog(
    licencias: List<Licencia>,
    onSelect: (Licencia) -> Unit,
    onDismiss: () -> Unit,
) {
    val tipos = stringArrayResource(Res.array.tipo_licencias)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.dialog_select_license)) },
        text = {
            if (licencias.isEmpty()) {
                Text(stringResource(Res.string.dialog_no_licenses_available))
            } else {
                LazyColumn {
                    items(licencias) { licencia ->
                        val nombre =
                            licencia.nombre?.takeIf { it.isNotBlank() }
                                ?: tipos.getOrElse(licencia.tipo) { "Licencia Tipo ${licencia.tipo}" }
                        Text(
                            text = nombre,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(licencia) }
                                    .padding(vertical = 12.dp),
                            style = MaterialTheme.typography.bodyLarge,
                        )
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
