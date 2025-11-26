package al.ahgitdevelopment.municion.ui.tiradas

import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import al.ahgitdevelopment.municion.ui.components.DeleteConfirmationDialog
import al.ahgitdevelopment.municion.ui.components.EmptyState
import al.ahgitdevelopment.municion.ui.components.ListScreenTopBar
import al.ahgitdevelopment.municion.ui.navigation.Routes
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import al.ahgitdevelopment.municion.ui.viewmodel.MainViewModel.SyncState
import al.ahgitdevelopment.municion.ui.viewmodel.TiradaViewModel
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

/**
 * Pantalla de listado de Tiradas (Stateful).
 *
 * Maneja el ViewModel, navegación y efectos secundarios.
 * Delega la UI a TiradasScreenContent (stateless).
 *
 * @param navController Controlador de navegación
 * @param syncState Estado actual de sincronización
 * @param onSyncClick Callback para sincronización manual
 * @param onSettingsClick Callback para el botón de settings
 * @param viewModel ViewModel de Tiradas
 *
 * @since v3.0.0 (Compose Migration)
 */
@Composable
fun TiradasScreen(
    navController: NavHostController,
    syncState: SyncState,
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit,
    bottomPadding: Dp = 0.dp,
    viewModel: TiradaViewModel = hiltViewModel()
) {
    val tiradas by viewModel.tiradas.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var tiradaToDelete by remember { mutableStateOf<Tirada?>(null) }

    // Mostrar mensajes de UiState
    LaunchedEffect(uiState) {
        when (uiState) {
            is TiradaViewModel.TiradaUiState.Success -> {
                snackbarHostState.showSnackbar(
                    (uiState as TiradaViewModel.TiradaUiState.Success).message
                )
                viewModel.resetUiState()
            }

            is TiradaViewModel.TiradaUiState.Error -> {
                snackbarHostState.showSnackbar(
                    "Error: ${(uiState as TiradaViewModel.TiradaUiState.Error).message}"
                )
                viewModel.resetUiState()
            }

            else -> {}
        }
    }

    // Dialog de confirmación de eliminación
    tiradaToDelete?.let { tirada ->
        DeleteConfirmationDialog(
            title = "Eliminar tirada",
            message = "¿Estás seguro de que deseas eliminar la tirada \"${tirada.descripcion}\"?",
            onConfirm = {
                viewModel.deleteTirada(tirada)
                tiradaToDelete = null
            },
            onDismiss = { tiradaToDelete = null }
        )
    }

    TiradasScreenContent(
        tiradas = tiradas,
        syncState = syncState,
        snackbarHostState = snackbarHostState,
        onSyncClick = onSyncClick,
        onSettingsClick = onSettingsClick,
        onAddClick = { navController.navigate("${Routes.TIRADA_FORM}?tiradaId=-1") },
        onItemClick = { /* Info */ },
        onItemLongClick = { tirada ->
            navController.navigate("${Routes.TIRADA_FORM}?tiradaId=${tirada.id}")
        },
        onDeleteClick = { tirada -> tiradaToDelete = tirada },
        bottomPadding = bottomPadding
    )
}

/**
 * Contenido de la pantalla de Tiradas (Stateless).
 *
 * Recibe estado y callbacks como parámetros.
 * Fácil de previsualizar y testear.
 *
 * @param tiradas Lista de tiradas a mostrar
 * @param syncState Estado actual de sincronización
 * @param snackbarHostState Estado del snackbar
 * @param onSyncClick Callback para sincronización manual
 * @param onSettingsClick Callback para el botón de settings
 * @param onAddClick Callback para añadir tirada
 * @param onItemClick Callback para click en item
 * @param onItemLongClick Callback para long-press (editar)
 * @param onDeleteClick Callback para swipe-to-delete
 *
 * @since v3.0.0 (Compose Migration)
 */
@Composable
fun TiradasScreenContent(
    tiradas: List<Tirada>,
    syncState: SyncState,
    snackbarHostState: SnackbarHostState,
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddClick: () -> Unit,
    onItemClick: (Tirada) -> Unit,
    onItemLongClick: (Tirada) -> Unit,
    onDeleteClick: (Tirada) -> Unit,
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
                Icon(Icons.Default.Add, contentDescription = "Añadir tirada")
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        if (tiradas.isEmpty()) {
            EmptyState(
                message = "No hay tiradas registradas",
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
                    items = tiradas,
                    key = { it.id }
                ) { tirada ->
                    TiradaItem(
                        tirada = tirada,
                        onClick = { onItemClick(tirada) },
                        onLongClick = { onItemLongClick(tirada) },
                        onDelete = { onDeleteClick(tirada) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TiradasScreenContentPreview() {
    MunicionTheme {
        TiradasScreenContent(
            tiradas = listOf(
                Tirada(
                    id = 1,
                    descripcion = "Práctica semanal",
                    rango = "Galería Municipal",
                    fecha = "01/01/2024",
                    puntuacion = 520
                ),
                Tirada(
                    id = 2,
                    descripcion = "Competición regional",
                    rango = "Club de Tiro",
                    fecha = "15/01/2024",
                    puntuacion = 380
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
private fun TiradasScreenContentEmptyPreview() {
    MunicionTheme {
        TiradasScreenContent(
            tiradas = emptyList(),
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
