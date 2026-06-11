package al.ahgitdevelopment.municion.ui.compras

import al.ahgitdevelopment.municion.ads.NativeAdHandle
import al.ahgitdevelopment.municion.ads.NativeAdManager
import al.ahgitdevelopment.municion.ads.itemsWithNativeAds
import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.dialog_delete_purchase_message
import al.ahgitdevelopment.municion.resources.dialog_delete_purchase_title
import al.ahgitdevelopment.municion.resources.empty_no_purchases
import al.ahgitdevelopment.municion.ui.components.DataQualityBanner
import al.ahgitdevelopment.municion.ui.components.DeleteConfirmationDialog
import al.ahgitdevelopment.municion.ui.components.EmptyState
import al.ahgitdevelopment.municion.ui.navigation.CompraForm
import al.ahgitdevelopment.municion.ui.viewmodel.CompraViewModel
import al.ahgitdevelopment.municion.ui.viewmodel.EntityUiState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/** Stateful Compras content (no Scaffold — TopBar/BottomBar/FAB live in MainScreen). */
@Composable
fun ComprasContent(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    viewModel: CompraViewModel = koinViewModel(),
) {
    val compras by viewModel.compras.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val needsAttentionCount by viewModel.needsAttentionCount.collectAsStateWithLifecycle()
    val nativeAdManager = koinInject<NativeAdManager>()
    val nativeAds by nativeAdManager.nativeAds.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { nativeAdManager.loadAds() }

    var compraToDelete by remember { mutableStateOf<Compra?>(null) }

    LaunchedEffect(uiState) {
        when (val s = uiState) {
            is EntityUiState.Success -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetUiState()
            }
            is EntityUiState.Error -> {
                snackbarHostState.showSnackbar("Error: ${s.message}")
                viewModel.resetUiState()
            }
            else -> Unit
        }
    }

    compraToDelete?.let { compra ->
        DeleteConfirmationDialog(
            title = stringResource(Res.string.dialog_delete_purchase_title),
            message = stringResource(Res.string.dialog_delete_purchase_message, compra.marca),
            onConfirm = {
                viewModel.deleteCompra(compra)
                compraToDelete = null
            },
            onDismiss = { compraToDelete = null },
        )
    }

    ComprasListContent(
        compras = compras,
        nativeAds = nativeAds,
        needsAttentionCount = needsAttentionCount,
        onItemClick = { navController.navigate(CompraForm(compraId = it.id, guiaId = it.idPosGuia)) },
        onDeleteClick = { compraToDelete = it },
    )
}

@Composable
fun ComprasListContent(
    compras: List<Compra>,
    onItemClick: (Compra) -> Unit,
    onDeleteClick: (Compra) -> Unit,
    onImageClick: (String) -> Unit = {},
    nativeAds: List<NativeAdHandle> = emptyList(),
    needsAttentionCount: Int = 0,
    modifier: Modifier = Modifier,
) {
    if (compras.isEmpty()) {
        EmptyState(message = stringResource(Res.string.empty_no_purchases), modifier = modifier.fillMaxSize())
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 8.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            if (needsAttentionCount > 0) {
                item(key = "data_quality_banner") {
                    DataQualityBanner(count = needsAttentionCount, entityLabel = "compra")
                }
            }
            itemsWithNativeAds(items = compras, nativeAds = nativeAds, key = { it.id }) { compra ->
                CompraItem(
                    compra = compra,
                    onClick = { onItemClick(compra) },
                    onDelete = { onDeleteClick(compra) },
                    onImageClick = onImageClick,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
    }
}
