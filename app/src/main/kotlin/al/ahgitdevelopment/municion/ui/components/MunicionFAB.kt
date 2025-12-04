package al.ahgitdevelopment.municion.ui.components

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.ui.navigation.Compras
import al.ahgitdevelopment.municion.ui.navigation.Guias
import al.ahgitdevelopment.municion.ui.navigation.Licencias
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import al.ahgitdevelopment.municion.ui.navigation.Route
import al.ahgitdevelopment.municion.ui.navigation.Tiradas

/**
 * FAB din�mico unificado para toda la aplicaci�n.
 *
 * Cambia seg�n el tipo de pantalla:
 * - ListScreens: Icono Add � ejecuta onAdd espec�fico para cada pantalla
 * - FormScreens: Icono Save � ejecuta onSave
 * - Otras pantallas: No se muestra
 *
 * @param currentRoute Ruta actual de navegaci�n
 * @param onAddLicencia Callback para a�adir licencia
 * @param onAddGuia Callback para a�adir gu�a (abre dialog de selecci�n de licencia)
 * @param onAddCompra Callback para a�adir compra (abre dialog de selecci�n de gu�a)
 * @param onAddTirada Callback para a�adir tirada
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
        Licencias::class.qualifiedName -> {
            AddFAB(
                onClick = onAddLicencia,
                contentDescription = "A�adir licencia"
            )
        }
        Guias::class.qualifiedName -> {
            AddFAB(
                onClick = onAddGuia,
                contentDescription = "A�adir gu�a"
            )
        }
        Compras::class.qualifiedName -> {
            AddFAB(
                onClick = onAddCompra,
                contentDescription = "A�adir compra"
            )
        }
        Tiradas::class.qualifiedName -> {
            AddFAB(
                onClick = onAddTirada,
                contentDescription = "A�adir tirada"
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
 * FAB para a�adir elementos.
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
            contentDescription = stringResource(R.string.cd_save)
        )
    }
}
