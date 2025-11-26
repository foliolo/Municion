package al.ahgitdevelopment.municion.ui.licencias

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.ui.components.DeleteConfirmationDialog
import al.ahgitdevelopment.municion.ui.components.EmptyState
import al.ahgitdevelopment.municion.ui.navigation.Routes
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import al.ahgitdevelopment.municion.ui.viewmodel.LicenciaViewModel

/**
 * Contenido de la pantalla de Licencias para Single Scaffold Architecture.
 *
 * NO contiene Scaffold, TopBar ni FAB - estos están en MainScreen.
 * Maneja el ViewModel, navegación y efectos secundarios.
 *
 * @param navController Controlador de navegación
 * @param snackbarHostState Estado del snackbar compartido desde MainScreen
 * @param viewModel ViewModel de Licencias (inyectado por Hilt)
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 */
@Composable
fun LicenciasContent(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    viewModel: LicenciaViewModel = hiltViewModel()
) {
    val licencias by viewModel.licencias.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var licenciaToDelete by remember { mutableStateOf<Licencia?>(null) }

    // Mostrar mensajes de UiState
    LaunchedEffect(uiState) {
        when (uiState) {
            is LicenciaViewModel.LicenciaUiState.Success -> {
                snackbarHostState.showSnackbar(
                    (uiState as LicenciaViewModel.LicenciaUiState.Success).message
                )
                viewModel.resetUiState()
            }
            is LicenciaViewModel.LicenciaUiState.Error -> {
                snackbarHostState.showSnackbar(
                    "Error: ${(uiState as LicenciaViewModel.LicenciaUiState.Error).message}"
                )
                viewModel.resetUiState()
            }
            else -> {}
        }
    }

    // Dialog de confirmación de eliminación
    licenciaToDelete?.let { licencia ->
        DeleteConfirmationDialog(
            title = "Eliminar licencia",
            message = "¿Estás seguro de que deseas eliminar la licencia ${licencia.numLicencia}?",
            onConfirm = {
                viewModel.deleteLicencia(licencia)
                licenciaToDelete = null
            },
            onDismiss = { licenciaToDelete = null }
        )
    }

    LicenciasListContent(
        licencias = licencias,
        onItemClick = { /* Click simple: info */ },
        onItemLongClick = { licencia ->
            navController.navigate("${Routes.LICENCIA_FORM}?licenciaId=${licencia.id}")
        },
        onDeleteClick = { licencia -> licenciaToDelete = licencia }
    )
}

/**
 * Contenido de la lista de Licencias (Stateless).
 *
 * Sin Scaffold - solo el contenido de la lista.
 * Recibe estado y callbacks como parámetros.
 * Fácil de previsualizar y testear.
 *
 * @param licencias Lista de licencias a mostrar
 * @param onItemClick Callback para click en item
 * @param onItemLongClick Callback para long-press (editar)
 * @param onDeleteClick Callback para swipe-to-delete
 * @param modifier Modificador opcional
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 */
@Composable
fun LicenciasListContent(
    licencias: List<Licencia>,
    onItemClick: (Licencia) -> Unit,
    onItemLongClick: (Licencia) -> Unit,
    onDeleteClick: (Licencia) -> Unit,
    modifier: Modifier = Modifier
) {
    if (licencias.isEmpty()) {
        EmptyState(
            message = "No hay licencias registradas",
            modifier = modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(
                items = licencias,
                key = { it.id }
            ) { licencia ->
                LicenciaItem(
                    licencia = licencia,
                    onClick = { onItemClick(licencia) },
                    onLongClick = { onItemLongClick(licencia) },
                    onDelete = { onDeleteClick(licencia) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LicenciasListContentPreview() {
    MunicionTheme {
        LicenciasListContent(
            licencias = listOf(
                Licencia(
                    id = 1,
                    tipo = 0,
                    edad = 30,
                    numLicencia = "12345678",
                    fechaExpedicion = "01/01/2024",
                    fechaCaducidad = "01/01/2029"
                ),
                Licencia(
                    id = 2,
                    tipo = 1,
                    edad = 35,
                    numLicencia = "87654321",
                    fechaExpedicion = "15/06/2023",
                    fechaCaducidad = "15/06/2025"
                )
            ),
            onItemClick = {},
            onItemLongClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LicenciasListContentEmptyPreview() {
    MunicionTheme {
        LicenciasListContent(
            licencias = emptyList(),
            onItemClick = {},
            onItemLongClick = {},
            onDeleteClick = {}
        )
    }
}
