package al.ahgitdevelopment.municion.ui.guias

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.ui.components.DeleteConfirmationDialog
import al.ahgitdevelopment.municion.ui.components.EmptyState
import al.ahgitdevelopment.municion.ui.components.ZoomableImageDialog
import al.ahgitdevelopment.municion.ui.navigation.GuiaForm
import al.ahgitdevelopment.municion.ui.navigation.navtypes.navigateSafely
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import al.ahgitdevelopment.municion.ui.viewmodel.GuiaViewModel
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
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

/**
 * Contenido de la pantalla de Guías para Single Scaffold Architecture.
 *
 * NO contiene Scaffold, TopBar ni FAB - estos están en MainScreen.
 * El dialog de selección de licencia también está en MainScreen.
 * Maneja el ViewModel, navegación y efectos secundarios.
 *
 * @param navController Controlador de navegación
 * @param snackbarHostState Estado del snackbar compartido desde MainScreen
 * @param viewModel ViewModel de Guías (inyectado por Hilt)
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 */
@Composable
fun GuiasContent(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    viewModel: GuiaViewModel = hiltViewModel()
) {
    val resources = LocalResources.current
    val guias by viewModel.guias.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var guiaToDelete by remember { mutableStateOf<Guia?>(null) }
    var imageUrlToShow by remember { mutableStateOf<String?>(null) }

    // Mostrar mensajes de UiState
    LaunchedEffect(uiState) {
        when (uiState) {
            is GuiaViewModel.GuiaUiState.Success -> {
                snackbarHostState.showSnackbar(
                    (uiState as GuiaViewModel.GuiaUiState.Success).message
                )
                viewModel.resetUiState()
            }

            is GuiaViewModel.GuiaUiState.Error -> {
                snackbarHostState.showSnackbar(
                    "Error: ${(uiState as GuiaViewModel.GuiaUiState.Error).message}"
                )
                viewModel.resetUiState()
            }

            else -> {}
        }
    }

    // Dialog de confirmación de eliminación
    guiaToDelete?.let { guia ->
        DeleteConfirmationDialog(
            title = stringResource(R.string.dialog_delete_guide_title),
            message = stringResource(R.string.dialog_delete_guide_message, guia.numGuia),
            onConfirm = {
                viewModel.deleteGuia(guia)
                guiaToDelete = null
            },
            onDismiss = { guiaToDelete = null }
        )
    }

    // Dialog de imagen con zoom
    imageUrlToShow?.let { imageUrl ->
        ZoomableImageDialog(
            imageUrl = imageUrl,
            contentDescription = stringResource(R.string.content_description_weapon_image),
            onDismiss = { imageUrlToShow = null }
        )
    }

    GuiasListContent(
        guias = guias,
        onItemClick = { /* Info */ },
        onItemLongClick = { guia ->
            val tipoLicenciaStr = resources.getStringArray(R.array.tipo_licencias)
                .getOrNull(guia.tipoLicencia) ?: ""
            navController.navigateSafely(
                GuiaForm(
                    guia = guia,
                    tipoLicencia = tipoLicenciaStr
                )
            )
        },
        onDeleteClick = { guia -> guiaToDelete = guia },
        onImageClick = { url -> imageUrlToShow = url }
    )
}

/**
 * Contenido de la lista de Guías (Stateless).
 *
 * Sin Scaffold - solo el contenido de la lista.
 * Recibe estado y callbacks como parámetros.
 * Fácil de previsualizar y testear.
 *
 * @param guias Lista de guías a mostrar
 * @param onItemClick Callback para click en item
 * @param onItemLongClick Callback para long-press (editar)
 * @param onDeleteClick Callback para swipe-to-delete
 * @param onImageClick Callback para click en imagen (mostrar zoom)
 * @param modifier Modificador opcional
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 * @since v3.2.3 (Added image click to zoom)
 */
@Composable
fun GuiasListContent(
    guias: List<Guia>,
    onItemClick: (Guia) -> Unit,
    onItemLongClick: (Guia) -> Unit,
    onDeleteClick: (Guia) -> Unit,
    onImageClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (guias.isEmpty()) {
        EmptyState(
            message = stringResource(R.string.empty_no_guides),
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
                items = guias,
                key = { it.id }
            ) { guia ->
                GuiaItem(
                    guia = guia,
                    onClick = { onItemClick(guia) },
                    onLongClick = { onItemLongClick(guia) },
                    onDelete = { onDeleteClick(guia) },
                    onImageClick = onImageClick,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GuiasListContentPreview() {
    MunicionTheme {
        GuiasListContent(
            guias = listOf(
                Guia(
                    id = 1,
                    tipoLicencia = 5,
                    numGuia = "G-12345",
                    numArma = "A-001",
                    marca = "Beretta",
                    modelo = "92FS",
                    apodo = "Mi Beretta",
                    tipoArma = 0,
                    calibre1 = "9mm Para",
                    cupo = 100,
                    gastado = 25
                ),
                Guia(
                    id = 2,
                    tipoLicencia = 4,
                    numGuia = "G-67890",
                    numArma = "A-002",
                    marca = "Remington",
                    modelo = "870",
                    apodo = "Mi Escopeta",
                    tipoArma = 1,
                    calibre1 = "12/70",
                    cupo = 200,
                    gastado = 180
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
private fun GuiasListContentEmptyPreview() {
    MunicionTheme {
        GuiasListContent(
            guias = emptyList(),
            onItemClick = {},
            onItemLongClick = {},
            onDeleteClick = {}
        )
    }
}
