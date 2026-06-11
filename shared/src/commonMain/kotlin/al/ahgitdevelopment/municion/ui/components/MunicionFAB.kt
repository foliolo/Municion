package al.ahgitdevelopment.municion.ui.components

import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.cd_save
import al.ahgitdevelopment.municion.ui.navigation.Compras
import al.ahgitdevelopment.municion.ui.navigation.Guias
import al.ahgitdevelopment.municion.ui.navigation.Licencias
import al.ahgitdevelopment.municion.ui.navigation.Tiradas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource

/**
 * Unified dynamic FAB: Add on the list tabs (entity-specific callback), Save on form screens
 * (only when a save callback is registered), hidden elsewhere.
 */
@Composable
fun MunicionFAB(
    currentRoute: String?,
    onAddLicencia: () -> Unit,
    onAddGuia: () -> Unit,
    onAddCompra: () -> Unit,
    onAddTirada: () -> Unit,
    onSave: () -> Unit,
    hasSaveCallback: Boolean,
) {
    when (currentRoute) {
        Licencias::class.qualifiedName -> AddFAB(onAddLicencia, "Añadir licencia")
        Guias::class.qualifiedName -> AddFAB(onAddGuia, "Añadir guía")
        Compras::class.qualifiedName -> AddFAB(onAddCompra, "Añadir compra")
        Tiradas::class.qualifiedName -> AddFAB(onAddTirada, "Añadir tirada")
        // Save FAB is driven purely by a registered save callback (set only on form screens, cleared
        // elsewhere by the NavHost). This avoids matching the route string, which is platform-fragile
        // (the qualified-name vs route-pattern mismatch hid the FAB on iOS).
        else -> if (hasSaveCallback) SaveFAB(onSave)
    }
}

@Composable
private fun AddFAB(
    onClick: () -> Unit,
    contentDescription: String,
) {
    FloatingActionButton(onClick = onClick, containerColor = MaterialTheme.colorScheme.primary) {
        Icon(Icons.Default.Add, contentDescription = contentDescription)
    }
}

@Composable
private fun SaveFAB(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick, containerColor = MaterialTheme.colorScheme.primary) {
        Icon(Icons.Default.Save, contentDescription = stringResource(Res.string.cd_save))
    }
}
