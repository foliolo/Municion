package al.ahgitdevelopment.municion.ui.compras

import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.ui.components.DeleteConfirmationDialog
import al.ahgitdevelopment.municion.ui.components.EmptyState
import al.ahgitdevelopment.municion.ui.navigation.Routes
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import al.ahgitdevelopment.municion.ui.viewmodel.CompraViewModel
import al.ahgitdevelopment.municion.ui.viewmodel.GuiaViewModel

/**
 * Pantalla de listado de Compras (Stateful).
 *
 * Maneja el ViewModel, navegación y efectos secundarios.
 * Delega la UI a ComprasScreenContent (stateless).
 *
 * @param navController Controlador de navegación
 * @param compraViewModel ViewModel de Compras
 * @param guiaViewModel ViewModel de Guías (para selección)
 *
 * @since v3.0.0 (Compose Migration)
 */
@Composable
fun ComprasScreen(
    navController: NavHostController,
    compraViewModel: CompraViewModel = hiltViewModel(),
    guiaViewModel: GuiaViewModel = hiltViewModel()
) {
    val compras by compraViewModel.compras.collectAsStateWithLifecycle()
    val guias by guiaViewModel.guias.collectAsStateWithLifecycle()
    val uiState by compraViewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var compraToDelete by remember { mutableStateOf<Compra?>(null) }
    var showGuiaSelectionDialog by remember { mutableStateOf(false) }

    // Mostrar mensajes de UiState
    LaunchedEffect(uiState) {
        when (uiState) {
            is CompraViewModel.CompraUiState.Success -> {
                snackbarHostState.showSnackbar(
                    (uiState as CompraViewModel.CompraUiState.Success).message
                )
                compraViewModel.resetUiState()
            }
            is CompraViewModel.CompraUiState.Error -> {
                val errorMessage = (uiState as CompraViewModel.CompraUiState.Error).message
                val userFriendlyMessage = formatErrorMessage(errorMessage)
                snackbarHostState.showSnackbar(userFriendlyMessage)
                compraViewModel.resetUiState()
            }
            else -> {}
        }
    }

    // Dialog de confirmación de eliminación
    compraToDelete?.let { compra ->
        DeleteConfirmationDialog(
            title = "Eliminar compra",
            message = "¿Estás seguro de que deseas eliminar esta compra de ${compra.marca}?",
            onConfirm = {
                compraViewModel.deleteCompra(compra)
                compraToDelete = null
            },
            onDismiss = { compraToDelete = null }
        )
    }

    // Dialog de selección de guía
    if (showGuiaSelectionDialog) {
        GuiaSelectionDialog(
            guias = guias,
            onGuiaSelected = { guia ->
                navController.navigate(
                    Routes.compraForm(
                        guiaId = guia.id,
                        cupoDisponible = guia.disponible(),
                        cupoTotal = guia.cupo
                    )
                )
                showGuiaSelectionDialog = false
            },
            onDismiss = { showGuiaSelectionDialog = false }
        )
    }

    ComprasScreenContent(
        compras = compras,
        guias = guias,
        snackbarHostState = snackbarHostState,
        onAddClick = {
            if (guias.isEmpty()) {
                // No hay guías
            } else {
                showGuiaSelectionDialog = true
            }
        },
        onItemClick = { /* Info */ },
        onItemLongClick = { compra ->
            val guia = guias.find { it.id == compra.idPosGuia }
            guia?.let { g ->
                navController.navigate(
                    Routes.compraForm(
                        guiaId = g.id,
                        cupoDisponible = g.disponible() + compra.unidades,
                        cupoTotal = g.cupo,
                        compraId = compra.id
                    )
                )
            }
        },
        onDeleteClick = { compra -> compraToDelete = compra }
    )
}

/**
 * Contenido de la pantalla de Compras (Stateless).
 *
 * Recibe estado y callbacks como parámetros.
 * Fácil de previsualizar y testear.
 *
 * @param compras Lista de compras a mostrar
 * @param guias Lista de guías disponibles
 * @param snackbarHostState Estado del snackbar
 * @param onAddClick Callback para añadir compra
 * @param onItemClick Callback para click en item
 * @param onItemLongClick Callback para long-press (editar)
 * @param onDeleteClick Callback para swipe-to-delete
 *
 * @since v3.0.0 (Compose Migration)
 */
@Composable
fun ComprasScreenContent(
    compras: List<Compra>,
    guias: List<Guia>,
    snackbarHostState: SnackbarHostState,
    onAddClick: () -> Unit,
    onItemClick: (Compra) -> Unit,
    onItemLongClick: (Compra) -> Unit,
    onDeleteClick: (Compra) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir compra")
            }
        }
    ) { innerPadding ->
        if (compras.isEmpty()) {
            EmptyState(
                message = "No hay compras registradas",
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    items = compras,
                    key = { it.id }
                ) { compra ->
                    CompraItem(
                        compra = compra,
                        onClick = { onItemClick(compra) },
                        onLongClick = { onItemLongClick(compra) },
                        onDelete = { onDeleteClick(compra) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Convierte mensajes de error técnicos a mensajes amigables
 */
private fun formatErrorMessage(message: String): String {
    return when {
        message.contains("Cupo insuficiente") -> {
            val regex = Regex("Disponible: (\\d+), Requerido: (\\d+)")
            val match = regex.find(message)
            if (match != null) {
                val disponible = match.groupValues[1]
                val requerido = match.groupValues[2]
                "No puedes comprar $requerido unidades. Solo tienes $disponible de cupo disponible."
            } else {
                "Cupo insuficiente para esta compra."
            }
        }
        message.contains("Guía no encontrada") -> "Error: La guía seleccionada ya no existe."
        else -> "Error: $message"
    }
}

@Preview(showBackground = true)
@Composable
private fun ComprasScreenContentPreview() {
    MunicionTheme {
        ComprasScreenContent(
            compras = listOf(
                Compra(
                    id = 1,
                    idPosGuia = 1,
                    marca = "Federal",
                    calibre1 = "9mm Para",
                    tipo = "FMJ",
                    peso = 115,
                    unidades = 50,
                    precio = 25.99,
                    fecha = "01/01/2024"
                ),
                Compra(
                    id = 2,
                    idPosGuia = 2,
                    marca = "Winchester",
                    calibre1 = "12/70",
                    tipo = "Perdigón",
                    peso = 28,
                    unidades = 25,
                    precio = 15.50,
                    fecha = "15/01/2024"
                )
            ),
            guias = emptyList(),
            snackbarHostState = SnackbarHostState(),
            onAddClick = {},
            onItemClick = {},
            onItemLongClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ComprasScreenContentEmptyPreview() {
    MunicionTheme {
        ComprasScreenContent(
            compras = emptyList(),
            guias = emptyList(),
            snackbarHostState = SnackbarHostState(),
            onAddClick = {},
            onItemClick = {},
            onItemLongClick = {},
            onDeleteClick = {}
        )
    }
}
