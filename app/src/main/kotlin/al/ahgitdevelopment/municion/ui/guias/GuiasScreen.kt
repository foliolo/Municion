package al.ahgitdevelopment.municion.ui.guias

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.ui.components.DeleteConfirmationDialog
import al.ahgitdevelopment.municion.ui.components.EmptyState
import al.ahgitdevelopment.municion.ui.components.ListScreenTopBar
import al.ahgitdevelopment.municion.ui.navigation.Routes
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import al.ahgitdevelopment.municion.ui.viewmodel.GuiaViewModel
import al.ahgitdevelopment.municion.ui.viewmodel.MainViewModel.SyncState

/**
 * Pantalla de listado de Guías (Stateful).
 *
 * Maneja el ViewModel, navegación y efectos secundarios.
 * Delega la UI a GuiasScreenContent (stateless).
 *
 * @param navController Controlador de navegación
 * @param syncState Estado actual de sincronización
 * @param onSyncClick Callback para sincronización manual
 * @param onSettingsClick Callback para el botón de settings
 * @param viewModel ViewModel de Guías (inyectado por Hilt)
 *
 * @since v3.0.0 (Compose Migration)
 */
@Composable
fun GuiasScreen(
    navController: NavHostController,
    syncState: SyncState,
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit,
    bottomPadding: Dp = 0.dp,
    viewModel: GuiaViewModel = hiltViewModel()
) {
    val guias by viewModel.guias.collectAsStateWithLifecycle()
    val licencias by viewModel.licencias.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    var guiaToDelete by remember { mutableStateOf<Guia?>(null) }
    var showLicenciaSelectionDialog by remember { mutableStateOf(false) }

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
            title = "Eliminar guía",
            message = "¿Estás seguro de que deseas eliminar la guía ${guia.numGuia}?",
            onConfirm = {
                viewModel.deleteGuia(guia)
                guiaToDelete = null
            },
            onDismiss = { guiaToDelete = null }
        )
    }

    // Dialog de selección de licencia para crear nueva guía
    if (showLicenciaSelectionDialog) {
        LicenciaSelectionDialog(
            licencias = licencias,
            onLicenciaSelected = { licencia ->
                val tipoLicencia = licencia.getNombre(context)
                navController.navigate(Routes.guiaForm(tipoLicencia = tipoLicencia))
                showLicenciaSelectionDialog = false
            },
            onDismiss = { showLicenciaSelectionDialog = false }
        )
    }

    GuiasScreenContent(
        guias = guias,
        hasLicencias = licencias.isNotEmpty(),
        syncState = syncState,
        snackbarHostState = snackbarHostState,
        onSyncClick = onSyncClick,
        onSettingsClick = onSettingsClick,
        onAddClick = {
            if (licencias.isEmpty()) {
                // No hay licencias
            } else {
                showLicenciaSelectionDialog = true
            }
        },
        onItemClick = { /* Info */ },
        onItemLongClick = { guia ->
            navController.navigate(
                Routes.guiaForm(
                    tipoLicencia = guia.tipoLicencia.toString(),
                    guiaId = guia.id
                )
            )
        },
        onDeleteClick = { guia -> guiaToDelete = guia },
        bottomPadding = bottomPadding
    )
}

/**
 * Contenido de la pantalla de Guías (Stateless).
 *
 * Recibe estado y callbacks como parámetros.
 * Fácil de previsualizar y testear.
 *
 * @param guias Lista de guías a mostrar
 * @param hasLicencias Indica si hay licencias disponibles
 * @param syncState Estado actual de sincronización
 * @param snackbarHostState Estado del snackbar
 * @param onSyncClick Callback para sincronización manual
 * @param onSettingsClick Callback para el botón de settings
 * @param onAddClick Callback para añadir guía
 * @param onItemClick Callback para click en item
 * @param onItemLongClick Callback para long-press (editar)
 * @param onDeleteClick Callback para swipe-to-delete
 *
 * @since v3.0.0 (Compose Migration)
 */
@Composable
fun GuiasScreenContent(
    guias: List<Guia>,
    hasLicencias: Boolean,
    syncState: SyncState,
    snackbarHostState: SnackbarHostState,
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddClick: () -> Unit,
    onItemClick: (Guia) -> Unit,
    onItemLongClick: (Guia) -> Unit,
    onDeleteClick: (Guia) -> Unit,
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
                Icon(Icons.Default.Add, contentDescription = "Añadir guía")
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        if (guias.isEmpty()) {
            EmptyState(
                message = "No hay guías registradas",
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
                    items = guias,
                    key = { it.id }
                ) { guia ->
                    GuiaItem(
                        guia = guia,
                        onClick = { onItemClick(guia) },
                        onLongClick = { onItemLongClick(guia) },
                        onDelete = { onDeleteClick(guia) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GuiasScreenContentPreview() {
    MunicionTheme {
        GuiasScreenContent(
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
            hasLicencias = true,
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
private fun GuiasScreenContentEmptyPreview() {
    MunicionTheme {
        GuiasScreenContent(
            guias = emptyList(),
            hasLicencias = false,
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
