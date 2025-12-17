package al.ahgitdevelopment.municion.ui.compras

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.ui.components.DeleteConfirmationDialog
import al.ahgitdevelopment.municion.ui.components.EmptyState
import al.ahgitdevelopment.municion.ui.components.ZoomableImageDialog
import al.ahgitdevelopment.municion.ui.navigation.CompraForm
import al.ahgitdevelopment.municion.ui.navigation.navtypes.navigateSafely
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import al.ahgitdevelopment.municion.ui.viewmodel.CompraViewModel
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

/**
 * Contenido de la pantalla de Compras para Single Scaffold Architecture.
 *
 * NO contiene Scaffold, TopBar ni FAB - estos están en MainScreen.
 * El dialog de selección de guía también está en MainScreen.
 * Maneja el ViewModel, navegación y efectos secundarios.
 *
 * @param navController Controlador de navegación
 * @param snackbarHostState Estado del snackbar compartido desde MainScreen
 * @param compraViewModel ViewModel de Compras
 * @param guiaViewModel ViewModel de Guías (para obtener datos de guía al editar)
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 */
@Composable
fun ComprasContent(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    compraViewModel: CompraViewModel = hiltViewModel(),
    guiaViewModel: GuiaViewModel = hiltViewModel()
) {
    val compras by compraViewModel.compras.collectAsStateWithLifecycle()
    val guias by guiaViewModel.guias.collectAsStateWithLifecycle()
    val uiState by compraViewModel.uiState.collectAsStateWithLifecycle()

    var compraToDelete by remember { mutableStateOf<Compra?>(null) }
    var imageUrlToShow by remember { mutableStateOf<String?>(null) }

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
            title = stringResource(R.string.dialog_delete_purchase_title),
            message = stringResource(R.string.dialog_delete_purchase_message, compra.marca),
            onConfirm = {
                compraViewModel.deleteCompra(compra)
                compraToDelete = null
            },
            onDismiss = { compraToDelete = null }
        )
    }

    // Dialog de imagen con zoom
    imageUrlToShow?.let { imageUrl ->
        ZoomableImageDialog(
            imageUrl = imageUrl,
            contentDescription = stringResource(R.string.content_description_ammunition_image),
            onDismiss = { imageUrlToShow = null }
        )
    }

    ComprasListContent(
        compras = compras,
        onItemClick = { /* Info */ },
        onItemLongClick = { compra ->
            val guia = guias.find { it.id == compra.idPosGuia }
            guia?.let { g ->
                // Restaurar cupo para edicion: guia actual + unidades de esta compra
                // Use liberarCupo to prevent negative gastado values (Fixes IllegalArgumentException)
                val guiaConCupoRestaurado = g.liberarCupo(compra.unidades)
                navController.navigateSafely(
                    CompraForm(
                        compra = compra,
                        guia = guiaConCupoRestaurado
                    )
                )
            }
        },
        onDeleteClick = { compra -> compraToDelete = compra },
        onImageClick = { url -> imageUrlToShow = url }
    )
}

/**
 * Contenido de la lista de Compras (Stateless).
 *
 * Sin Scaffold - solo el contenido de la lista.
 * Recibe estado y callbacks como parámetros.
 * Fácil de previsualizar y testear.
 *
 * @param compras Lista de compras a mostrar
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
fun ComprasListContent(
    compras: List<Compra>,
    onItemClick: (Compra) -> Unit,
    onItemLongClick: (Compra) -> Unit,
    onDeleteClick: (Compra) -> Unit,
    onImageClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (compras.isEmpty()) {
        EmptyState(
            message = stringResource(R.string.empty_no_purchases),
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
                items = compras,
                key = { it.id }
            ) { compra ->
                CompraItem(
                    compra = compra,
                    onClick = { onItemClick(compra) },
                    onLongClick = { onItemLongClick(compra) },
                    onDelete = { onDeleteClick(compra) },
                    onImageClick = onImageClick,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
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
private fun ComprasListContentPreview() {
    MunicionTheme {
        ComprasListContent(
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
            onItemClick = {},
            onItemLongClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ComprasListContentEmptyPreview() {
    MunicionTheme {
        ComprasListContent(
            compras = emptyList(),
            onItemClick = {},
            onItemLongClick = {},
            onDeleteClick = {}
        )
    }
}
