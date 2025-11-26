package al.ahgitdevelopment.municion.ui.licencias

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.ui.components.DeleteConfirmationDialog
import al.ahgitdevelopment.municion.ui.components.EmptyState
import al.ahgitdevelopment.municion.ui.components.ListScreenTopBar
import al.ahgitdevelopment.municion.ui.navigation.Routes
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import al.ahgitdevelopment.municion.ui.viewmodel.LicenciaViewModel
import al.ahgitdevelopment.municion.ui.viewmodel.MainViewModel.SyncState

/**
 * Pantalla de listado de Licencias (Stateful).
 *
 * Maneja el ViewModel, navegación y efectos secundarios.
 * Delega la UI a LicenciasScreenContent (stateless).
 *
 * @param navController Controlador de navegación
 * @param syncState Estado actual de sincronización
 * @param onSyncClick Callback para sincronización manual
 * @param onSettingsClick Callback para el botón de settings
 * @param viewModel ViewModel de Licencias (inyectado por Hilt)
 *
 * @since v3.0.0 (Compose Migration)
 */
@Composable
fun LicenciasScreen(
    navController: NavHostController,
    syncState: SyncState,
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit,
    bottomPadding: Dp = 0.dp,
    viewModel: LicenciaViewModel = hiltViewModel()
) {
    val licencias by viewModel.licencias.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
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

    LicenciasScreenContent(
        licencias = licencias,
        syncState = syncState,
        snackbarHostState = snackbarHostState,
        onSyncClick = onSyncClick,
        onSettingsClick = onSettingsClick,
        onAddClick = { navController.navigate("${Routes.LICENCIA_FORM}?licenciaId=-1") },
        onItemClick = { /* Click simple: info */ },
        onItemLongClick = { licencia ->
            navController.navigate("${Routes.LICENCIA_FORM}?licenciaId=${licencia.id}")
        },
        onDeleteClick = { licencia -> licenciaToDelete = licencia },
        bottomPadding = bottomPadding
    )
}

/**
 * Contenido de la pantalla de Licencias (Stateless).
 *
 * Recibe estado y callbacks como parámetros.
 * Fácil de previsualizar y testear.
 *
 * @param licencias Lista de licencias a mostrar
 * @param syncState Estado actual de sincronización
 * @param snackbarHostState Estado del snackbar
 * @param onSyncClick Callback para sincronización manual
 * @param onSettingsClick Callback para el botón de settings
 * @param onAddClick Callback para añadir licencia
 * @param onItemClick Callback para click en item
 * @param onItemLongClick Callback para long-press (editar)
 * @param onDeleteClick Callback para swipe-to-delete
 *
 * @since v3.0.0 (Compose Migration)
 */
@Composable
fun LicenciasScreenContent(
    licencias: List<Licencia>,
    syncState: SyncState,
    snackbarHostState: SnackbarHostState,
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddClick: () -> Unit,
    onItemClick: (Licencia) -> Unit,
    onItemLongClick: (Licencia) -> Unit,
    onDeleteClick: (Licencia) -> Unit,
    bottomPadding: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ListScreenTopBar(
                syncState = syncState,
                onSyncClick = onSyncClick,
                onSettingsClick = onSettingsClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir licencia")
            }
        },
        // Edge-to-edge: TopBar handles status bar inset
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        if (licencias.isEmpty()) {
            EmptyState(
                message = "No hay licencias registradas",
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(bottom = bottomPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp + bottomPadding)
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
}

@Preview(showBackground = true)
@Composable
private fun LicenciasScreenContentPreview() {
    MunicionTheme {
        LicenciasScreenContent(
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
            syncState = SyncState.Idle,
            snackbarHostState = SnackbarHostState(),
            onSyncClick = {},
            onSettingsClick = {},
            onAddClick = {},
            onItemClick = {},
            onItemLongClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LicenciasScreenContentEmptyPreview() {
    MunicionTheme {
        LicenciasScreenContent(
            licencias = emptyList(),
            syncState = SyncState.Idle,
            snackbarHostState = SnackbarHostState(),
            onSyncClick = {},
            onSettingsClick = {},
            onAddClick = {},
            onItemClick = {},
            onItemLongClick = {},
            onDeleteClick = {}
        )
    }
}
