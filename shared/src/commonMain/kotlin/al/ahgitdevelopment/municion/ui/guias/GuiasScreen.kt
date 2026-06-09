package al.ahgitdevelopment.municion.ui.guias

import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.dialog_delete_guide_message
import al.ahgitdevelopment.municion.resources.dialog_delete_guide_title
import al.ahgitdevelopment.municion.resources.empty_no_guides
import al.ahgitdevelopment.municion.ui.components.DataQualityBanner
import al.ahgitdevelopment.municion.ui.components.DeleteConfirmationDialog
import al.ahgitdevelopment.municion.ui.components.EmptyState
import al.ahgitdevelopment.municion.ui.navigation.GuiaForm
import al.ahgitdevelopment.municion.ui.viewmodel.EntityUiState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Stateful Guías content (no Scaffold — TopBar/BottomBar/FAB live in MainScreen). */
@Composable
fun GuiasContent(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    viewModel: GuiaViewModel = koinViewModel(),
) {
    val guias by viewModel.guias.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val needsAttentionCount by viewModel.needsAttentionCount.collectAsStateWithLifecycle()

    var guiaToDelete by remember { mutableStateOf<Guia?>(null) }

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

    guiaToDelete?.let { guia ->
        DeleteConfirmationDialog(
            title = stringResource(Res.string.dialog_delete_guide_title),
            message = stringResource(Res.string.dialog_delete_guide_message, guia.numGuia),
            onConfirm = {
                viewModel.deleteGuia(guia)
                guiaToDelete = null
            },
            onDismiss = { guiaToDelete = null },
        )
    }

    GuiasListContent(
        guias = guias,
        needsAttentionCount = needsAttentionCount,
        onItemClick = { navController.navigate(GuiaForm(guiaId = it.id)) },
        onDeleteClick = { guiaToDelete = it },
    )
}

@Composable
fun GuiasListContent(
    guias: List<Guia>,
    onItemClick: (Guia) -> Unit,
    onDeleteClick: (Guia) -> Unit,
    onImageClick: ((String) -> Unit)? = null,
    needsAttentionCount: Int = 0,
    modifier: Modifier = Modifier,
) {
    if (guias.isEmpty()) {
        EmptyState(message = stringResource(Res.string.empty_no_guides), modifier = modifier.fillMaxSize())
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 8.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            if (needsAttentionCount > 0) {
                item(key = "data_quality_banner") {
                    DataQualityBanner(count = needsAttentionCount, entityLabel = "guía")
                }
            }
            items(items = guias, key = { it.id }) { guia ->
                GuiaItem(
                    guia = guia,
                    onClick = { onItemClick(guia) },
                    onDelete = { onDeleteClick(guia) },
                    onImageClick = onImageClick,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
    }
}
