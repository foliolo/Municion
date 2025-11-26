package al.ahgitdevelopment.municion.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import al.ahgitdevelopment.municion.ui.navigation.Routes

/**
 * FAB dinámico unificado para toda la aplicación.
 *
 * Cambia según el tipo de pantalla:
 * - ListScreens: Icono Add ’ ejecuta onAdd específico para cada pantalla
 * - FormScreens: Icono Save ’ ejecuta onSave
 * - Otras pantallas: No se muestra
 *
 * @param currentRoute Ruta actual de navegación
 * @param onAddLicencia Callback para añadir licencia
 * @param onAddGuia Callback para añadir guía (abre dialog de selección de licencia)
 * @param onAddCompra Callback para añadir compra (abre dialog de selección de guía)
 * @param onAddTirada Callback para añadir tirada
 * @param onSave Callback para guardar en formularios
 * @param hasSaveCallback Indica si hay un callback de guardado registrado
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 */
@Composable
fun MunicionFAB(
    currentRoute: String?,
    onAddLicencia: () -> Unit,
    onAddGuia: () -> Unit,
    onAddCompra: () -> Unit,
    onAddTirada: () -> Unit,
    onSave: () -> Unit,
    hasSaveCallback: Boolean
) {
    when (currentRoute) {
        Routes.LICENCIAS -> {
            AddFAB(
                onClick = onAddLicencia,
                contentDescription = "Añadir licencia"
            )
        }
        Routes.GUIAS -> {
            AddFAB(
                onClick = onAddGuia,
                contentDescription = "Añadir guía"
            )
        }
        Routes.COMPRAS -> {
            AddFAB(
                onClick = onAddCompra,
                contentDescription = "Añadir compra"
            )
        }
        Routes.TIRADAS -> {
            AddFAB(
                onClick = onAddTirada,
                contentDescription = "Añadir tirada"
            )
        }
        else -> {
            // FormScreens: mostrar FAB de guardar solo si hay callback registrado
            if (currentRoute?.contains("Form") == true && hasSaveCallback) {
                SaveFAB(onClick = onSave)
            }
        }
    }
}

/**
 * FAB para añadir elementos.
 */
@Composable
private fun AddFAB(
    onClick: () -> Unit,
    contentDescription: String
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = contentDescription
        )
    }
}

/**
 * FAB para guardar en formularios.
 */
@Composable
private fun SaveFAB(
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(
            imageVector = Icons.Default.Save,
            contentDescription = "Guardar"
        )
    }
}
