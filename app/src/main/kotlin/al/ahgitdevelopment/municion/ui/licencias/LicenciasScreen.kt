package al.ahgitdevelopment.municion.ui.licencias

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.ui.components.DeleteConfirmationDialog
import al.ahgitdevelopment.municion.ui.components.EmptyState
import al.ahgitdevelopment.municion.ui.components.ZoomableImageDialog
import al.ahgitdevelopment.municion.ui.navigation.LicenciaForm
import al.ahgitdevelopment.municion.ui.navigation.navtypes.navigateSafely
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import al.ahgitdevelopment.municion.ui.viewmodel.LicenciaViewModel
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

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
    var imageUrlToShow by remember { mutableStateOf<String?>(null) }

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
            title = stringResource(R.string.dialog_delete_license_title),
            message = stringResource(R.string.dialog_delete_license_message, licencia.numLicencia),
            onConfirm = {
                viewModel.deleteLicencia(licencia)
                licenciaToDelete = null
            },
            onDismiss = { licenciaToDelete = null }
        )
    }

    // Dialog de imagen con zoom
    imageUrlToShow?.let { imageUrl ->
        ZoomableImageDialog(
            imageUrl = imageUrl,
            contentDescription = stringResource(R.string.content_description_license_image),
            onDismiss = { imageUrlToShow = null }
        )
    }

    LicenciasListContent(
        licencias = licencias,
        onItemClick = { licencia ->
            navController.navigateSafely(LicenciaForm(licencia = licencia))
        },
        onDeleteClick = { licencia -> licenciaToDelete = licencia },
        onImageClick = { url -> imageUrlToShow = url }
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
 * @param onItemClick Callback para click en item (editar)
 * @param onDeleteClick Callback para swipe-to-delete
 * @param onImageClick Callback para click en imagen (mostrar zoom)
 * @param modifier Modificador opcional
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 * @since v3.2.3 (Added image click to zoom)
 * @since v3.2.4 (Changed long-click to click for edit)
 */
@Composable
fun LicenciasListContent(
    licencias: List<Licencia>,
    onItemClick: (Licencia) -> Unit,
    onDeleteClick: (Licencia) -> Unit,
    onImageClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (licencias.isEmpty()) {
        EmptyState(
            message = stringResource(R.string.empty_no_licenses),
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
                    onDelete = { onDeleteClick(licencia) },
                    onImageClick = onImageClick,
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
            onDeleteClick = {}
        )
    }
}
