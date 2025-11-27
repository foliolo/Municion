package al.ahgitdevelopment.municion.ui.tiradas

import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import al.ahgitdevelopment.municion.ui.components.DeleteConfirmationDialog
import al.ahgitdevelopment.municion.ui.components.EmptyState
import al.ahgitdevelopment.municion.ui.navigation.Route
import al.ahgitdevelopment.municion.ui.navigation.TiradaForm
import al.ahgitdevelopment.municion.ui.navigation.navtypes.navigateSafely
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import al.ahgitdevelopment.municion.ui.viewmodel.TiradaViewModel
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

/**
 * Contenido de la pantalla de Tiradas para Single Scaffold Architecture.
 *
 * NO contiene Scaffold, TopBar ni FAB - estos están en MainScreen.
 * Maneja el ViewModel, navegación y efectos secundarios.
 *
 * @param navController Controlador de navegación
 * @param snackbarHostState Estado del snackbar compartido desde MainScreen
 * @param viewModel ViewModel de Tiradas (inyectado por Hilt)
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 */
@Composable
fun TiradasContent(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    viewModel: TiradaViewModel = hiltViewModel()
) {
    val tiradas by viewModel.tiradas.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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

    TiradasListContent(
        tiradas = tiradas,
        onItemClick = { /* Info */ },
        onItemLongClick = { tirada ->
            navController.navigateSafely(TiradaForm(tirada = tirada))
        },
        onDeleteClick = { tirada -> tiradaToDelete = tirada }
    )
}

/**
 * Contenido de la lista de Tiradas (Stateless).
 *
 * Sin Scaffold - solo el contenido de la lista.
 * Recibe estado y callbacks como parámetros.
 * Fácil de previsualizar y testear.
 *
 * @param tiradas Lista de tiradas a mostrar
 * @param onItemClick Callback para click en item
 * @param onItemLongClick Callback para long-press (editar)
 * @param onDeleteClick Callback para swipe-to-delete
 * @param modifier Modificador opcional
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 */
@Composable
fun TiradasListContent(
    tiradas: List<Tirada>,
    onItemClick: (Tirada) -> Unit,
    onItemLongClick: (Tirada) -> Unit,
    onDeleteClick: (Tirada) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tiradas.isEmpty()) {
        EmptyState(
            message = "No hay tiradas registradas",
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

@Preview(showBackground = true)
@Composable
private fun TiradasListContentPreview() {
    MunicionTheme {
        TiradasListContent(
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
            onItemClick = {},
            onItemLongClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TiradasListContentEmptyPreview() {
    MunicionTheme {
        TiradasListContent(
            tiradas = emptyList(),
            onItemClick = {},
            onItemLongClick = {},
            onDeleteClick = {}
        )
    }
}
