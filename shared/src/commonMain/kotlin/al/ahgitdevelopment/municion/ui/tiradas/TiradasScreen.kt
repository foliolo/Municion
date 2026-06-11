package al.ahgitdevelopment.municion.ui.tiradas

import al.ahgitdevelopment.municion.ads.NativeAdHandle
import al.ahgitdevelopment.municion.ads.NativeAdManager
import al.ahgitdevelopment.municion.ads.itemsWithNativeAds
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.dialog_delete_competition_message
import al.ahgitdevelopment.municion.resources.dialog_delete_competition_title
import al.ahgitdevelopment.municion.resources.empty_no_competitions
import al.ahgitdevelopment.municion.ui.components.DataQualityBanner
import al.ahgitdevelopment.municion.ui.components.DeleteConfirmationDialog
import al.ahgitdevelopment.municion.ui.components.EmptyState
import al.ahgitdevelopment.municion.ui.navigation.TiradaForm
import al.ahgitdevelopment.municion.ui.viewmodel.EntityUiState
import al.ahgitdevelopment.municion.ui.viewmodel.TiradaViewModel
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

/** Stateful Tiradas content (no Scaffold — TopBar/BottomBar/FAB live in MainScreen). */
@Composable
fun TiradasContent(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    viewModel: TiradaViewModel = koinViewModel(),
) {
    val tiradas by viewModel.tiradas.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val needsAttentionCount by viewModel.needsAttentionCount.collectAsStateWithLifecycle()
    val nativeAdManager = koinInject<NativeAdManager>()
    val nativeAds by nativeAdManager.nativeAds.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { nativeAdManager.loadAds() }

    var tiradaToDelete by remember { mutableStateOf<Tirada?>(null) }

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

    tiradaToDelete?.let { tirada ->
        DeleteConfirmationDialog(
            title = stringResource(Res.string.dialog_delete_competition_title),
            message = stringResource(Res.string.dialog_delete_competition_message, tirada.descripcion),
            onConfirm = {
                viewModel.deleteTirada(tirada)
                tiradaToDelete = null
            },
            onDismiss = { tiradaToDelete = null },
        )
    }

    TiradasListContent(
        tiradas = tiradas,
        nativeAds = nativeAds,
        needsAttentionCount = needsAttentionCount,
        onItemClick = { navController.navigate(TiradaForm(tiradaId = it.id)) },
        onDeleteClick = { tiradaToDelete = it },
    )
}

@Composable
fun TiradasListContent(
    tiradas: List<Tirada>,
    onItemClick: (Tirada) -> Unit,
    onDeleteClick: (Tirada) -> Unit,
    nativeAds: List<NativeAdHandle> = emptyList(),
    needsAttentionCount: Int = 0,
    modifier: Modifier = Modifier,
) {
    if (tiradas.isEmpty()) {
        EmptyState(message = stringResource(Res.string.empty_no_competitions), modifier = modifier.fillMaxSize())
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 8.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            if (needsAttentionCount > 0) {
                item(key = "data_quality_banner") {
                    DataQualityBanner(count = needsAttentionCount, entityLabel = "tirada")
                }
            }
            itemsWithNativeAds(items = tiradas, nativeAds = nativeAds, key = { it.id }) { tirada ->
                TiradaItem(
                    tirada = tirada,
                    onClick = { onItemClick(tirada) },
                    onDelete = { onDeleteClick(tirada) },
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
    }
}
