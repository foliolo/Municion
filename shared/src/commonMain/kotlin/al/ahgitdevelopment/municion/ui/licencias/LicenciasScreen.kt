package al.ahgitdevelopment.municion.ui.licencias

import al.ahgitdevelopment.municion.ads.NativeAdHandle
import al.ahgitdevelopment.municion.ads.NativeAdManager
import al.ahgitdevelopment.municion.ads.itemsWithNativeAds
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.dialog_delete_license_message
import al.ahgitdevelopment.municion.resources.dialog_delete_license_title
import al.ahgitdevelopment.municion.resources.empty_no_licenses
import al.ahgitdevelopment.municion.ui.components.DataQualityBanner
import al.ahgitdevelopment.municion.ui.components.DeleteConfirmationDialog
import al.ahgitdevelopment.municion.ui.components.EmptyState
import al.ahgitdevelopment.municion.ui.navigation.LicenciaForm
import al.ahgitdevelopment.municion.ui.viewmodel.EntityUiState
import al.ahgitdevelopment.municion.ui.viewmodel.LicenciaViewModel
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

/** Stateful Licencias content (no Scaffold — TopBar/BottomBar/FAB live in MainScreen). */
@Composable
fun LicenciasContent(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    viewModel: LicenciaViewModel = koinViewModel(),
) {
    val licencias by viewModel.licencias.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val needsAttentionCount by viewModel.needsAttentionCount.collectAsStateWithLifecycle()
    val nativeAdManager = koinInject<NativeAdManager>()
    val nativeAds by nativeAdManager.nativeAds.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { nativeAdManager.loadAds() }

    var licenciaToDelete by remember { mutableStateOf<Licencia?>(null) }

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

    licenciaToDelete?.let { licencia ->
        DeleteConfirmationDialog(
            title = stringResource(Res.string.dialog_delete_license_title),
            message = stringResource(Res.string.dialog_delete_license_message, licencia.numLicencia),
            onConfirm = {
                viewModel.deleteLicencia(licencia)
                licenciaToDelete = null
            },
            onDismiss = { licenciaToDelete = null },
        )
    }

    LicenciasListContent(
        licencias = licencias,
        nativeAds = nativeAds,
        needsAttentionCount = needsAttentionCount,
        onItemClick = { navController.navigate(LicenciaForm(licenciaId = it.id)) },
        onDeleteClick = { licenciaToDelete = it },
    )
}

@Composable
fun LicenciasListContent(
    licencias: List<Licencia>,
    onItemClick: (Licencia) -> Unit,
    onDeleteClick: (Licencia) -> Unit,
    nativeAds: List<NativeAdHandle> = emptyList(),
    needsAttentionCount: Int = 0,
    modifier: Modifier = Modifier,
) {
    if (licencias.isEmpty()) {
        EmptyState(message = stringResource(Res.string.empty_no_licenses), modifier = modifier.fillMaxSize())
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 8.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            if (needsAttentionCount > 0) {
                item(key = "data_quality_banner") {
                    DataQualityBanner(count = needsAttentionCount, entityLabel = "licencia")
                }
            }
            itemsWithNativeAds(items = licencias, nativeAds = nativeAds, key = { it.id }) { licencia ->
                LicenciaItem(
                    licencia = licencia,
                    onClick = { onItemClick(licencia) },
                    onDelete = { onDeleteClick(licencia) },
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
    }
}
